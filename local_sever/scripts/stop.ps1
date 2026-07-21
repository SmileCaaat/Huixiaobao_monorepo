# ASCII-only stopper for Huixiaobao local_sever
param(
  [switch]$RestoreMiniprogram,
  [switch]$KeepMiniprogram
)

$ErrorActionPreference = "Continue"
$Root = Split-Path -Parent $PSScriptRoot
$PidDir = Join-Path $Root ".pids"
$ConfigPath = Join-Path $Root "config.env"

function Read-EnvFile {
  param([string]$Path)
  $map = @{}
  if (-not (Test-Path $Path)) { return $map }
  Get-Content -Path $Path -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if (-not $line -or $line.StartsWith("#")) { return }
    $idx = $line.IndexOf("=")
    if ($idx -lt 1) { return }
    $key = $line.Substring(0, $idx).Trim()
    $val = $line.Substring($idx + 1).Trim()
    $map[$key] = $val
  }
  return $map
}

function Stop-PidFile([string]$File, [string]$Label) {
  if (-not (Test-Path $File)) {
    Write-Host "[local_sever] No $Label pid file."
    return
  }
  $pidText = (Get-Content -Path $File -Raw).Trim()
  if ($pidText -match "^\d+$") {
    $procId = [int]$pidText
    try {
      $p = Get-Process -Id $procId -ErrorAction Stop
      Write-Host "[local_sever] Stopping $Label pid=$procId ($($p.ProcessName))"
      Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
    } catch {
      Write-Host "[local_sever] $Label pid=$procId not running."
    }
  }
  Remove-Item -Force $File -ErrorAction SilentlyContinue
}

function Stop-ListenersOnPort([int]$Port, [string]$Label) {
  try {
    $conns = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $conns) { return }
    $ids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($procId in $ids) {
      if (-not $procId -or $procId -eq 0) { continue }
      try {
        $p = Get-Process -Id $procId -ErrorAction Stop
        Write-Host "[local_sever] Stopping $Label on port $Port pid=$procId ($($p.ProcessName))"
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
      } catch { }
    }
  } catch { }
}

$cfg = Read-EnvFile $ConfigPath
$dashPort = 3080
if ($cfg.ContainsKey("DASHBOARD_PORT") -and $cfg["DASHBOARD_PORT"]) {
  $dashPort = [int]$cfg["DASHBOARD_PORT"]
}

Stop-PidFile (Join-Path $PidDir "gateway.pid") "gateway"
Stop-ListenersOnPort $dashPort "dashboard"
Stop-PidFile (Join-Path $PidDir "backend.pid") "backend"

$shouldRestore = $false
if ($RestoreMiniprogram.IsPresent) {
  $shouldRestore = $true
} elseif (-not $KeepMiniprogram.IsPresent) {
  if ($cfg.ContainsKey("SWITCH_MINIPROGRAM") -and $cfg["SWITCH_MINIPROGRAM"] -ne "0") {
    $shouldRestore = $true
  }
}

if ($shouldRestore) {
  $prod = "https://huixiaobao-admin.site"
  if ($cfg.ContainsKey("MINIPROGRAM_PROD_URL") -and $cfg["MINIPROGRAM_PROD_URL"]) {
    $prod = $cfg["MINIPROGRAM_PROD_URL"]
  }
  Write-Host "[local_sever] Restoring miniprogram BASE_URL to prod..."
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "switch-miniprogram-env.ps1") -Mode prod -BaseUrl $prod
}

Write-Host "[local_sever] Stopped."

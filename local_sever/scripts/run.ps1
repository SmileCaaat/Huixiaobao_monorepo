# ASCII-only foreground runner for local_sever
# Runs node in THIS console so Maven stdio inherit shows progress in start.bat.

param(
  [switch]$SkipBackend,
  [switch]$SkipInstall,
  [switch]$SkipMiniprogram
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$PidDir = Join-Path $Root ".pids"
$GatewayPidFile = Join-Path $PidDir "gateway.pid"

$prepareArgs = @()
if ($SkipBackend) { $prepareArgs += "-SkipBackend" }
if ($SkipInstall) { $prepareArgs += "-SkipInstall" }
if ($SkipMiniprogram) { $prepareArgs += "-SkipMiniprogram" }

& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "prepare.ps1") @prepareArgs
if ($LASTEXITCODE -ne 0) {
  Write-Host "[local_sever] prepare failed."
  exit $LASTEXITCODE
}

$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" +
            [System.Environment]::GetEnvironmentVariable("Path", "User")

$cfgPath = Join-Path $Root "config.env"
$dashPort = 3080
if (Test-Path $cfgPath) {
  Get-Content $cfgPath -Encoding UTF8 | ForEach-Object {
    if ($_ -match '^\s*DASHBOARD_PORT\s*=\s*(\d+)') { $dashPort = [int]$Matches[1] }
  }
}

Write-Host ""
Write-Host "[local_sever] Dashboard: http://127.0.0.1:$dashPort/"
Write-Host "[local_sever] keep this window open. Press Ctrl+C to stop."
Write-Host "[local_sever] Maven Clean/Package progress will print HERE."
Write-Host "----------------------------------------"

New-Item -ItemType Directory -Force -Path $PidDir | Out-Null
Set-Content -Path $GatewayPidFile -Value $PID -Encoding ASCII

Push-Location $Root
$exitCode = 0
try {
  # Same-console node: required for child Maven stdio inherit to be visible
  & node server.js
  $exitCode = $LASTEXITCODE
} catch {
  Write-Host "[local_sever] ERROR: $($_.Exception.Message)"
  $exitCode = 1
} finally {
  Pop-Location
  Write-Host "[local_sever] cleaning up..."
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "stop.ps1") -KeepMiniprogram
}

if ($null -eq $exitCode) { $exitCode = 0 }
if ($exitCode -ne 0) {
  Write-Host "[local_sever] gateway exited with code $exitCode"
  exit $exitCode
}

Write-Host "[local_sever] stopped."
exit 0

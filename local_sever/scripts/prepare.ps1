# ASCII-only prepare step for local_sever start.bat
# Installs deps, switches miniprogram, optionally starts backend.
# Does NOT start the Node gateway (start.bat runs node in foreground).

param(
  [switch]$SkipBackend,
  [switch]$SkipInstall,
  [switch]$SkipMiniprogram
)

$ErrorActionPreference = "Stop"

$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" +
            [System.Environment]::GetEnvironmentVariable("Path", "User")
$javaHome = [System.Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
if (-not $javaHome) {
  $javaHome = [System.Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
}
if ($javaHome) {
  $env:JAVA_HOME = $javaHome
  $env:Path = "$javaHome\bin;" + $env:Path
}

$Root = Split-Path -Parent $PSScriptRoot
# Ensure local MySQL is up (3306)
& (Join-Path $Root "start-mysql.bat")
Start-Sleep -Seconds 2
$mysqlListening = $false
try {
  $mysqlListening = $null -ne (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)
} catch { }
if ($mysqlListening) {
  Write-Host "[local_sever] MySQL port 3306 is listening"
} else {
  Write-Host "[local_sever] WARN: MySQL port 3306 is NOT listening"
}

$RepoRoot = Split-Path -Parent $Root
$ConfigPath = Join-Path $Root "config.env"
$ExampleConfig = Join-Path $Root "config.example.env"
$PidDir = Join-Path $Root ".pids"
$LogDir = Join-Path $Root "logs"
$StateDir = Join-Path $Root "state"

New-Item -ItemType Directory -Force -Path $PidDir, $LogDir, $StateDir | Out-Null

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
  if (-not (Test-Path $File)) { return }
  $pidText = (Get-Content -Path $File -Raw -ErrorAction SilentlyContinue)
  if (-not $pidText) {
    Remove-Item -Force $File -ErrorAction SilentlyContinue
    return
  }
  $pidText = $pidText.Trim()
  if ($pidText -match "^\d+$") {
    $procId = [int]$pidText
    try {
      $p = Get-Process -Id $procId -ErrorAction Stop
      Write-Host "[local_sever] Stopping old $Label pid=$procId ($($p.ProcessName))"
      Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
      Start-Sleep -Milliseconds 400
    } catch { }
  }
  Remove-Item -Force $File -ErrorAction SilentlyContinue
}

function Test-PortInUse([int]$Port) {
  try {
    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    return $null -ne $conn
  } catch {
    return $false
  }
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
        Write-Host "[local_sever] Port $Port busy by pid=$procId ($($p.ProcessName)), stopping ($Label)..."
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
      } catch { }
    }
    Start-Sleep -Milliseconds 500
  } catch { }
}

function Assert-Command([string]$Name) {
  $cmd = Get-Command $Name -ErrorAction SilentlyContinue
  if (-not $cmd) {
    throw "Required command not found: $Name. Install it and reopen the terminal."
  }
  return $cmd.Source
}

if (-not (Test-Path $ConfigPath)) {
  Copy-Item $ExampleConfig $ConfigPath
  Write-Host "[local_sever] Created config.env from config.example.env"
  Write-Host "[local_sever] Edit DB_PASS in config.env before relying on backend startup."
}

$cfg = Read-EnvFile $ConfigPath

function Get-Cfg([string]$Key, [string]$Default) {
  if ($cfg.ContainsKey($Key) -and $cfg[$Key] -ne "") { return $cfg[$Key] }
  return $Default
}

$DashboardPort = [int](Get-Cfg "DASHBOARD_PORT" "3080")
$BackendPort = [int](Get-Cfg "BACKEND_PORT" "83")
$AutoStartBackend = (Get-Cfg "AUTO_START_BACKEND" "1") -ne "0"
$AutoBuildBackend = (Get-Cfg "AUTO_BUILD_BACKEND" "0") -eq "1"
$SwitchMini = (Get-Cfg "SWITCH_MINIPROGRAM" "1") -ne "0"
$OpenBrowser = (Get-Cfg "OPEN_BROWSER" "1") -ne "0"
$MiniBase = Get-Cfg "MINIPROGRAM_BASE_URL" ("http://127.0.0.1:" + $BackendPort)

$DbHost = Get-Cfg "DB_HOST" "127.0.0.1"
$DbPort = Get-Cfg "DB_PORT" "3306"
$DbName = Get-Cfg "DB_NAME" "dev_manager"
$DbUser = Get-Cfg "DB_USER" "root"
$DbPass = Get-Cfg "DB_PASS" "replace-me"
$DruidUser = Get-Cfg "DRUID_USER" "admin"
$DruidPass = Get-Cfg "DRUID_PASS" "replace-me"

$GatewayPidFile = Join-Path $PidDir "gateway.pid"
$BackendPidFile = Join-Path $PidDir "backend.pid"
$BackendOutLog = Join-Path $LogDir "backend.out.log"
$BackendErrLog = Join-Path $LogDir "backend.err.log"

Write-Host "[local_sever] Checking tools..."
Assert-Command "node" | Out-Null
Assert-Command "npm" | Out-Null
Write-Host "[local_sever] node=$(node -v) npm=$(npm -v)"

if (-not $SkipInstall) {
  Write-Host "[local_sever] Installing npm dependencies..."
  Push-Location $Root
  try {
    npm install --no-fund --no-audit
    if ($LASTEXITCODE -ne 0) {
      throw "npm install failed with exit code $LASTEXITCODE"
    }
  } finally {
    Pop-Location
  }
}

$serverJs = Join-Path $Root "server.js"
if (-not (Test-Path $serverJs)) {
  throw "Missing server.js at $serverJs"
}
if (-not (Test-Path (Join-Path $Root "node_modules\express"))) {
  throw "Missing node_modules. Run npm install in local_sever first."
}

if ($SwitchMini -and -not $SkipMiniprogram) {
  Write-Host "[local_sever] Switching miniprogram BASE_URL to local..."
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "switch-miniprogram-env.ps1") -Mode local -BaseUrl $MiniBase
}

Stop-PidFile $GatewayPidFile "gateway"
Stop-ListenersOnPort $DashboardPort "dashboard"

$JarPath = Join-Path $RepoRoot "backend\ruoyi-admin\target\ruoyi-admin.jar"

if ($AutoStartBackend -and -not $SkipBackend) {
  Assert-Command "java" | Out-Null
  # java -version writes to stderr; under ErrorActionPreference=Stop that becomes a terminating error.
  $prevEap = $ErrorActionPreference
  $ErrorActionPreference = "Continue"
  $javaLines = & java -version 2>&1 | ForEach-Object { "$_" }
  $ErrorActionPreference = $prevEap
  Write-Host "[local_sever] java: $($javaLines -join ' | ')"

  if (-not (Test-Path $JarPath)) {
    if ($AutoBuildBackend) {
      Assert-Command "mvn" | Out-Null
      Write-Host "[local_sever] Building backend jar (mvn package)..."
      Push-Location (Join-Path $RepoRoot "backend")
      try {
        mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
          throw "mvn package failed with exit code $LASTEXITCODE"
        }
      } finally {
        Pop-Location
      }
    } else {
      Write-Host "[local_sever] WARN: jar not found: $JarPath"
      Write-Host "[local_sever] Set AUTO_BUILD_BACKEND=1 in config.env or build manually."
    }
  }

  if (Test-Path $JarPath) {
    Stop-PidFile $BackendPidFile "backend"
    if (Test-PortInUse $BackendPort) {
      Write-Host "[local_sever] WARN: port $BackendPort already in use; skip starting another backend."
    } else {
      $env:DB_URL = "jdbc:mysql://${DbHost}:${DbPort}/${DbName}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8"
      $env:DB_USER = $DbUser
      $env:DB_PASS = $DbPass
      $env:DRUID_USER = $DruidUser
      $env:DRUID_PASS = $DruidPass

      if ($DbPass -eq "replace-me") {
        Write-Host "[local_sever] WARN: DB_PASS is still replace-me. Backend may fail to start."
      }

      Write-Host "[local_sever] Starting backend on port $BackendPort (background)..."
      $backendWorkDir = Join-Path $RepoRoot "backend"
      $proc = Start-Process -FilePath "java" `
        -ArgumentList @("-jar", $JarPath, "--server.port=$BackendPort") `
        -WorkingDirectory $backendWorkDir `
        -RedirectStandardOutput $BackendOutLog `
        -RedirectStandardError $BackendErrLog `
        -PassThru `
        -WindowStyle Hidden
      Set-Content -Path $BackendPidFile -Value $proc.Id -Encoding ASCII
      Write-Host "[local_sever] backend pid=$($proc.Id) log=$BackendOutLog"
    }
  }
} else {
  Write-Host "[local_sever] Skip backend auto-start."
}

$dashUrl = "http://127.0.0.1:$DashboardPort/"
Write-Host "[local_sever] prepare ok"
Write-Host "[local_sever] Dashboard will be: $dashUrl"

if ($OpenBrowser) {
  Start-Process $dashUrl
}

exit 0

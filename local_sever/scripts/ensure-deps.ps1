# ASCII-only: detect and auto-install local_sever build dependencies.
# Safe to re-run. Uses winget when available; Maven is fetched as a zip (no winget package).

param(
  [switch]$SkipMysql,
  [switch]$SkipNode
)

$ErrorActionPreference = "Stop"

function Refresh-ProcessPath {
  $machine = [Environment]::GetEnvironmentVariable("Path", "Machine")
  $user = [Environment]::GetEnvironmentVariable("Path", "User")
  $env:Path = @($machine, $user) -join ";"
  $jh = [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
  if (-not $jh) {
    $jh = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
  }
  if ($jh) {
    $env:JAVA_HOME = $jh
    $env:Path = "$jh\bin;" + $env:Path
  }
}

function Test-Cmd([string]$Name) {
  Refresh-ProcessPath
  return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Add-UserPath([string]$Dir) {
  if (-not $Dir -or -not (Test-Path $Dir)) { return }
  $userPath = [Environment]::GetEnvironmentVariable("Path", "User")
  if (-not $userPath) { $userPath = "" }
  $parts = $userPath -split ";" | Where-Object { $_ -and $_.Trim() -ne "" }
  $exists = $parts | Where-Object { $_.TrimEnd("\") -ieq $Dir.TrimEnd("\") }
  if ($exists) { return }
  $newPath = if ($userPath.Trim()) { $userPath.TrimEnd(";") + ";" + $Dir } else { $Dir }
  [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
  Write-Host "[deps] Added to User PATH: $Dir"
}

function Install-WingetPackage([string]$Id, [string]$Label) {
  Refresh-ProcessPath
  if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
    throw "winget not found. Install $Label manually, then reopen the terminal."
  }
  Write-Host "[deps] Installing $Label via winget ($Id)..."
  & winget install -e --id $Id --accept-package-agreements --accept-source-agreements --silent
  if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne -1978335189) {
    # -1978335189 often means already installed
    throw "winget install failed for $Id (exit $LASTEXITCODE)"
  }
  Refresh-ProcessPath
}

function Find-JavaHome {
  $candidates = @(
    [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine"),
    [Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
  )
  $roots = @(
    "C:\Program Files\Eclipse Adoptium",
    "C:\Program Files\Java",
    "C:\Program Files\Microsoft",
    "C:\Program Files\Amazon Corretto"
  )
  foreach ($root in $roots) {
    if (Test-Path $root) {
      Get-ChildItem $root -Directory -ErrorAction SilentlyContinue | ForEach-Object {
        $candidates += $_.FullName
        if (Test-Path (Join-Path $_.FullName "bin\java.exe")) {
          $candidates += $_.FullName
        }
      }
      Get-ChildItem $root -Directory -Recurse -Depth 2 -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
        ForEach-Object { $candidates += $_.FullName }
    }
  }
  foreach ($c in ($candidates | Select-Object -Unique)) {
    if ($c -and (Test-Path (Join-Path $c "bin\java.exe"))) {
      return $c
    }
  }
  return $null
}

function Ensure-Java {
  Refresh-ProcessPath
  if (Test-Cmd "java") {
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $verLines = & java -version 2>&1 | ForEach-Object { "$_" }
    $ErrorActionPreference = $prev
    Write-Host "[deps] java OK: $($verLines[0])"
    $jh = Find-JavaHome
    if ($jh) {
      [Environment]::SetEnvironmentVariable("JAVA_HOME", $jh, "User")
      $env:JAVA_HOME = $jh
      Add-UserPath (Join-Path $jh "bin")
    }
    return
  }

  Install-WingetPackage "EclipseAdoptium.Temurin.21.JDK" "JDK 21 (Temurin)"
  Start-Sleep -Seconds 2
  Refresh-ProcessPath
  $jh = Find-JavaHome
  if (-not $jh) {
    throw "JDK installed but JAVA_HOME not found. Reopen terminal and retry."
  }
  [Environment]::SetEnvironmentVariable("JAVA_HOME", $jh, "User")
  $env:JAVA_HOME = $jh
  Add-UserPath (Join-Path $jh "bin")
  Refresh-ProcessPath
  if (-not (Test-Cmd "java")) {
    throw "java still not on PATH after JDK install."
  }
  Write-Host "[deps] JAVA_HOME=$jh"
}

function Ensure-Maven {
  Refresh-ProcessPath
  if (Test-Cmd "mvn") {
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $ver = (& mvn -version 2>&1 | Select-Object -First 1)
    $ErrorActionPreference = $prev
    Write-Host "[deps] mvn OK: $ver"
    return
  }

  $toolsRoot = Join-Path $env:LOCALAPPDATA "Huixiaobao\tools"
  $mavenVersion = "3.9.9"
  $mavenHome = Join-Path $toolsRoot "apache-maven-$mavenVersion"
  $mavenBin = Join-Path $mavenHome "bin"
  $zipPath = Join-Path $toolsRoot "apache-maven-$mavenVersion-bin.zip"
  $url = "https://dlcdn.apache.org/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
  $fallback = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"

  New-Item -ItemType Directory -Force -Path $toolsRoot | Out-Null

  if (-not (Test-Path (Join-Path $mavenBin "mvn.cmd"))) {
    Write-Host "[deps] Downloading Apache Maven $mavenVersion..."
    try {
      Invoke-WebRequest -Uri $url -OutFile $zipPath -UseBasicParsing
    } catch {
      Write-Host "[deps] Primary mirror failed, trying archive.apache.org..."
      Invoke-WebRequest -Uri $fallback -OutFile $zipPath -UseBasicParsing
    }
    if (Test-Path $mavenHome) {
      Remove-Item -Recurse -Force $mavenHome
    }
    Expand-Archive -Path $zipPath -DestinationPath $toolsRoot -Force
    Remove-Item -Force $zipPath -ErrorAction SilentlyContinue
  }

  if (-not (Test-Path (Join-Path $mavenBin "mvn.cmd"))) {
    throw "Maven extract failed at $mavenHome"
  }

  Add-UserPath $mavenBin
  Refresh-ProcessPath
  if (-not (Test-Cmd "mvn")) {
    # Current process: prepend even if User PATH propagation lags
    $env:Path = "$mavenBin;" + $env:Path
  }
  if (-not (Test-Cmd "mvn")) {
    throw "mvn still not available after install."
  }
  Write-Host "[deps] Maven installed at $mavenHome"
}

function Ensure-Node {
  if ($SkipNode) { return }
  Refresh-ProcessPath
  if ((Test-Cmd "node") -and (Test-Cmd "npm")) {
    Write-Host "[deps] node=$(node -v) npm=$(npm -v)"
    return
  }
  Install-WingetPackage "OpenJS.NodeJS.LTS" "Node.js LTS"
  Refresh-ProcessPath
  if (-not ((Test-Cmd "node") -and (Test-Cmd "npm"))) {
    throw "node/npm still missing after install. Reopen terminal and retry."
  }
  Write-Host "[deps] node=$(node -v) npm=$(npm -v)"
}

function Ensure-MySql {
  if ($SkipMysql) { return }
  Refresh-ProcessPath
  $listening = $false
  try {
    $listening = $null -ne (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)
  } catch { }

  if ($listening) {
    Write-Host "[deps] MySQL port 3306 is listening"
    return
  }

  $svc = Get-Service -Name "MySQL84" -ErrorAction SilentlyContinue
  if ($svc) {
    if ($svc.Status -ne "Running") {
      Write-Host "[deps] Starting Windows service MySQL84..."
      Start-Service MySQL84
      Start-Sleep -Seconds 3
    }
    return
  }

  # Prefer project helper (starts mysqld if binaries exist)
  $root = Split-Path -Parent $PSScriptRoot
  $startMysql = Join-Path $root "start-mysql.bat"
  if (Test-Path $startMysql) {
    Write-Host "[deps] Trying start-mysql.bat..."
    & cmd.exe /c "`"$startMysql`""
    Start-Sleep -Seconds 2
    try {
      $listening = $null -ne (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)
    } catch { }
    if ($listening) {
      Write-Host "[deps] MySQL port 3306 is listening"
      return
    }
  }

  if (-not (Test-Path "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe")) {
    Install-WingetPackage "Oracle.MySQL" "MySQL Server 8.4"
    Write-Host "[deps] MySQL binaries installed. If port 3306 is still down, initialize Data dir once (see docs/09)."
  } else {
    Write-Host "[deps] WARN: MySQL binaries exist but port 3306 is not listening."
  }
}

Write-Host "[deps] Checking required tools..."
Ensure-Node
Ensure-Java
Ensure-Maven
Ensure-MySql
Refresh-ProcessPath
Write-Host "[deps] All checked."
Write-Host "[deps] java=$(if (Test-Cmd 'java') { 'yes' } else { 'NO' }) mvn=$(if (Test-Cmd 'mvn') { 'yes' } else { 'NO' }) node=$(if (Test-Cmd 'node') { 'yes' } else { 'NO' })"
exit 0

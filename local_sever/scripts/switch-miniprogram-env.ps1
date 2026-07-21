# ASCII-only helper: switch miniprogram BASE_URL between local and prod
param(
  [ValidateSet("local", "prod")]
  [string]$Mode = "local",
  [string]$BaseUrl = ""
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$RepoRoot = Split-Path -Parent $Root
$RequestJs = Join-Path $RepoRoot "miniprogram\utils\request.js"
$StateDir = Join-Path $Root "state"
$Backup = Join-Path $StateDir "request.js.bak"
$PrivateConfig = Join-Path $RepoRoot "miniprogram\project.private.config.json"

New-Item -ItemType Directory -Force -Path $StateDir | Out-Null

if (-not (Test-Path $RequestJs)) {
  throw "File not found: $RequestJs"
}

if (-not $BaseUrl) {
  if ($Mode -eq "local") { $BaseUrl = "http://127.0.0.1:83" }
  else { $BaseUrl = "https://huixiaobao-admin.site" }
}

$text = Get-Content -Path $RequestJs -Raw -Encoding UTF8
if ($text -notmatch 'let\s+BASE_URL\s*=\s*["''][^"'']+["'']') {
  throw "BASE_URL assignment not found in request.js"
}

if (-not (Test-Path $Backup)) {
  Copy-Item $RequestJs $Backup
  Write-Host "[local_sever] Backup created: $Backup"
}

$newText = [regex]::Replace(
  $text,
  'let\s+BASE_URL\s*=\s*["''][^"'']+["'']',
  ('let BASE_URL = "' + $BaseUrl + '"')
)
Set-Content -Path $RequestJs -Value $newText -Encoding UTF8 -NoNewline
Write-Host "[local_sever] miniprogram BASE_URL => $BaseUrl ($Mode)"

# Disable domain check for local debugging in WeChat DevTools
if ($Mode -eq "local") {
  $privateObj = @{
    description = "Local debug overrides from local_sever"
    setting = @{
      urlCheck = $false
    }
  }
  $json = $privateObj | ConvertTo-Json -Depth 5
  Set-Content -Path $PrivateConfig -Value $json -Encoding UTF8
  Write-Host "[local_sever] Wrote project.private.config.json (urlCheck=false)"
} else {
  if (Test-Path $PrivateConfig) {
    Remove-Item -Force $PrivateConfig -ErrorAction SilentlyContinue
    Write-Host "[local_sever] Removed project.private.config.json"
  }
}

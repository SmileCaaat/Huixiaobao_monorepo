# ASCII-only PowerShell entry (same behavior as start.bat)
param(
  [switch]$SkipBackend,
  [switch]$SkipInstall,
  [switch]$SkipMiniprogram
)

$argsList = @()
if ($SkipBackend) { $argsList += "-SkipBackend" }
if ($SkipInstall) { $argsList += "-SkipInstall" }
if ($SkipMiniprogram) { $argsList += "-SkipMiniprogram" }

& powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot "run.ps1") @argsList
exit $LASTEXITCODE

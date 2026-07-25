@echo off
setlocal EnableDelayedExpansion
set OUTDIR=
:parse
if "%~1"=="" goto run
if /I "%~1"=="--outdir" (
  set "OUTDIR=%~2"
  shift
  shift
  goto parse
)
shift
goto parse
:run
if "%OUTDIR%"=="" exit /b 1
> "%OUTDIR%\source.pdf" (
echo %%PDF-1.4
echo 1 0 obj^<^</Type/Catalog/Pages 2 0 R^>^>endobj
echo 2 0 obj^<^</Type/Pages/Count 1/Kids[3 0 R]^>^>endobj
echo 3 0 obj^<^</Type/Page/Parent 2 0 R/MediaBox[0 0 612 792]^>^>endobj
echo trailer^<^</Root 1 0 R^>^>
echo %%%%EOF
)
exit /b 0

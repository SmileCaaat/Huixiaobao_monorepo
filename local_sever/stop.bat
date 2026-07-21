@echo off
REM Stop local_sever processes (ASCII only)
setlocal
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\stop.ps1" %*
endlocal

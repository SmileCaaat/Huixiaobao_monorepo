@echo off
REM One-click start for Huixiaobao local_sever (ASCII only)
REM Console stays awake while the dashboard gateway is running.
setlocal
cd /d "%~dp0"
title Huixiaobao local_sever
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\run.ps1" %*
set EXITCODE=%ERRORLEVEL%
if not "%EXITCODE%"=="0" (
  echo [local_sever] exited with code %EXITCODE%
  echo [local_sever] press any key to close...
  pause >nul
)
endlocal & exit /b %EXITCODE%

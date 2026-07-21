@echo off
REM Start MySQL 8.4 if port 3306 is not listening (no Windows service installed yet)
netstat -an | findstr ":3306" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
  echo MySQL already listening on 3306
  exit /b 0
)
start "" /B "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.4\my.ini"
echo Started mysqld
exit /b 0

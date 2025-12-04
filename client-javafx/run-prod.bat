@echo off
REM ========================================
REM Run Client App - Production Mode
REM API Base URL: https://ttapi.manhhao.com
REM ========================================
REM CreatedBy: K24DTCN210-NVMANH
REM ========================================

echo.
echo ========================================
echo   Running MS.TrustTest Client (PROD)
echo ========================================
echo.
echo Profile: prod (Production)
echo API Base URL: https://ttapi.manhhao.com
echo.

cd /d "%~dp0"

echo [1/2] Compiling with PROD profile...
call mvn clean compile -Pprod -q
if errorlevel 1 (
    echo [ERROR] Compile failed!
    pause
    exit /b 1
)

echo [2/2] Starting application...
echo.
call mvn javafx:run -Pprod

pause


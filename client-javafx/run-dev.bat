@echo off
REM ========================================
REM Run Client App - Development Mode
REM API Base URL: http://localhost:8080
REM ========================================
REM CreatedBy: K24DTCN210-NVMANH
REM ========================================

echo.
echo ========================================
echo   Running MS.TrustTest Client (DEV)
echo ========================================
echo.
echo Profile: dev (Development)
echo API Base URL: http://localhost:8080
echo.

cd /d "%~dp0"

echo [1/2] Compiling with DEV profile...
call mvn clean compile -Pdev -q
if errorlevel 1 (
    echo [ERROR] Compile failed!
    pause
    exit /b 1
)

echo [2/2] Starting application...
echo.
call mvn javafx:run -Pdev

pause


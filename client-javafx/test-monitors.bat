@echo off
REM Test script cho Monitor Test Application
REM Author: K24DTCN210-NVMANH (01/12/2025)

echo ========================================
echo Monitor Test Application - Phase 11
echo ========================================
echo.

cd /d "%~dp0"

echo Compiling...
call mvn clean compile -q

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Starting Monitor Test Application...
echo.
echo Note: Backend should be running on http://localhost:8080
echo       Or use test-token if backend is not available
echo.

REM Dùng Maven JavaFX plugin để chạy (tự động handle JavaFX modules)
call mvn javafx:run -Djavafx.mainClass=com.mstrust.client.monitoring.test.MonitorTestApplication

pause


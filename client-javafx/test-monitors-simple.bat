@echo off
REM Simple test script - Dùng Maven exec plugin
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
echo Note: Backend should be running on http://localhost:8080
echo.

REM Dùng Maven exec plugin với JavaFX classpath
call mvn exec:java ^
    -Dexec.mainClass="com.mstrust.client.monitoring.test.MonitorTestApplication" ^
    -Dexec.classpathScope=compile ^
    -Dexec.args=""

pause


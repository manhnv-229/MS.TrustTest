@echo off
REM Run Exam Client Application
REM CreatedBy: K24DTCN210-NVMANH (24/11/2025 09:42)

echo ========================================
echo   MS.TrustTest - Exam Client Launcher
echo ========================================
echo.

cd /d "%~dp0"

echo [1/2] Building project...
call mvn clean compile -q
if errorlevel 1 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo [2/2] Starting Exam Client...
echo.
call mvn exec:java -Dexec.mainClass="com.mstrust.client.exam.ExamClientApplication" -Dexec.classpathScope=compile

pause

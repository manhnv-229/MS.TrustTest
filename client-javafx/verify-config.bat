@echo off
REM ========================================
REM Verify Config in Built JAR
REM ========================================
REM CreatedBy: K24DTCN210-NVMANH
REM ========================================

echo.
echo ========================================
echo   Verifying Config in JAR
echo ========================================
echo.

cd /d "%~dp0"

if not exist "target\exam-client-javafx-1.0.0.jar" (
    echo [ERROR] JAR file not found!
    echo Please build the project first.
    pause
    exit /b 1
)

echo Extracting config.properties from JAR...
jar xf target\exam-client-javafx-1.0.0.jar config.properties 2>nul

if not exist "config.properties" (
    echo [ERROR] Could not extract config.properties from JAR!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Config Content:
echo ========================================
echo.
findstr /C:"api.base.url" config.properties
echo.
echo ========================================
echo.

del config.properties 2>nul

pause


@echo off
REM ========================================
REM Run Client App from JAR
REM ========================================
REM CreatedBy: K24DTCN210-NVMANH
REM ========================================

echo.
echo ========================================
echo   Running MS.TrustTest Client (from JAR)
echo ========================================
echo.

cd /d "%~dp0"

if not exist "target\exam-client-javafx-1.0.0.jar" (
    echo [ERROR] JAR file not found!
    echo.
    echo Please build the project first:
    echo   build-dev.bat    (for development)
    echo   build-prod.bat   (for production)
    echo.
    pause
    exit /b 1
)

echo Starting application from JAR...
echo.

REM Try to find JavaFX SDK in common locations
set JAVAFX_PATH=
if exist "%JAVA_HOME%\..\javafx-sdk-21\lib" (
    set JAVAFX_PATH=%JAVA_HOME%\..\javafx-sdk-21\lib
) else if exist "C:\Program Files\Java\javafx-sdk-21\lib" (
    set JAVAFX_PATH=C:\Program Files\Java\javafx-sdk-21\lib
) else if exist "%USERPROFILE%\javafx-sdk-21\lib" (
    set JAVAFX_PATH=%USERPROFILE%\javafx-sdk-21\lib
)

if "%JAVAFX_PATH%"=="" (
    echo [WARNING] JavaFX SDK not found in common locations.
    echo.
    echo Please set JAVAFX_PATH environment variable or modify this script.
    echo.
    echo Example:
    echo   set JAVAFX_PATH=C:\path\to\javafx-sdk-21\lib
    echo   run-jar.bat
    echo.
    echo Or use JavaFX Maven plugin instead:
    echo   mvn javafx:run
    echo.
    pause
    exit /b 1
)

echo Using JavaFX from: %JAVAFX_PATH%
echo.

java --module-path "%JAVAFX_PATH%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -jar target\exam-client-javafx-1.0.0.jar

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to start application!
    pause
    exit /b 1
)

pause


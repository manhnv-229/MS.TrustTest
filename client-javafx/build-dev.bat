@echo off
REM ========================================
REM Build Client App - Development Mode
REM API Base URL: http://localhost:8080
REM ========================================
REM CreatedBy: K24DTCN210-NVMANH
REM ========================================

echo.
echo ========================================
echo   Building MS.TrustTest Client (DEV)
echo ========================================
echo.
echo Profile: dev (Development)
echo API Base URL: http://localhost:8080
echo.

cd /d "%~dp0"

echo [1/3] Cleaning previous build...
call mvn clean -q
if errorlevel 1 (
    echo [ERROR] Clean failed!
    pause
    exit /b 1
)

echo [2/3] Building with DEV profile...
call mvn package -Pdev -DskipTests
if errorlevel 1 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo [3/3] Build completed successfully!
echo.
echo Output JAR: target\exam-client-javafx-1.0.0.jar
echo.
echo To run the application:
echo   java --module-path "path\to\javafx\lib" --add-modules javafx.controls,javafx.fxml -jar target\exam-client-javafx-1.0.0.jar
echo.
echo Or use JavaFX Maven plugin:
echo   mvn javafx:run
echo.

pause


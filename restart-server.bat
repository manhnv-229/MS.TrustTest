@echo off
echo ===================================
echo Cleaning and Restarting Server
echo ===================================

REM Kill any running Java processes (Spring Boot)
taskkill /F /IM java.exe 2>nul

echo.
echo [1/3] Cleaning build artifacts...
call mvn clean -f backend/pom.xml

echo.
echo [2/3] Compiling project...
call mvn compile -f backend/pom.xml

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Starting server...
echo Server will start at http://localhost:8080
echo Context path: /api
echo Press Ctrl+C to stop
echo.
call mvn spring-boot:run -f backend/pom.xml

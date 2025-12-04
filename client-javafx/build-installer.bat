@echo off
REM ========================================
REM Build Installer exe - Production Mode
REM API Base URL: https://ttapi.manhhao.com
REM ========================================
REM CreatedBy: K24DTCN210-NVMANH
REM ========================================

echo.
echo ========================================
echo   Building MS TrustTest Client Installer
echo ========================================
echo.
echo Profile: prod Production
echo API Base URL: https://ttapi.manhhao.com
echo Output: Windows Installer exe
echo.

cd /d "%~dp0"

REM Check Java version
echo [Step 0/6] Checking Java and jpackage
where jpackage >nul 2>nul
if errorlevel 1 (
    echo [ERROR] jpackage command not found!
    echo.
    echo jpackage is included in JDK 17+ not JRE
    echo Please ensure you have JDK 17+ installed and in PATH
    echo.
    echo To check: java -version
    echo Should show: openjdk version 17 or higher
    echo.
    echo Note: You need JDK Java Development Kit not just JRE
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

REM Simple Java version check
java -version >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Java not found!
    pause
    exit /b 1
)

echo Java and jpackage OK.

echo.
echo [Step 1/6] Cleaning previous build
call mvn clean -q
if errorlevel 1 (
    echo [ERROR] Clean failed!
    pause
    exit /b 1
)

echo [Step 2/6] Building JAR with PROD profile
call mvn package -Pprod -DskipTests
if errorlevel 1 (
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

if not exist target\exam-client-javafx-1.0.0.jar (
    echo [ERROR] JAR file not found after build!
    pause
    exit /b 1
)

echo [Step 3/6] Verifying config in JAR
jar xf target\exam-client-javafx-1.0.0.jar config.properties 2>nul
if exist config.properties (
    findstr /C:"api.base.url" config.properties | findstr /C:"ttapi.manhhao.com" >nul 2>nul
    if errorlevel 1 (
        echo [WARNING] Config may not have production URL
        type config.properties | findstr "api.base.url"
    ) else (
        echo Config verified: Production URL found
    )
    del config.properties 2>nul
)

echo [Step 4/6] Creating installer directory
if not exist target\installer mkdir target\installer

echo [Step 5/6] Creating Windows installer with jpackage
echo.
echo This may take a few minutes
echo.

REM Build jpackage command
REM Added --icon for application icon
REM Changed Main-Class to Launcher to fix JavaFX Runtime missing error in fat-jar
jpackage --input target --name MSTrustTestClient --main-jar exam-client-javafx-1.0.0.jar --main-class com.mstrust.client.Launcher --type exe --dest target\installer --app-version 1.0.0 --vendor MS.TrustTest --description "MS.TrustTest Exam Client Application" --icon "favicon.ico" --win-dir-chooser --win-menu --win-menu-group "MS.TrustTest" --win-shortcut --java-options "--add-reads=com.mstrust.client=ALL-UNNAMED" --java-options "--add-opens=com.mstrust.client/com.mstrust.client.teacher.api=ALL-UNNAMED" --java-options "--add-opens=com.mstrust.client/com.mstrust.client.teacher.dto=ALL-UNNAMED"

if errorlevel 1 (
    echo.
    echo [ERROR] Installer creation failed!
    echo.
    echo Troubleshooting:
    echo 1. Ensure JDK 17+ is installed not just JRE
    echo 2. For exe installer WiX Toolset may be required
    echo    Download from: https://wixtoolset.org/
    echo 3. Try using --type msi instead of --type exe
    echo 4. Check jpackage documentation
    echo.
    pause
    exit /b 1
)

echo.
echo [Step 6/6] Build completed successfully!
echo.
echo ========================================
echo   Installer Location:
echo ========================================
echo.

REM Check for installer file
if exist target\installer\*.exe (
    echo Installer files found:
    dir /b target\installer\*.exe
    echo.
    echo Installer ready for distribution!
) else (
    echo [WARNING] Installer file not found in target\installer\
    echo Please check the build output above for errors.
)
echo.

pause

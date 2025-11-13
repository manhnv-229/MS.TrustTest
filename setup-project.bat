@echo off
REM ===================================================
REM MS.TrustTest Project Setup Script
REM Tạo cấu trúc project và các file cần thiết
REM Author: K24DTCN210-NVMANH (13/11/2025 14:21)
REM ===================================================

echo.
echo ========================================
echo   MS.TrustTest Project Setup
echo ========================================
echo.

REM Tạo cấu trúc thư mục Backend
echo [1/4] Tạo cấu trúc Backend...
mkdir backend\src\main\java\com\mstrust\exam\controller 2>nul
mkdir backend\src\main\java\com\mstrust\exam\service 2>nul
mkdir backend\src\main\java\com\mstrust\exam\repository 2>nul
mkdir backend\src\main\java\com\mstrust\exam\entity 2>nul
mkdir backend\src\main\java\com\mstrust\exam\dto 2>nul
mkdir backend\src\main\java\com\mstrust\exam\security 2>nul
mkdir backend\src\main\java\com\mstrust\exam\config 2>nul
mkdir backend\src\main\java\com\mstrust\exam\exception 2>nul
mkdir backend\src\main\java\com\mstrust\exam\websocket 2>nul
mkdir backend\src\main\resources\db\migration 2>nul
mkdir backend\src\test\java\com\mstrust\exam 2>nul

REM Tạo cấu trúc thư mục Client
echo [2/4] Tạo cấu trúc Client...
mkdir client\src\main\java\com\mstrust\client\controller 2>nul
mkdir client\src\main\java\com\mstrust\client\service 2>nul
mkdir client\src\main\java\com\mstrust\client\model 2>nul
mkdir client\src\main\java\com\mstrust\client\monitoring 2>nul
mkdir client\src\main\java\com\mstrust\client\util 2>nul
mkdir client\src\main\resources\fxml 2>nul
mkdir client\src\main\resources\css 2>nul
mkdir client\src\main\resources\images 2>nul

REM Tạo thư mục database
echo [3/4] Tạo thư mục Database...
mkdir database 2>nul

REM Tạo .gitignore
echo [4/4] Tạo .gitignore...
(
echo # Maven
echo target/
echo pom.xml.tag
echo pom.xml.releaseBackup
echo pom.xml.versionsBackup
echo.
echo # IDE
echo .idea/
echo *.iml
echo .vscode/
echo .settings/
echo .project
echo .classpath
echo.
echo # OS
echo .DS_Store
echo Thumbs.db
echo.
echo # Logs
echo *.log
echo.
echo # Application
echo application-local.yml
echo application-dev.yml
) > .gitignore

echo.
echo ========================================
echo   Setup hoàn tất!
echo ========================================
echo.
echo Cấu trúc project đã được tạo thành công.
echo.
echo Các bước tiếp theo:
echo 1. Chạy: mvn clean install
echo 2. Setup MySQL database
echo 3. Cấu hình application.yml
echo.
pause

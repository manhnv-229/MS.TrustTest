@echo off
REM ========================================
REM Build Portable MS.TrustTest Client App
REM Không cần cài đặt Java, chạy trực tiếp
REM API Mode: Production (ttapi.manhhao.com)
REM ========================================
REM CreatedBy: K24DTCN210-NVMANH (04/12/2025 15:21)
REM ========================================

setlocal enabledelayedexpansion

echo. 
echo ========================================
echo   Building Portable MS.TrustTest Client
echo ========================================
echo. 
echo Mode: Production (Online API)
echo API: https://ttapi.manhhao.com
echo Output: build/portable/
echo. 

REM Kiểm tra Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java không được tìm thấy trong PATH
    echo Vui lòng cài đặt Java JDK 17+ và thêm vào PATH
    pause
    exit /b 1
)

REM Kiểm tra JavaFX
where jlink >nul 2>&1
if errorlevel 1 (
    echo [ERROR] jlink không được tìm thấy
    echo Vui lòng cài đặt OpenJDK với JavaFX hoặc Oracle JDK
    pause
    exit /b 1
)

cd /d "%~dp0"

REM Tạo thư mục build nếu chưa có
if not exist "build" mkdir build
if not exist "build\portable" mkdir build\portable

echo [1/6] Cleaning previous builds...
if exist "build\portable\*" (
    rd /s /q "build\portable" 2>nul
    mkdir "build\portable"
)

echo [2/6] Building client JAR với production profile...
cd client-javafx
call mvn clean package -Pprod -DskipTests -q
if errorlevel 1 (
    echo [ERROR] Maven build failed!  
    cd .. 
    pause
    exit /b 1
)

REM Kiểm tra JAR file được tạo
if not exist "target\exam-client-javafx-1.0.0.jar" (
    echo [ERROR] JAR file không được tạo
    cd ..
    pause
    exit /b 1
)

echo [3/6] Copying application JAR...
copy "target\exam-client-javafx-1.0.0.jar" "..\build\portable\ms-trusttest-client.jar" >nul
if errorlevel 1 (
    echo [ERROR] Failed to copy JAR file
    cd ..
    pause
    exit /b 1
)

cd ..  

echo [4/6] Creating custom JRE with JavaFX...
REM Tạo custom JRE với các modules cần thiết
jlink --module-path "%JAVA_HOME%\jmods" ^
      --add-modules java.base,java.desktop,java.logging,java.naming,java.net.http,java.prefs,java.sql,java.xml,jdk.crypto.ec,jdk.unsupported ^
      --output "build\portable\jre" ^
      --compress=2 ^
      --no-header-files ^
      --no-man-pages

if errorlevel 1 (
    echo [ERROR] Failed to create custom JRE
    echo Trying with minimal modules...
    jlink --module-path "%JAVA_HOME%\jmods" ^
          --add-modules java.base,java.desktop,java.logging ^
          --output "build\portable\jre" ^
          --compress=2 ^
          --no-header-files ^
          --no-man-pages
    
    if errorlevel 1 (
        echo [ERROR] Cannot create custom JRE
        pause
        exit /b 1
    )
)

echo [5/6] Creating launcher scripts... 

REM Tạo Windows launcher
echo @echo off > "build\portable\MS-TrustTest-Client.bat"
echo cd /d "%%~dp0" >> "build\portable\MS-TrustTest-Client.bat"
echo echo Starting MS.TrustTest Client... >> "build\portable\MS-TrustTest-Client.bat"
echo .\jre\bin\java -jar ms-trusttest-client.jar >> "build\portable\MS-TrustTest-Client.bat"
echo pause >> "build\portable\MS-TrustTest-Client.bat"

REM Tạo launcher ẩn console (chạy im lặng)
echo @echo off > "build\portable\MS-TrustTest-Client-Silent.bat"
echo cd /d "%%~dp0" >> "build\portable\MS-TrustTest-Client-Silent.bat"
echo start /min .\jre\bin\javaw -jar ms-trusttest-client.jar >> "build\portable\MS-TrustTest-Client-Silent.bat"

echo [6/6] Creating documentation...
echo # MS.TrustTest Portable Client > "build\portable\README.md"
echo.  >> "build\portable\README.md"
echo ## Hướng dẫn sử dụng >> "build\portable\README.md"
echo. >> "build\portable\README.md"
echo 1. **Chạy ứng dụng với console:** >> "build\portable\README.md"
echo    - Double-click `MS-TrustTest-Client.bat` >> "build\portable\README.md"
echo    - Hoặc từ Command Prompt: `MS-TrustTest-Client.bat` >> "build\portable\README.md"
echo. >> "build\portable\README.md"
echo 2. **Chạy ứng dụng im lặng (không hiện console):** >> "build\portable\README.md"
echo    - Double-click `MS-TrustTest-Client-Silent.bat` >> "build\portable\README.md"
echo. >> "build\portable\README.md"
echo ## Thông tin kỹ thuật >> "build\portable\README.md"
echo. >> "build\portable\README.md"
echo - **Phiên bản:** 1.0.0 >> "build\portable\README.md"
echo - **API Server:** https://ttapi.manhhao.com >> "build\portable\README.md"
echo - **Java Runtime:** Custom JRE được nhúng >> "build\portable\README.md"
echo - **Yêu cầu:** Windows 10+ (64-bit) >> "build\portable\README.md"
echo - **Kích thước:** ~50-80MB >> "build\portable\README.md"
echo. >> "build\portable\README.md"
echo ## Chú ý >> "build\portable\README.md"
echo. >> "build\portable\README.md"
echo - Không cần cài đặt Java >> "build\portable\README.md"
echo - Có thể copy toàn bộ thư mục để sử dụng trên máy khác >> "build\portable\README.md"
echo - Ứng dụng kết nối với server online >> "build\portable\README.md"
echo. >> "build\portable\README.md"
echo ------- >> "build\portable\README.md"
echo *Built by: K24DTCN210-NVMANH (04/12/2025)* >> "build\portable\README.md"

echo. 
echo ========================================
echo   🎉 Build Portable Completed!  
echo ========================================
echo. 
echo Output location: build\portable\
echo. 
echo Files created:
echo   - MS-TrustTest-Client.bat         (Run with console)
echo   - MS-TrustTest-Client-Silent.bat  (Run silently)
echo   - ms-trusttest-client.jar         (Application)
echo   - jre\                            (Java Runtime)
echo   - README.md                       (Documentation)
echo.
echo Total size: ~50-80MB
echo. 
echo To test: 
echo   cd build\portable
echo   MS-TrustTest-Client.bat
echo. 

pause

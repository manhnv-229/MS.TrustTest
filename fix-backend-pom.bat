@echo off
echo Dang copy noi dung tu docs/backend-pom-FULL.xml sang backend/pom.xml...
copy /Y docs\backend-pom-FULL.xml backend\pom.xml
if %errorlevel% == 0 (
    echo.
    echo [THANH CONG] Da tao file backend/pom.xml hoan chinh!
    echo.
    echo Ban co the chay lenh: mvn clean install
) else (
    echo.
    echo [LOI] Khong the copy file!
)
pause

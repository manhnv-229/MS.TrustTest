# Hướng Dẫn Build Installer (.exe) - MS.TrustTest Client

## 📋 Tổng Quan

Hướng dẫn này sẽ giúp bạn tạo bộ cài đặt Windows (.exe) cho MS.TrustTest Client với base URL Production (`https://ttapi.manhhao.com`).

## ✅ Yêu Cầu

### 1. JDK 17+ (Bắt buộc)
- **Không phải JRE**, phải là **JDK** (Java Development Kit)
- jpackage tool chỉ có trong JDK, không có trong JRE
- Download từ: https://adoptium.net/ hoặc https://www.oracle.com/java/technologies/downloads/

**Kiểm tra:**
```bash
java -version
jpackage --version
```

### 2. WiX Toolset (Khuyến nghị cho .exe)
- Cần thiết để tạo installer định dạng `.exe`
- Download từ: https://wixtoolset.org/
- Hoặc có thể dùng `--type msi` (không cần WiX)

### 3. Maven (Đã có sẵn)
- Đã được sử dụng trong project

## 🚀 Cách Build Installer

### Option 1: Sử dụng Script (Khuyến nghị)

```bash
cd client-javafx
build-installer.bat
```

Script này sẽ:
1. Kiểm tra Java version
2. Build JAR với profile `prod` (base URL: `https://ttapi.manhhao.com`)
3. Verify config trong JAR
4. Tạo installer .exe bằng jpackage
5. Output: `target\installer\MS.TrustTest Client-1.0.0.exe`

### Option 2: Build Thủ Công

#### Bước 1: Build JAR với Production Profile
```bash
cd client-javafx
mvn clean package -Pprod -DskipTests
```

#### Bước 2: Tạo Installer với jpackage
```bash
jpackage ^
    --input target ^
    --name "MS.TrustTest Client" ^
    --main-jar exam-client-javafx-1.0.0.jar ^
    --main-class com.mstrust.client.exam.ExamClientApplication ^
    --type exe ^
    --dest target\installer ^
    --app-version 1.0.0 ^
    --vendor "MS.TrustTest" ^
    --description "MS.TrustTest Exam Client Application" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-menu-group "MS.TrustTest" ^
    --win-shortcut ^
    --java-options "--add-reads=com.mstrust.client=ALL-UNNAMED" ^
    --java-options "--add-opens=com.mstrust.client/com.mstrust.client.teacher.api=ALL-UNNAMED" ^
    --java-options "--add-opens=com.mstrust.client/com.mstrust.client.teacher.dto=ALL-UNNAMED"
```

## 📁 Output

Sau khi build thành công:
- **Installer**: `target\installer\MS.TrustTest Client-1.0.0.exe`
- **JAR**: `target\exam-client-javafx-1.0.0.jar`

## 🔍 Verify Config

Trước khi tạo installer, có thể verify config trong JAR:

```bash
cd client-javafx
verify-config.bat
```

Hoặc thủ công:
```bash
jar xf target\exam-client-javafx-1.0.0.jar config.properties
type config.properties | findstr api.base.url
del config.properties
```

**Kết quả mong đợi:**
```properties
api.base.url=https://ttapi.manhhao.com
```

## 🐛 Troubleshooting

### Lỗi: "jpackage command not found"
**Nguyên nhân:** Chưa cài JDK hoặc JDK không có trong PATH

**Giải pháp:**
1. Cài đặt JDK 17+ từ https://adoptium.net/
2. Thêm JDK vào PATH:
   - Windows: Thêm `C:\Program Files\Java\jdk-17\bin` vào System PATH
   - Hoặc set JAVA_HOME và thêm `%JAVA_HOME%\bin` vào PATH

### Lỗi: "WiX Toolset not found" (khi dùng --type exe)
**Nguyên nhân:** Chưa cài WiX Toolset

**Giải pháp:**
1. Download và cài WiX từ https://wixtoolset.org/
2. Hoặc dùng `--type msi` thay vì `--type exe` (không cần WiX)

### Lỗi: "Config vẫn là localhost:8080"
**Nguyên nhân:** Build không dùng profile `prod`

**Giải pháp:**
- Đảm bảo build với `-Pprod`: `mvn clean package -Pprod`
- Verify config trước khi tạo installer

### Lỗi: "Main class not found"
**Nguyên nhân:** Main class path sai

**Giải pháp:**
- Main class đúng: `com.mstrust.client.exam.ExamClientApplication`
- Kiểm tra JAR có chứa class này: `jar tf target\exam-client-javafx-1.0.0.jar | findstr ExamClientApplication`

### Installer quá lớn
**Nguyên nhân:** jpackage tạo runtime image bao gồm JRE

**Giải pháp:**
- Đây là bình thường, installer sẽ bao gồm JRE để app chạy độc lập
- Có thể giảm kích thước bằng cách dùng `--strip-native-commands` (không khuyến nghị)

## 📝 Tùy Chỉnh Installer

### Thay đổi Icon
Thêm option:
```bash
--icon path\to\icon.ico
```

### Thay đổi Installer Type
- `.exe`: `--type exe` (cần WiX)
- `.msi`: `--type msi` (không cần WiX)
- `.app-image`: `--type app-image` (chỉ thư mục, không phải installer)

### Thêm Java Options
Thêm vào command:
```bash
--java-options "-Xmx2G"
--java-options "-Dsome.property=value"
```

### Tùy chỉnh Installer UI
```bash
--win-dir-chooser        # Cho phép chọn thư mục cài đặt
--win-menu              # Tạo Start Menu shortcut
--win-menu-group "Group" # Nhóm trong Start Menu
--win-shortcut          # Tạo Desktop shortcut
```

## 🎯 Best Practices

1. **Luôn verify config** trước khi tạo installer
2. **Test installer** trên máy sạch (không có Java) để đảm bảo app chạy độc lập
3. **Kiểm tra kích thước** installer (thường 100-200MB do bao gồm JRE)
4. **Đặt tên rõ ràng** cho installer file
5. **Versioning**: Cập nhật version trong `pom.xml` khi release mới

## 📦 Distribution

Sau khi tạo installer:
1. Test installer trên máy sạch
2. Verify app kết nối đúng API (`https://ttapi.manhhao.com`)
3. Đóng gói và phân phối installer

## 🔗 Related Files

- `build-installer.bat`: Script build installer
- `build-prod.bat`: Script build JAR production
- `verify-config.bat`: Script verify config
- `pom.xml`: Cấu hình Maven và profiles
- `src/main/resources/config.properties`: File config template

---
**CreatedBy**: K24DTCN210-NVMANH  
**Last Updated**: 02/12/2025


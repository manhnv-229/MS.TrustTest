# Hướng Dẫn Build Client App - MS.TrustTest

## 📋 Tổng Quan

Client app hỗ trợ 2 chế độ build:
- **Development (dev)**: Sử dụng `http://localhost:8080`
- **Production (prod)**: Sử dụng `https://ttapi.manhhao.com`

## 🚀 Cách Build

### Option 1: Sử dụng Script (Khuyến nghị)

#### Build Development
```bash
cd client-javafx
build-dev.bat
```

#### Build Production
```bash
cd client-javafx
build-prod.bat
```

### Option 2: Sử dụng Maven Command

#### Build Development (mặc định)
```bash
cd client-javafx
mvn clean package
```
hoặc rõ ràng hơn:
```bash
mvn clean package -Pdev
```

#### Build Production
```bash
cd client-javafx
mvn clean package -Pprod
```

## ✅ Verify Config Sau Khi Build

Để kiểm tra config đã được thay thế đúng chưa:

```bash
cd client-javafx
verify-config.bat
```

Hoặc thủ công:
```bash
# Extract config từ JAR
jar xf target\exam-client-javafx-1.0.0.jar config.properties

# Xem nội dung
type config.properties | findstr api.base.url

# Cleanup
del config.properties
```

## 🧪 Test Application

### ⚠️ QUAN TRỌNG: Phải chỉ định Profile khi chạy!

Khi chạy `mvn javafx:run` **KHÔNG chỉ định profile**, Maven sẽ dùng profile mặc định (`dev`), dù đã build với profile `prod` trước đó.

### Cách 1: Sử dụng Script (Khuyến nghị - Dễ nhất)

**Development:**
```bash
cd client-javafx
run-dev.bat
```

**Production:**
```bash
cd client-javafx
run-prod.bat
```

### Cách 2: Chạy trực tiếp với JavaFX Maven Plugin

**⚠️ LƯU Ý:** Phải compile trước với profile đúng, sau đó mới chạy với profile đúng!

**Development:**
```bash
cd client-javafx
mvn clean compile -Pdev
mvn javafx:run -Pdev
```

**Production:**
```bash
cd client-javafx
mvn clean compile -Pprod
mvn javafx:run -Pprod
```

### Cách 3: Chạy từ JAR đã build

**Yêu cầu:**
- Java 17+
- JavaFX SDK 21 (nếu chạy standalone JAR)

**Command:**
```bash
java --module-path "path\to\javafx-sdk-21\lib" ^
     --add-modules javafx.controls,javafx.fxml ^
     -jar target\exam-client-javafx-1.0.0.jar
```

**Lưu ý:** Thay `path\to\javafx-sdk-21\lib` bằng đường dẫn thực tế đến JavaFX SDK của bạn.

### Cách 4: Sử dụng Script có sẵn (không chỉ định profile - sẽ dùng dev)

```bash
cd client-javafx
run-exam-client.bat
```

## 📁 Output Files

Sau khi build thành công:
- **JAR file**: `target\exam-client-javafx-1.0.0.jar`
- **Original JAR**: `target\original-exam-client-javafx-1.0.0.jar` (không có dependencies)

## 🔍 Kiểm Tra Config Trong JAR

### Development Build
Sau khi build với profile `dev`, file `config.properties` trong JAR sẽ có:
```properties
api.base.url=http://localhost:8080
```

### Production Build
Sau khi build với profile `prod`, file `config.properties` trong JAR sẽ có:
```properties
api.base.url=https://ttapi.manhhao.com
```

## 🐛 Troubleshooting

### Lỗi: "Could not find or load main class"
- Đảm bảo đã build thành công với `mvn clean package`
- Kiểm tra JAR có tồn tại trong `target\` folder

### Lỗi: "JavaFX runtime components are missing"
- Cần JavaFX SDK để chạy standalone JAR
- Hoặc sử dụng `mvn javafx:run` (không cần JavaFX SDK riêng)

### Config không được thay thế / Vẫn thấy localhost:8080 khi chạy production
- **Nguyên nhân phổ biến**: Chạy `mvn javafx:run` mà không chỉ định profile `-Pprod`
- **Giải pháp**: 
  - Sử dụng script: `run-prod.bat` (khuyến nghị)
  - Hoặc chạy: `mvn clean compile -Pprod` rồi `mvn javafx:run -Pprod`
- Kiểm tra resource filtering đã được bật trong `pom.xml`
- Đảm bảo đã chỉ định đúng profile: `-Pdev` hoặc `-Pprod` khi cả compile và run
- Xóa `target\` folder và build lại: `mvn clean package -Pprod`

### Build chậm
- Lần đầu build sẽ download dependencies (chậm)
- Các lần sau sẽ nhanh hơn nhờ Maven cache

## 📝 Notes

1. **Profile mặc định**: Nếu không chỉ định profile, Maven sẽ dùng profile `dev` (activeByDefault)
2. **Resource Filtering**: Maven sẽ tự động thay thế `${api.base.url}` trong `config.properties` khi build
3. **JAR Location**: JAR được tạo trong `target\exam-client-javafx-1.0.0.jar`
4. **Dependencies**: JAR đã bao gồm tất cả dependencies (fat JAR) nhờ maven-shade-plugin

## 📦 Build Installer (.exe)

Để tạo bộ cài đặt Windows (.exe) cho production:

```bash
cd client-javafx
build-installer.bat
```

**Yêu cầu:**
- JDK 17+ (không phải JRE)
- WiX Toolset (cho .exe) hoặc dùng `--type msi`

**Output:** `target\installer\MS.TrustTest Client-1.0.0.exe`

**Chi tiết:** Xem `INSTALLER_GUIDE.md`

## 🔗 Related Files

- `pom.xml`: Cấu hình Maven và profiles
- `src/main/resources/config.properties`: File config template với placeholder
- `build-dev.bat`: Script build development
- `build-prod.bat`: Script build production
- `build-installer.bat`: Script build installer .exe (production)
- `run-dev.bat`: Script chạy app development (compile + run với profile dev)
- `run-prod.bat`: Script chạy app production (compile + run với profile prod)
- `verify-config.bat`: Script verify config trong JAR
- `INSTALLER_GUIDE.md`: Hướng dẫn chi tiết về build installer

---
**CreatedBy**: K24DTCN210-NVMANH  
**Last Updated**: 02/12/2025


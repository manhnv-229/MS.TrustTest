# Hướng dẫn cài đặt Apache Maven trên Windows 10

## Bước 1: Kiểm tra Java đã cài chưa

Mở Command Prompt (cmd) và chạy:
```cmd
java -version
```

Nếu chưa có Java hoặc version < 17, tải Java 17 tại: https://adoptium.net/

## Bước 2: Download Maven

1. Truy cập: https://maven.apache.org/download.cgi
2. Tải file **apache-maven-3.9.x-bin.zip** (Binary zip archive)
3. Ví dụ: `apache-maven-3.9.5-bin.zip`

## Bước 3: Giải nén Maven

1. Giải nén file zip vừa tải về
2. Đặt vào thư mục: `C:\Program Files\Apache\maven`
3. Đường dẫn đầy đủ sẽ là: `C:\Program Files\Apache\maven\apache-maven-3.9.5`

## Bước 4: Thiết lập biến môi trường

### 4.1. Thiết lập MAVEN_HOME

1. Nhấn **Windows + Pause** (hoặc chuột phải This PC → Properties)
2. Click **Advanced system settings**
3. Click **Environment Variables**
4. Trong **System variables**, click **New**
5. Điền:
   - **Variable name:** `MAVEN_HOME`
   - **Variable value:** `C:\Program Files\Apache\maven\apache-maven-3.9.5`
6. Click **OK**

### 4.2. Thêm Maven vào PATH

1. Vẫn trong **Environment Variables**
2. Tìm biến **Path** trong **System variables**
3. Click **Edit**
4. Click **New**
5. Thêm: `%MAVEN_HOME%\bin`
6. Click **OK** để đóng tất cả cửa sổ

## Bước 5: Kiểm tra cài đặt

1. **Đóng tất cả cửa sổ Command Prompt cũ**
2. Mở **Command Prompt mới**
3. Chạy lệnh:

```cmd
mvn -version
```

Nếu thành công, sẽ hiển thị:
```
Apache Maven 3.9.5 (...)
Maven home: C:\Program Files\Apache\maven\apache-maven-3.9.5
Java version: 17.0.x, vendor: ...
Java home: C:\Program Files\Eclipse Adoptium\jdk-17...
Default locale: en_US, platform encoding: UTF-8
OS name: "windows 10", version: "10.0", arch: "amd64"
```

## Bước 6: (Tùy chọn) Cấu hình Maven settings

Tạo file `settings.xml` tại `C:\Users\nvmanh\.m2\settings.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              http://maven.apache.org/xsd/settings-1.0.0.xsd">
    
    <!-- Local repository path -->
    <localRepository>C:/Users/nvmanh/.m2/repository</localRepository>
    
    <!-- Use mirrors for faster download (optional) -->
    <mirrors>
        <mirror>
            <id>aliyun</id>
            <mirrorOf>central</mirrorOf>
            <name>Aliyun Maven Mirror</name>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
</settings>
```

## Bước 7: Compile project MS.TrustTest

Sau khi cài Maven xong, compile project:

```cmd
cd d:\PRIVATE\MS.TrustTest
mvn clean install
```

Lệnh này sẽ:
- Download tất cả dependencies (~200MB lần đầu)
- Compile Java code
- Tạo file JAR

**Lưu ý:** Lần đầu sẽ mất 5-10 phút để download dependencies.

## Bước 8: Run Spring Boot application

```cmd
cd backend
mvn spring-boot:run
```

Application sẽ chạy tại: http://localhost:8080

## Troubleshooting

### Lỗi: 'mvn' is not recognized

- Kiểm tra lại biến PATH đã có `%MAVEN_HOME%\bin` chưa
- **Nhớ đóng và mở lại Command Prompt**
- Hoặc restart máy

### Lỗi: JAVA_HOME not found

Thêm biến môi trường JAVA_HOME:
- Variable name: `JAVA_HOME`
- Variable value: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x` (đường dẫn Java của cụ)

### Lỗi: Cannot download dependencies

- Kiểm tra internet connection
- Hoặc dùng mirror Aliyun (đã config ở Bước 6)

## Alternative: Sử dụng IDE có sẵn Maven

Nếu cụ Mạnh dùng IntelliJ IDEA hoặc Eclipse, không cần cài Maven riêng:

### IntelliJ IDEA
- File → Settings → Build, Execution, Deployment → Build Tools → Maven
- IDE đã có Maven bundled sẵn

### Eclipse
- Window → Preferences → Maven
- Eclipse cũng có Maven (m2e) tích hợp sẵn

**Khuyến nghị:** Nếu dùng IDE, không cần cài Maven command line.

---

**Created:** 13/11/2025 15:10  
**Author:** K24DTCN210-NVMANH

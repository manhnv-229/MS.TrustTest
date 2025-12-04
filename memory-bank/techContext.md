# Tech Context: MS.TrustTest

## Technology Stack

### Backend
- **Java**: 17 (LTS)
- **Framework**: Spring Boot 3.2.x
- **Database**: MySQL 8.0.x
- **ORM**: Spring Data JPA / Hibernate
- **Migration**: Flyway
- **Security**: Spring Security + JWT (jjwt 0.12.x)
- **WebSocket**: Spring WebSocket + STOMP
- **Build Tool**: Maven 3.9.x

### Client
- **UI Framework**: JavaFX 21
- **Layout**: FXML + CSS
- **Icons**: Ikonli 12.3.1 (FontAwesome 5)
- **Icon Factory**: IconFactory utility class
- **Monitoring**: JNA 5.x, Robot API, java.awt.Toolkit
- **HTTP Client**: Spring WebClient
- **WebSocket**: STOMP Client
- **Build Tool**: Maven 3.9.x

### Database
- **RDBMS**: MySQL 8.0.x
- **Engine**: InnoDB
- **Charset**: UTF8MB4
- **Collation**: utf8mb4_unicode_ci

## Configuration Files

### Backend application.yml

```yaml
spring:
  application:
    name: ms-trust-exam-backend
  
  datasource:
    url: jdbc:mysql://104.199.231.104:3306/MS.TrustTest?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true
    username: nvmanh
    password: '!M@nh1989'
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        
  flyway:
    enabled: false
    baseline-on-migrate: true
    locations: classpath:db/migration
    
server:
  port: 8080
  servlet:
    context-path: /api
    
jwt:
  secret: ms-trust-exam-secret-key-change-this-in-production-at-least-32-characters
  expiration: 86400000

logging:
  level:
    com.mstrust.exam: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

**Important Notes:**
- **Database Host**: Remote server tại 104.199.231.104:3306
- **Database Name**: MS.TrustTest (không phải ms_trust_exam)
- **Flyway**: Disabled vì database đã có sẵn tables
- **JPA ddl-auto**: validate - không tạo/sửa tables tự động
- **Timezone**: Asia/Ho_Chi_Minh
- **Debug logging**: Enabled cho development

### Client application.properties

**Location**: `client-javafx/src/main/resources/config.properties`

**Template (với Maven resource filtering):**
```properties
# Backend API Configuration
# Sử dụng Maven resource filtering - giá trị sẽ được thay thế khi build
# Development: mvn clean package (mặc định)
# Production: mvn clean package -Pprod
api.base.url=${api.base.url}
api.timeout.seconds=30

# Monitoring Configuration
monitoring.screenshot.interval.seconds=30
monitoring.activity.batch.interval.seconds=60
monitoring.screenshot.max.width=1920
monitoring.screenshot.max.height=1080
monitoring.screenshot.jpeg.quality=0.7

# Alert Thresholds
alert.window.switch.threshold=10
alert.window.switch.timeframe.minutes=5
alert.clipboard.threshold=20
alert.clipboard.timeframe.minutes=10

# Blacklisted Processes
blacklist.processes=teamviewer,anydesk,chrome,firefox,safari,edge,discord,telegram,skype,zoom,slack

# Network Queue Configuration
queue.max.size=1000
queue.retry.max.attempts=3
queue.retry.delay.seconds=5

# Logging
logging.level=INFO
logging.file.enabled=true
logging.file.path=./logs/client.log
```

**Maven Resource Filtering:**
- Enabled trong `pom.xml`: `<filtering>true</filtering>`
- Placeholder `${api.base.url}` được thay thế khi build
- Development build: `http://localhost:8080`
- Production build: `https://ttapi.manhhao.com`

## Development Commands

### Backend

```bash
# Build project
mvn clean install

# Run backend
mvn spring-boot:run

# Run with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Package JAR
mvn package
```

### Client

**Build Commands:**
```bash
# Development build (mặc định)
cd client-javafx
mvn clean package -Pdev
# hoặc
mvn clean package  # (dev là mặc định)

# Production build
mvn clean package -Pprod

# Run với JavaFX Maven plugin
mvn javafx:run -Pdev   # Development
mvn javafx:run -Pprod  # Production

# Hoặc dùng scripts
build-dev.bat      # Build development
build-prod.bat     # Build production
run-dev.bat        # Run development
run-prod.bat       # Run production
```

**Installer Build:**
```bash
# Build installer .exe (Production)
cd client-javafx
build-installer.bat      # Windows batch (cần JDK 17+)
# hoặc
.\build-installer.ps1    # PowerShell (auto-detect JDK)

# Yêu cầu:
# - JDK 17+ (không phải JRE)
# - WiX Toolset (cho .exe) hoặc dùng --type msi
# - Output: target\installer\MSTrustTestClient-1.0.0.exe
```

**Verify Config:**
```bash
# Verify config trong JAR sau khi build
verify-config.bat

# Hoặc thủ công
jar xf target\exam-client-javafx-1.0.0.jar config.properties
type config.properties | findstr api.base.url
del config.properties
```

## Database Setup

### Remote Database (Current Configuration)
- **Host**: 104.199.231.104:3306
- **Database**: MS.TrustTest
- **Username**: nvmanh
- **Password**: !M@nh1989
- **Character Set**: utf8mb4
- **Collation**: utf8mb4_unicode_ci
- **Status**: ✅ Active and connected

### Local Database Setup (For reference)
```sql
-- Create database
CREATE DATABASE MS.TrustTest CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'nvmanh'@'localhost' IDENTIFIED BY '!M@nh1989';
GRANT ALL PRIVILEGES ON MS.TrustTest.* TO 'nvmanh'@'localhost';
FLUSH PRIVILEGES;
```

## Technical Constraints

1. **Java Version**: Minimum Java 17
2. **MySQL Version**: Minimum 8.0
3. **Memory**: 
   - Backend: Min 512MB, Recommended 2GB
   - Client: Min 256MB, Recommended 1GB
4. **Disk Space**: ~500MB for application + database
5. **Network**: HTTP/HTTPS, WebSocket support required

## Cross-Platform Considerations

### Windows
- Native installer: .exe, .msi
- JNA works natively
- Process monitoring fully supported

### macOS
- Native installer: .dmg, .pkg
- Requires permission for Screen Recording
- Process monitoring requires accessibility permissions

### Linux
- Native installer: .deb, .rpm, AppImage
- JNA requires libjna-java
- Process monitoring varies by DE (Gnome/KDE)

## Security Configurations

### Password Hashing
- Algorithm: BCrypt
- Cost Factor: 12
- Salt: Auto-generated per password

### JWT Tokens
- Algorithm: HS512
- Expiration: 24 hours
- Refresh: Not implemented in v1.0

### HTTPS (Production)
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

## Performance Targets

- API Response Time: < 500ms (p95)
- WebSocket Latency: < 100ms
- Screenshot Upload: < 2s per image
- Database Query: < 100ms (p95)
- Concurrent Users: 500+

## Environment Configuration

### Development Environment (Current)
- **Java Version**: 25.0.1
- **Maven Version**: 3.9.11
- **Spring Boot Version**: 3.5.7
- **Database**: Remote MySQL 8.0 (104.199.231.104)
- **Application Port**: 8080
- **Context Path**: /api
- **Base URL**: http://localhost:8080/api

### Application Status
- ✅ Backend running successfully on port 8080
- ✅ Connected to remote database MS.TrustTest
- ✅ Flyway disabled (tables already exist)
- ✅ Spring Security configured correctly (all bugs fixed)
- ✅ Authentication API fully functional

### Recent Configuration Changes

**15/11/2025 - MCP Server Setup:**
1. Created MCP Server mysql-trusttest
   - Location: `G:\WORKS\GitHub\MS.TrustTest\mysql-trusttest\`
   - Language: TypeScript + Node.js
   - Dependencies: @modelcontextprotocol/sdk@1.0.4, mysql2@3.11.5
   - Build tool: TypeScript compiler

2. MCP Server Configuration:
   - Database host: 104.199.231.104:3306
   - Database name: MS.TrustTest
   - Connection hardcoded in source
   - Tools: execute_query, get_table_info, describe_database
   - Resources: Tables, views, procedures listing

3. Cline Integration:
   - Config file: `cline_mcp_settings.json`
   - Server path: `G:\WORKS\GitHub\MS.TrustTest\mysql-trusttest\build\index.js`
   - Auto-approve: describe_database, get_table_info
   - Status: Active and functional

**14/11/2025 - Authentication Bug Fixes:**
1. Updated datasource URL to remote server (104.199.231.104)
2. Changed database name from ms_trust_exam to MS.TrustTest
3. Disabled Flyway migration
4. Enabled debug logging for Spring Security
5. Updated timezone to Asia/Ho_Chi_Minh
6. Fixed URL mapping (removed duplicate /api prefix)
7. Added AuditingConfig for JPA Auditing
8. Optimized transaction handling (updateLastLogin query)
9. Fixed database constraints (role_name, password_hash)
10. Added TestController for debugging (temporary)

---

## Known Issues & Solutions

### Authentication Issues (ALL RESOLVED - 14/11/2025)
1. ✅ **Duplicate /api prefix**: Removed from controllers
2. ✅ **SQL query syntax**: Added parentheses
3. ✅ **Username mismatch**: Use actual input username
4. ✅ **Duplicate ROLE_ prefix**: Removed from code
5. ✅ **Empty role_name**: Updated in database
6. ✅ **Wrong password hash**: Generated correct BCrypt hash
7. ✅ **Missing AuditorAware**: Created AuditingConfig
8. ✅ **Transaction conflict**: Use @Modifying @Query instead of save()

### Debugging Tools
- **TestController**: Temporary endpoints for testing
  - GET `/api/test/hash-password?password=xxx` - Generate BCrypt hash
  - GET `/api/test/verify-password?password=xxx&hash=xxx` - Verify hash
  - **Note**: DELETE before production deployment

### Best Practices Learned
1. **Spring Security URL Mapping**:
   - Context-path (`/api`) is added automatically
   - Don't duplicate prefix in `@RequestMapping`
   - Use `permitAll()` for public endpoints

2. **JPA Auditing with Spring Security**:
   - Always provide `AuditorAware` bean
   - Don't use `save()` during authentication flow
   - Use `@Modifying @Query` for direct updates

3. **BCrypt Password Hashing**:
   - Generate hash using application's `PasswordEncoder`
   - Cost factor 12 is recommended
   - Never copy hash from external sources

4. **Database Integrity**:
   - Verify role names match code expectations
   - Check NOT NULL constraints before inserting
   - Use MCP Server for direct database operations

---

## Icon System (Ikonli)

### Library
- **Ikonli Version**: 12.3.1
- **Icon Set**: FontAwesome 5 (Solid)
- **Package**: `org.kordamp.ikonli.fontawesome5.FontAwesomeSolid`
- **Prefix**: `fas-`

### IconFactory Utility
**Location**: `client-javafx/src/main/java/com/mstrust/client/exam/util/IconFactory.java`

**Purpose**: Centralized icon creation với consistent sizing và coloring

**Common Methods**:
```java
// Standard icons
IconFactory.createViewIcon()           // Eye icon (18px, primary)
IconFactory.createEditIconForButton()  // Pencil icon (18px, primary)
IconFactory.createPublishIcon()        // Bullhorn icon (18px, success)
IconFactory.createLockIconForButton()  // Lock icon (18px, warning)
IconFactory.createDeleteIconForButton() // Trash icon (18px, danger)

// Menu icons (18px, white)
IconFactory.createQuestionBankIcon()   // Book icon
IconFactory.createExamIcon()           // File-alt icon
IconFactory.createGradingIcon()        // Edit icon
```

**Size Constants**:
- `SIZE_SMALL = 14`
- `SIZE_NORMAL = 16`
- `SIZE_MEDIUM = 20`
- `SIZE_LARGE = 24`
- `MENU_ICON_SIZE = 18`

**Color Constants**:
- `COLOR_PRIMARY = #2196F3`
- `COLOR_SUCCESS = #4CAF50`
- `COLOR_WARNING = #FF9800`
- `COLOR_DANGER = #F44336`
- `COLOR_INFO = #00BCD4`
- `COLOR_WHITE = white`
- `COLOR_GRAY = #757575`

**Usage Pattern**:
```java
Button viewButton = new Button();
viewButton.setGraphic(IconFactory.createViewIcon());
viewButton.getStyleClass().add("icon-button");
```

**Documentation**: `docs/IKONLI-USAGE-GUIDE.md`

---

## Build & Deployment

### Maven Profiles

**Development Profile (default):**
- Profile ID: `dev`
- API Base URL: `http://localhost:8080`
- Active by default: `true`
- Usage: `mvn clean package` hoặc `mvn clean package -Pdev`

**Production Profile:**
- Profile ID: `prod`
- API Base URL: `https://ttapi.manhhao.com`
- Usage: `mvn clean package -Pprod`

### Resource Filtering

**Configuration trong pom.xml:**
```xml
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
</build>
```

**Placeholder trong config.properties:**
```properties
api.base.url=${api.base.url}
```

**Kết quả sau khi build:**
- Development: `api.base.url=http://localhost:8080`
- Production: `api.base.url=https://ttapi.manhhao.com`

### Installer Build Process

**Requirements:**
- JDK 17+ (not JRE) - jpackage chỉ có trong JDK
- WiX Toolset (cho .exe installer) hoặc dùng `--type msi`
- Maven đã build JAR với profile `prod`

**Process:**
1. Build JAR với profile prod: `mvn clean package -Pprod`
2. Verify config trong JAR
3. Create runtime image với jpackage
4. Package installer (.exe hoặc .msi)

**Output:**
- Installer: `target\installer\MSTrustTestClient-1.0.0.exe`
- JAR: `target\exam-client-javafx-1.0.0.jar`
- Installer bao gồm JRE (standalone, không cần cài Java riêng)

**Troubleshooting:**
- "jpackage not found": Cần JDK 17+, không phải JRE
- "WiX Toolset not found": Cài WiX hoặc dùng `--type msi`
- Config vẫn localhost: Đảm bảo build với `-Pprod` và verify config

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 14:00  
**Last Updated**: 02/12/2025

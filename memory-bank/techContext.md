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

```properties
api.base.url=http://localhost:8080/api
websocket.url=ws://localhost:8080/ws
monitoring.enabled=true
monitoring.screenshot.interval=60000
```

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

```bash
# Build client
cd client
mvn clean install

# Run client
mvn javafx:run

# Package native installer (Windows)
mvn javafx:jlink
jpackage --type msi --input target --name MS.TrustTest --main-jar client.jar
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
- ⚠️ Spring Security blocking all endpoints (needs fix)

### Recent Configuration Changes (14/11/2025)
1. Updated datasource URL to remote server (104.199.231.104)
2. Changed database name from ms_trust_exam to MS.TrustTest
3. Disabled Flyway migration
4. Enabled debug logging for Spring Security
5. Updated timezone to Asia/Ho_Chi_Minh

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 14:00  
**Last Updated**: 14/11/2025 09:40

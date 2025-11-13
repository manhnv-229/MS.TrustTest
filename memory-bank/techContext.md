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
    name: ms-trust-exam
  
  datasource:
    url: jdbc:mysql://localhost:3306/ms_trust_exam?useSSL=false&serverTimezone=UTC
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

server:
  port: 8080
  servlet:
    context-path: /api

jwt:
  secret: ${JWT_SECRET:your-secret-key-change-in-production}
  expiration: 86400000  # 24 hours

monitoring:
  screenshot:
    interval: 60000  # 60 seconds
    quality: 0.7     # JPEG quality
    max-size: 5242880  # 5MB
```

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

```sql
-- Create database
CREATE DATABASE ms_trust_exam CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'mstrust'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON ms_trust_exam.* TO 'mstrust'@'localhost';
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

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 14:00  
**Last Updated**: 13/11/2025 14:00

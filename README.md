# MS.TrustTest - Hệ Thống Thi Trực Tuyến Với Giám Sát Chống Gian Lận

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

## 📋 Tổng Quan

MS.TrustTest là một hệ thống thi trực tuyến được thiết kế với mục tiêu **đảm bảo tính toàn vẹn và công bằng trong thi cử** thông qua công nghệ giám sát thông minh. Hệ thống hỗ trợ đa nền tảng (Windows, macOS, Linux) và được xây dựng bằng Java với Spring Boot (backend) và JavaFX (client).

### ✨ Đặc Điểm Nổi Bật

- 🔐 **Xác thực đa hình thức**: Đăng nhập bằng mã sinh viên, email, hoặc số điện thoại
- 🎯 **Quản lý tổ chức**: Khoa → Lớp → Sinh viên, hỗ trợ lớp môn học linh hoạt
- 📝 **Đa dạng loại bài thi**: 
  - **Mục đích**: Kiểm tra nhanh, Tiến độ, Giữa kỳ, Cuối kỳ, Kết thúc học phần, Thi lại, Bài tập, Luyện tập
  - **Hình thức**: Trắc nghiệm, Tự luận, Lập trình, Hỗn hợp
  - **Câu hỏi**: 8 loại (Trắc nghiệm đơn, Trắc nghiệm nhiều đáp án, Đúng/Sai, Tự luận, Trả lời ngắn, Lập trình, Điền chỗ trống, Nối câu)
- ⏰ **Kiểm soát thời gian**: Thời gian bắt đầu/kết thúc, thời lượng làm bài
- 🔍 **Giám sát chống gian lận** (Core Feature):
  - Chụp màn hình ngẫu nhiên
  - Phát hiện chuyển cửa sổ (Alt+Tab)
  - Phát hiện AI tools (ChatGPT, Copilot, Cursor)
  - Monitor clipboard, keystroke patterns
  - Cảnh báo real-time qua WebSocket
- ✅ **Tự động chấm điểm**: Chấm trắc nghiệm tự động, interface chấm tự luận
- 📊 **Dashboard admin**: Giám sát real-time, cấu hình hệ thống

---

## 🏗️ Kiến Trúc Hệ Thống

```
┌─────────────────────────────────────┐
│     JavaFX Desktop Client           │
│  (Windows/macOS/Linux)              │
│  + Monitoring Agents                │
└─────────────────────────────────────┘
                ↕ REST API / WebSocket
┌─────────────────────────────────────┐
│     Spring Boot Backend             │
│  + Spring Security                  │
│  + Spring Data JPA                  │
│  + WebSocket                        │
└─────────────────────────────────────┘
                ↕ JDBC
┌─────────────────────────────────────┐
│        MySQL Database               │
│  16 tables, InnoDB, UTF8MB4         │
└─────────────────────────────────────┘
```

---

## 🚀 Bắt Đầu Nhanh

### Yêu Cầu Hệ Thống

- **Java JDK**: 17 hoặc cao hơn
- **Maven**: 3.9.x hoặc cao hơn
- **MySQL**: 8.0.x hoặc cao hơn
- **IDE**: IntelliJ IDEA / Eclipse / VS Code (khuyến nghị)
- **RAM**: Tối thiểu 4GB (khuyến nghị 8GB)

### Cài Đặt

#### 1. Clone Repository

```bash
git clone https://github.com/your-org/ms-trust-exam.git
cd ms-trust-exam
```

#### 2. Cài Đặt Database

```bash
# Đăng nhập MySQL
mysql -u root -p

# Tạo database
CREATE DATABASE ms_trust_exam CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Tạo user
CREATE USER 'mstrust'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON ms_trust_exam.* TO 'mstrust'@'localhost';
FLUSH PRIVILEGES;
```

#### 3. Cấu Hình Backend

Chỉnh sửa `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ms_trust_exam
    username: mstrust
    password: your_password

jwt:
  secret: your-secret-key-at-least-32-characters
```

#### 4. Build & Run

**Backend:**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Client:**
```bash
cd client
mvn clean install
mvn javafx:run
```

### Tài Khoản Mặc Định

- **Username**: `ADMIN`
- **Password**: `Admin@123`
- **Role**: Admin (toàn quyền)

---

## 📚 Tài Liệu

### Cấu Trúc Dự Án

```
MS.TrustTest/
├── memory-bank/              # Memory Bank (Cline AI)
│   ├── projectbrief.md       # Tổng quan dự án
│   ├── productContext.md     # Bối cảnh sản phẩm
│   ├── systemPatterns.md     # Kiến trúc & patterns
│   ├── techContext.md        # Stack công nghệ
│   ├── activeContext.md      # Trạng thái hiện tại
│   └── progress.md           # Tiến độ dự án
├── docs/
│   ├── phases/
│   │   └── phase-1-setup.md  # Phase 1 chi tiết
│   └── phases-summary.md     # Tổng hợp 8 phases
├── backend/                  # Spring Boot backend
│   ├── src/
│   │   ├── main/java/
│   │   │   └── com/mstrust/exam/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── entity/
│   │   │       ├── dto/
│   │   │       ├── security/
│   │   │       └── websocket/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── pom.xml
├── client/                   # JavaFX client
│   ├── src/
│   │   ├── main/java/
│   │   │   └── com/mstrust/client/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── monitoring/
│   │   │       └── model/
│   │   └── resources/
│   │       ├── fxml/
│   │       └── css/
│   └── pom.xml
├── database/
│   ├── init-schema.sql
│   └── sample-data.sql
├── pom.xml                   # Root POM
└── README.md
```

### Các Phase Phát Triển

| Phase | Tên | Thời gian | Trạng thái |
|-------|-----|-----------|------------|
| 0 | Planning & Documentation | 1 ngày | ✅ Hoàn thành |
| 1 | Setup & Database Schema | 1 tuần | ⏳ Chưa bắt đầu |
| 2 | Authentication & Authorization | 1 tuần | ⏳ Chưa bắt đầu |
| 3 | Organization Management | 1-2 tuần | ⏳ Chưa bắt đầu |
| 4 | Exam Management | 2 tuần | ⏳ Chưa bắt đầu |
| 5 | Exam Taking Interface | 2 tuần | ⏳ Chưa bắt đầu |
| 6 | Anti-Cheat Monitoring | 3 tuần | ⏳ Chưa bắt đầu |
| 7 | Grading & Results | 1 tuần | ⏳ Chưa bắt đầu |
| 8 | Admin Dashboard | 1 tuần | ⏳ Chưa bắt đầu |

**Tổng thời gian dự kiến**: 8-12 tuần

Chi tiết: Xem [docs/phases-summary.md](docs/phases-summary.md)

---

## 🔧 Công Nghệ Sử Dụng

### Backend
- **Framework**: Spring Boot 3.2.x
- **Security**: Spring Security + JWT
- **Database**: MySQL 8.0.x
- **ORM**: Spring Data JPA (Hibernate)
- **Migration**: Flyway
- **Real-time**: WebSocket + STOMP
- **Build**: Maven

### Client
- **UI**: JavaFX 21
- **Monitoring**: JNA (Java Native Access)
- **HTTP Client**: Spring WebClient
- **WebSocket**: STOMP Client
- **Build**: Maven

### Database Schema
- **16 tables**: users, roles, departments, classes, exams, questions, submissions, monitoring_logs, screenshots, alerts, etc.
- **Engine**: InnoDB
- **Charset**: UTF8MB4
- **Relationships**: Properly normalized with foreign keys

---

## 🎯 Use Cases Chính

### 1. Sinh Viên (Student)
- ✅ Đăng nhập bằng mã SV/email/SĐT
- ✅ Xem danh sách bài thi được gán
- ✅ Làm bài thi trong thời gian quy định
- ✅ Xem lại kết quả và đáp án (nếu được phép)
- ⚠️ Nhận cảnh báo khi có hành vi nghi ngờ

### 2. Giáo Viên (Teacher)
- ✅ Tạo đề thi (trắc nghiệm/tự luận)
- ✅ Quản lý ngân hàng câu hỏi
- ✅ Gán bài thi cho lớp môn học
- ✅ Chấm bài tự động (trắc nghiệm)
- ✅ Chấm bài thủ công (tự luận)
- ✅ Xem báo cáo thống kê lớp

### 3. Quản Lý Lớp (Class Manager)
- ✅ Quản lý sinh viên trong lớp
- ✅ Thêm/xóa sinh viên
- ✅ Xem danh sách lớp môn học

### 4. Quản Lý Khoa (Department Manager)
- ✅ Quản lý tất cả lớp trong khoa
- ✅ Quản lý sinh viên trong khoa
- ✅ Phân công giáo viên

### 5. Admin (System Administrator)
- ✅ Giám sát thi real-time
- ✅ Xem cảnh báo gian lận
- ✅ Cấu hình hệ thống
- ✅ Quản lý người dùng hàng loạt
- ✅ Xem logs và audit trail

---

## 🔒 Bảo Mật

### Authentication
- ✅ BCrypt password hashing (cost factor: 12)
- ✅ JWT tokens (24h expiration)
- ✅ Stateless authentication
- ✅ Role-based access control (RBAC)

### Monitoring Security
- ✅ Screenshots encrypted in transit
- ✅ Monitoring data only accessible by authorized users
- ✅ Transparent monitoring (sinh viên biết họ đang được giám sát)
- ✅ Data retention policy (tự động xóa sau 6 tháng)

### API Security
- ✅ Input validation
- ✅ SQL injection prevention (Prepared statements)
- ✅ XSS protection
- ✅ CSRF protection (disabled for stateless REST API)
- ✅ Rate limiting (future)

---

## 📊 Performance Targets

| Metric | Target | Current |
|--------|--------|---------|
| API Response Time (p95) | < 500ms | TBD |
| WebSocket Latency | < 100ms | TBD |
| Concurrent Users | 500+ | TBD |
| Screenshot Upload | < 2s | TBD |
| Database Query (p95) | < 100ms | TBD |
| Uptime | ≥ 99.5% | TBD |

---

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

### Coverage Report
```bash
mvn jacoco:report
```

**Target**: > 80% code coverage

---

## 🚧 Roadmap

### Version 1.0 (MVP) - Q1 2026
- ✅ Core features (8 phases)
- ✅ Basic monitoring
- ✅ Auto-grading
- ✅ Admin dashboard

### Version 1.1 - Q2 2026
- 📋 Import/Export Excel
- 📋 Email notifications
- 📋 Advanced analytics
- 📋 Question tagging

### Version 2.0 - Q3 2026
- 📋 Mobile app
- 📋 Video proctoring (optional)
- 📋 AI plagiarism detection
- 📋 LMS integration

### Version 3.0 - Q4 2026
- 📋 Blockchain certificates
- 📋 Peer review
- 📋 Adaptive testing
- 📋 Multi-language

---

## 🤝 Đóng Góp

Dự án này hiện đang trong giai đoạn phát triển nội bộ. Vui lòng liên hệ team để biết thêm thông tin về việc đóng góp.

---

## 📝 License

Proprietary - All rights reserved

---

## 👥 Team

- **Product Owner**: Cụ Mạnh
- **Developer**: AI Assistant (Cline)
- **Support**: K24DTCN210-NVMANH

---

## 📞 Liên Hệ

- **Email**: support@mstrust.edu.vn
- **GitHub**: [https://github.com/your-org/ms-trust-exam](https://github.com/your-org/ms-trust-exam)
- **Documentation**: [docs/](docs/)

---

## 🙏 Acknowledgments

- Spring Boot Team
- JavaFX Community
- JNA Contributors
- All open-source contributors

---

**Built with ❤️ and ☕ by MS.Trust Team**

---

**Last Updated**: 13/11/2025  
**Version**: 1.0.0-SNAPSHOT  
**Status**: 📋 Planning Phase (5% complete)

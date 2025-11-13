# Project Brief: MS.TrustTest

## Tổng Quan Dự Án

**Tên dự án**: MS.TrustTest  
**Mục đích**: Hệ thống thi trực tuyến với khả năng giám sát chống gian lận  
**Ngôn ngữ**: Java  
**Database**: MySQL  
**Nền tảng**: Cross-platform (Windows, macOS, Linux)

## Mục Tiêu Chính

1. Xây dựng hệ thống quản lý thi trực tuyến hoàn chỉnh
2. Tích hợp giám sát chống gian lận thời gian thực
3. Hỗ trợ đa dạng loại bài thi (trắc nghiệm, tự luận)
4. Quản lý phân quyền theo cấp bậc tổ chức
5. Đảm bảo tính bảo mật và công bằng trong thi cử

## Phạm Vi Dự Án

### Các Module Chính

1. **Authentication & Authorization**
   - Đăng nhập đa hình thức (Mã SV, Email, SĐT)
   - Mã hóa mật khẩu BCrypt
   - JWT token authentication
   - Phân quyền 5 cấp: Student, Teacher, Class Manager, Department Manager, Admin

2. **Organization Management**
   - Quản lý Khoa
   - Quản lý Lớp (Lớp chung + Lớp môn học)
   - Quản lý Sinh viên
   - Quản lý Giáo viên và phân công

3. **Exam Management**
   - Tạo đề thi (trắc nghiệm/tự luận)
   - Thiết lập thời gian thi
   - Gán bài thi cho nhiều lớp
   - Ngân hàng câu hỏi

4. **Exam Taking**
   - Giao diện làm bài
   - Kiểm tra thời gian hợp lệ
   - Auto-save
   - Đếm ngược thời gian
   - Nộp bài

5. **Anti-Cheat Monitoring** (Trọng tâm)
   - Chụp màn hình ngẫu nhiên (config: default 1 phút/lần)
   - Phát hiện chuyển cửa sổ (browser, IDE, AI tools)
   - Monitor clipboard
   - Phát hiện process đáng ngờ (Copilot, extensions, ChatGPT)
   - Log chi tiết mọi hành động
   - Cảnh báo real-time qua WebSocket

6. **Grading & Results**
   - Tự động chấm trắc nghiệm
   - Interface chấm tự luận
   - Xem lịch sử và kết quả thi

7. **Admin Dashboard**
   - Giám sát thi real-time
   - Cấu hình hệ thống
   - Quản lý người dùng
   - Logs và audit trail

## Kiến Trúc Kỹ Thuật

### Backend
- **Framework**: Spring Boot 3.x
- **Database**: MySQL 8.x
- **Security**: Spring Security + JWT
- **Real-time**: WebSocket
- **ORM**: Spring Data JPA
- **Migration**: Flyway

### Client
- **UI Framework**: JavaFX 21
- **Communication**: REST API + WebSocket
- **Monitoring**: JNA (Java Native Access)
- **Screen Capture**: Robot API

### Architecture Pattern
- 3-tier architecture: Client ↔ Backend API ↔ Database
- RESTful API cho CRUD operations
- WebSocket cho real-time monitoring
- Multi-module Maven project

## Cấu Trúc Tổ Chức

```
Trường (School)
└── Khoa (Department)
    ├── Lớp Chung (Class) - SV cùng khóa/ngành
    └── Lớp Môn Học (Subject Class) - SV từ nhiều lớp/khóa
        └── Sinh Viên (Student)
```

## Phân Quyền Chi Tiết

1. **STUDENT**: Làm bài thi, xem kết quả của mình
2. **TEACHER**: Tạo đề thi, chấm bài cho lớp môn học được phân công
3. **CLASS_MANAGER**: Quản lý lớp chung (CRUD sinh viên trong lớp)
4. **DEPT_MANAGER**: Quản lý khoa (CRUD tất cả lớp và sinh viên trong khoa)
5. **ADMIN**: Toàn quyền hệ thống

## Ràng Buộc & Yêu Cầu Phi Chức Năng

1. **Security**:
   - Mọi mật khẩu phải được BCrypt hash
   - JWT token hết hạn sau 24h
   - HTTPS/WSS cho production

2. **Performance**:
   - Response time API < 500ms
   - WebSocket latency < 100ms
   - Hỗ trợ đồng thời 500+ sinh viên thi

3. **Reliability**:
   - Auto-save bài thi mỗi 30s
   - Xử lý mất kết nối gracefully
   - Backup screenshots định kỳ

4. **Usability**:
   - Giao diện trực quan, dễ sử dụng
   - Hỗ trợ tiếng Việt
   - Cross-platform compatibility

## Các Phase Phát Triển

1. **Phase 1**: Setup & Database Schema
2. **Phase 2**: Authentication & Authorization
3. **Phase 3**: Organization Management
4. **Phase 4**: Exam Management
5. **Phase 5**: Exam Taking Interface
6. **Phase 6**: Anti-Cheat Monitoring (Core Feature)
7. **Phase 7**: Grading & Results
8. **Phase 8**: Admin Dashboard & Configuration

## Tính Năng Tương Lai (Không trong scope hiện tại)

- Import/Export Excel
- Báo cáo thống kê nâng cao
- Backup/Restore tự động
- Email notifications
- Mobile app

## Stakeholders

- **Product Owner**: Cụ Mạnh
- **Developer**: AI Assistant (Cline)
- **End Users**: Sinh viên, Giáo viên, Quản lý, Admin

## Success Criteria

- [ ] Sinh viên có thể làm bài thi trong thời gian quy định
- [ ] Hệ thống phát hiện được 95%+ hành vi gian lận
- [ ] Giáo viên có thể tạo và chấm bài dễ dàng
- [ ] Admin có thể giám sát real-time toàn bộ hệ thống
- [ ] Zero data loss trong quá trình thi
- [ ] Chạy stable trên Windows, macOS, Linux

## Timeline

Dự kiến: 8-12 tuần (tùy theo complexity của từng phase)

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 13:55  
**Last Updated**: 13/11/2025 13:55

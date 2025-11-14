# Active Context: MS.TrustTest

## Current Work Focus

**Status**: Phase 2 Complete - Đang fix lỗi database connection  
**Phase**: Phase 2 - Authentication & Authorization (COMPLETED)  
**Date**: 14/11/2025

## Recent Activities

### Completed Today (14/11/2025)
1. ✅ Fix lỗi database connection:
   - Cập nhật `application.yml` với thông tin kết nối đúng
   - Host: `104.199.231.104:3306`
   - Database: `MS.TrustTest`
   - Username: `nvmanh`
   - Password: `!M@nh1989`
2. ✅ Tắt Flyway migration (database đã có sẵn tables)
3. ✅ Chạy thành công Spring Boot application trên port 8080
4. ✅ Xác nhận ứng dụng hoạt động với remote database

### Completed (13/11/2025)
1. ✅ Phase 1: Setup & Database Schema
2. ✅ Phase 2: Authentication & Authorization (26 files)
3. ✅ Tạo Memory Bank hoàn chỉnh
4. ✅ Database schema với 16 tables
5. ✅ MCP Server (ms-trust-test-server)

### In Progress
- 🔄 Testing API endpoints với remote database
- 🔄 Sẵn sàng bắt đầu Phase 3

## Next Steps

### Immediate (Hôm nay)
1. Test các API endpoints:
   - POST `/api/auth/login`
   - POST `/api/auth/register`
   - GET `/api/users`
2. Verify JWT authentication hoạt động đúng
3. Test với dữ liệu có sẵn trong database

### Short-term (Tuần này)
1. Bắt đầu Phase 3: Department & Class Management
2. Tạo Department Service & Controller
3. Tạo Class Service & Controller
4. Implement student enrollment APIs

### Medium-term (2 tuần tới)
1. Complete Phase 3
2. Start Phase 4: Subject & Course Management
3. Begin writing unit tests

## Key Decisions Made

### Architecture
- ✅ **Pattern**: 3-tier architecture (Client - Backend - Database)
- ✅ **Backend**: Spring Boot 3.5.7 với Spring Security + JWT
- ✅ **Client**: JavaFX 21 với native installers
- ✅ **Database**: MySQL 8.0.x (Remote server tại 104.199.231.104)
- ✅ **Real-time**: WebSocket cho monitoring alerts

### Technology Choices
- ✅ **Java 25**: Latest version với modern features
- ✅ **Maven 3.9.11**: Build tool cho multi-module project
- ✅ **JNA**: Để monitor processes trên client
- ✅ **BCrypt**: Password hashing với cost factor 12
- ✅ **JWT**: Stateless authentication, 24h expiration

### Project Structure
- ✅ **Multi-module**: Tách backend và client thành 2 modules riêng
- ✅ **Documentation**: Tách riêng docs/ và memory-bank/
- ✅ **Database scripts**: Centralized trong database/

### Database Configuration (NEW)
- ✅ **Remote Database**: 104.199.231.104:3306
- ✅ **Database Name**: MS.TrustTest (không phải ms_trust_exam)
- ✅ **Flyway**: Disabled vì database đã có sẵn tables
- ✅ **JPA ddl-auto**: validate (không tạo/sửa tables)

## Pending Decisions

### Cần xác nhận từ cụ Mạnh
1. ⏳ Có cần thêm tính năng nào không?
2. ⏳ Timeline implementation có phù hợp không? (8-12 tuần)
3. ⏳ Có cần demo/prototype trước khi bắt đầu full implementation?

### Technical
1. ⏳ Sử dụng Redis cho caching? (Có thể defer đến v1.1)
2. ⏳ Containerization với Docker? (Production deployment)
3. ⏳ CI/CD pipeline setup? (GitHub Actions hoặc Jenkins)

## Current Challenges

### Recently Resolved ✅
- ✅ **Database Connection Issues**: 
  - Đã fix bằng cách cập nhật đúng thông tin remote database
  - Tắt Flyway vì tables đã tồn tại
  
### Current Issues
- ⚠️ **Spring Security Configuration**: 
  - Tất cả endpoints đang trả về 403 Forbidden
  - Cần kiểm tra và fix SecurityConfig để cho phép public endpoints
  - Ảnh hưởng: Không thể test login API

### Anticipated Technical Challenges
1. **Client Monitoring**: 
   - Cross-platform compatibility (Windows/Mac/Linux)
   - Permissions handling (Screen recording, accessibility)
   - Performance impact trên máy sinh viên

2. **Real-time Communication**:
   - WebSocket connection stability
   - Handle reconnection gracefully
   - Scalability với 500+ concurrent users

3. **Security**:
   - Prevent tampering với client app
   - Screenshot security (encryption, storage)
   - JWT token management

## Important Notes

### For Future Reference
- Mọi function phải comment đầy đủ theo format trong .clinerules
- Database migration phải có rollback script
- API endpoints phải có validation và error handling
- Client monitoring phải transparent cho sinh viên

### Code Style Guidelines
- Java: Google Java Style Guide
- SQL: Uppercase keywords, snake_case tables
- REST API: RESTful conventions, HTTP status codes
- Git commit: Conventional Commits format

## Stakeholder Communication

### Cụ Mạnh (Product Owner)
- **Last update**: 13/11/2025 13:49 - Đã confirm requirements
- **Next update**: Sau khi hoàn thành Phase documents
- **Communication**: Through Cline chat

### Dependencies
- None currently (greenfield project)

## Risk Assessment

### High Risk
- ⚠️ **Cross-platform monitoring**: Khác biệt giữa OS có thể gây issues
  - Mitigation: Test sớm trên cả 3 platforms
  
- ⚠️ **Performance**: Screenshot capture + upload có thể slow
  - Mitigation: Compression, async upload, configurable interval

### Medium Risk
- ⚠️ **Scalability**: 500+ concurrent users
  - Mitigation: Load testing, optimize queries, consider caching
  
- ⚠️ **Security**: Client app có thể bị reverse engineer
  - Mitigation: Code obfuscation, server-side validation

### Low Risk
- ✓ **Technology maturity**: Spring Boot và JavaFX đều mature
- ✓ **Team expertise**: AI assistant có knowledge về stack này

## Metrics to Track

### Development Progress
- [ ] Memory Bank completion: 83% (5/6 files done)
- [ ] Phase documents: 0% (0/8 phases written)
- [ ] Database schema: 0%
- [ ] Backend implementation: 0%
- [ ] Client implementation: 0%

### Quality Metrics (When implementation starts)
- Code coverage target: > 80%
- API response time: < 500ms (p95)
- Bug rate: < 5 per 1000 LOC
- Documentation coverage: 100%

## Resources & References

### Documentation
- Spring Boot Docs: https://spring.io/projects/spring-boot
- JavaFX Docs: https://openjfx.io/
- JNA Documentation: https://github.com/java-native-access/jna

### Similar Projects (For reference)
- ProctorU: Online proctoring system
- ExamSoft: Secure exam software
- Respondus LockDown Browser: Browser-based exam lock

## Daily Standup Notes

### 14/11/2025 (9:38 AM)
- **Completed**: 
  - ✅ Fixed database connection issues
  - ✅ Application chạy thành công trên port 8080
  - ✅ Kết nối thành công đến remote database
- **Current Issue**: 
  - Spring Security đang block tất cả requests (403 Forbidden)
  - Cần fix SecurityConfig để allow public endpoints
- **Today's Goal**: 
  - Fix Security configuration
  - Test login/register APIs
  - Begin Phase 3 if time permits
- **Blockers**: Security config cần được điều chỉnh

### 13/11/2025
- **Completed**: Phase 1 & 2 implementation
- **Today's Goal**: Documentation và testing
- **Blockers**: None

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 14:01  
**Last Updated**: 13/11/2025 14:01  
**Next Review**: Sau khi hoàn thành Phase documents

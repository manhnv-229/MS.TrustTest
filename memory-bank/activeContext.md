# Active Context: MS.TrustTest

## Current Work Focus

**Status**: Đang trong giai đoạn lập kế hoạch và tạo tài liệu  
**Phase**: Planning & Documentation  
**Date**: 13/11/2025

## Recent Activities

### Completed
1. ✅ Tạo cấu trúc thư mục dự án
2. ✅ Tạo Memory Bank:
   - projectbrief.md - Tổng quan dự án
   - productContext.md - Bối cảnh sản phẩm và user personas
   - systemPatterns.md - Kiến trúc và design patterns
   - techContext.md - Stack công nghệ và cấu hình
   - activeContext.md - Trạng thái hiện tại (đang tạo)

### In Progress
- 🔄 Tạo progress.md
- 🔄 Viết chi tiết 8 Phase vào docs/phases/

## Next Steps

### Immediate (Hôm nay)
1. Hoàn thành file `progress.md`
2. Tạo chi tiết Phase 1: Setup & Database Schema
3. Tạo chi tiết Phase 2: Authentication & Authorization
4. Tạo chi tiết Phase 3: Organization Management

### Short-term (Tuần này)
1. Hoàn thiện tất cả 8 Phase documents
2. Tạo database schema diagram
3. Tạo API documentation skeleton
4. Chuẩn bị .clinerules với project-specific patterns

### Medium-term (2 tuần tới)
1. Chờ cụ Mạnh review và approve kế hoạch
2. Bắt đầu implementation Phase 1
3. Setup project structure (Maven multi-module)
4. Configure Spring Boot và JavaFX

## Key Decisions Made

### Architecture
- ✅ **Pattern**: 3-tier architecture (Client - Backend - Database)
- ✅ **Backend**: Spring Boot 3.2.x với Spring Security + JWT
- ✅ **Client**: JavaFX 21 với native installers
- ✅ **Database**: MySQL 8.0.x với Flyway migration
- ✅ **Real-time**: WebSocket cho monitoring alerts

### Technology Choices
- ✅ **Java 17**: LTS version, stable và modern features
- ✅ **Maven**: Build tool cho multi-module project
- ✅ **JNA**: Để monitor processes trên client
- ✅ **BCrypt**: Password hashing với cost factor 12
- ✅ **JWT**: Stateless authentication, 24h expiration

### Project Structure
- ✅ **Multi-module**: Tách backend và client thành 2 modules riêng
- ✅ **Documentation**: Tách riêng docs/ và memory-bank/
- ✅ **Database scripts**: Centralized trong database/

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

### Documentation Phase
- Đảm bảo tài liệu đủ chi tiết cho mỗi Phase
- Balance giữa detail và flexibility
- Tránh over-engineering ngay từ đầu

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

### 13/11/2025
- **Completed**: Tạo Memory Bank structure
- **Today's Goal**: Hoàn thành progress.md và bắt đầu Phase documents
- **Blockers**: None

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 14:01  
**Last Updated**: 13/11/2025 14:01  
**Next Review**: Sau khi hoàn thành Phase documents

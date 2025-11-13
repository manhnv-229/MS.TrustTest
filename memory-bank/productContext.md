# Product Context: MS.TrustTest

## Vấn Đề Cần Giải Quyết

### 1. Thực Trạng Thi Trực Tuyến Hiện Nay

**Vấn đề gian lận nghiêm trọng**:
- Sinh viên sử dụng AI tools (ChatGPT, Claude, Copilot) để làm bài
- Tìm kiếm đáp án trên Google, Stack Overflow
- Copy-paste từ tài liệu, source code
- Sử dụng extensions hỗ trợ coding trong IDE
- Truyền file, nhắn tin với người khác

**Hạn chế của các hệ thống hiện tại**:
- Không có giám sát thời gian thực
- Không phát hiện được hành vi đáng ngờ
- Khó khăn trong việc chứng minh gian lận
- Giáo viên không thể theo dõi nhiều sinh viên cùng lúc

### 2. Nhu Cầu Thực Tế

**Từ góc độ Giáo viên**:
- Cần công cụ tạo đề thi nhanh chóng
- Muốn chấm bài tự động (trắc nghiệm)
- Cần giám sát sinh viên trong quá trình thi
- Muốn có bằng chứng khi phát hiện gian lận

**Từ góc độ Sinh viên**:
- Cần giao diện làm bài đơn giản, dễ sử dụng
- Muốn biết rõ thời gian còn lại
- Cần tính năng auto-save tránh mất bài
- Mong muốn công bằng trong thi cử

**Từ góc độ Quản lý**:
- Cần giám sát toàn bộ quá trình thi
- Muốn báo cáo thống kê chi tiết
- Cần quản lý người dùng theo phân cấp
- Muốn cấu hình linh hoạt hệ thống

## Giải Pháp: MS.TrustTest

### Tầm Nhìn (Vision)

> "Xây dựng hệ thống thi trực tuyến đáng tin cậy nhất, đảm bảo tính công bằng và minh bạch trong giáo dục, thông qua công nghệ giám sát thông minh và thân thiện với người dùng."

### Sứ Mệnh (Mission)

1. **Tính toàn vẹn**: Đảm bảo mọi bài thi được thực hiện trung thực
2. **Công bằng**: Tạo môi trường thi bình đẳng cho tất cả sinh viên
3. **Minh bạch**: Cung cấp bằng chứng rõ ràng về hành vi trong thi
4. **Hiệu quả**: Giảm tải công việc cho giáo viên, tăng năng suất

### Giá Trị Cốt Lõi (Core Values)

1. **Trust (Tin cậy)**: Hệ thống đáng tin cậy, không có lỗ hổng
2. **Fairness (Công bằng)**: Áp dụng quy tắc như nhau cho mọi người
3. **Transparency (Minh bạch)**: Sinh viên biết mình đang được giám sát
4. **Privacy (Riêng tư)**: Dữ liệu giám sát chỉ dùng cho mục đích thi cử

## Đặc Điểm Sản Phẩm

### 1. Anti-Cheat Monitoring (Trọng tâm)

**Điểm khác biệt chính**:
- Giám sát đa chiều: màn hình, process, clipboard, window focus
- Real-time alerting: Cảnh báo ngay khi phát hiện
- Bằng chứng trực quan: Screenshots tự động
- Configurable: Admin tùy chỉnh mức độ giám sát

**Cách thức hoạt động**:
```
Student starts exam
    ↓
Client monitors activate
    ↓ (Continuous monitoring)
- Screen capture every ~60s
- Window focus tracking
- Process monitoring
- Clipboard tracking
- Keystroke analysis
    ↓ (If suspicious activity detected)
Alert sent to Admin Dashboard
    ↓
Admin reviews evidence
    ↓
Take appropriate action
```

**Hành vi được phát hiện**:
- ✅ Chuyển sang browser (Google, ChatGPT)
- ✅ Mở IDE với AI tools (Copilot, Cursor)
- ✅ Sử dụng extensions (ChatGPT, Claude)
- ✅ Copy-paste nội dung dài
- ✅ Alt+Tab ra khỏi ứng dụng thi
- ✅ Truy cập file explorer
- ✅ Mở nhiều cửa sổ

### 2. User Experience (UX)

**Cho Sinh viên**:
- Giao diện clean, không distraction
- Timer countdown rõ ràng
- Auto-save mỗi 30s
- Cảnh báo thân thiện khi có hành vi nghi ngờ
- Xem lại đáp án sau khi thi (nếu giáo viên cho phép)

**Cho Giáo viên**:
- Tạo đề thi bằng form đơn giản
- Import câu hỏi từ ngân hàng
- Chấm trắc nghiệm tự động
- Interface chấm tự luận trực quan
- Xem report chi tiết từng sinh viên

**Cho Admin**:
- Dashboard tổng quan real-time
- Heatmap hoạt động nghi ngờ
- Cấu hình monitoring rules
- Quản lý user hàng loạt
- Export logs và reports

### 3. Security & Privacy

**Bảo mật dữ liệu**:
- Mật khẩu BCrypt (cost factor 12)
- JWT tokens với expiration
- HTTPS/WSS cho production
- SQL injection prevention (Prepared statements)
- XSS protection

**Quyền riêng tư**:
- Screenshots chỉ lưu trong thời gian thi
- Không ghi âm, không camera (chỉ màn hình)
- Sinh viên được thông báo rõ về giám sát
- Dữ liệu chỉ admin và giáo viên liên quan được xem
- Tự động xóa dữ liệu cũ sau 6 tháng

### 4. Scalability & Performance

**Khả năng mở rộng**:
- Hỗ trợ 500+ concurrent users
- WebSocket với load balancing
- Database indexing tối ưu
- Caching với Redis (future)
- Microservices ready (future)

**Performance**:
- API response < 500ms
- WebSocket latency < 100ms
- Screenshot compression (JPEG quality 70%)
- Lazy loading cho exam list
- Pagination cho large datasets

## User Personas

### 1. Sinh Viên - Minh (19 tuổi)

**Background**:
- Sinh viên năm 2, ngành CNTT
- Quen với công nghệ
- Lo lắng về tính công bằng trong thi online

**Goals**:
- Làm bài thi một cách trung thực
- Không muốn bị nghi ngờ oan
- Muốn kết quả phản ánh đúng năng lực

**Pain Points**:
- Sợ bị lỗi kỹ thuật mất bài
- Không rõ còn bao nhiêu thời gian
- Lo lắng về việc giám sát

**How MS.TrustTest helps**:
- Auto-save tránh mất dữ liệu
- Timer rõ ràng
- Thông báo giám sát minh bạch
- Giao diện đơn giản, không gây stress

### 2. Giáo Viên - Cô Hương (35 tuổi)

**Background**:
- Giảng dạy 10 năm
- Dạy 5 lớp môn học (200+ sinh viên)
- Mệt mỏi với việc chấm bài thủ công

**Goals**:
- Tạo đề thi nhanh
- Chấm bài tự động
- Phát hiện gian lận
- Tiết kiệm thời gian

**Pain Points**:
- Tốn nhiều thời gian chấm trắc nghiệm
- Khó phát hiện gian lận
- Không giám sát được nhiều sinh viên
- Quản lý câu hỏi phức tạp

**How MS.TrustTest helps**:
- Tự động chấm trắc nghiệm
- Dashboard giám sát real-time
- Ngân hàng câu hỏi dễ quản lý
- Report chi tiết từng sinh viên

### 3. Admin - Anh Tuấn (40 tuổi)

**Background**:
- Quản lý IT của trường
- Chịu trách nhiệm vận hành hệ thống thi
- Cần đảm bảo uptime 99.9%

**Goals**:
- Hệ thống stable, không downtime
- Giám sát toàn bộ quá trình thi
- Cấu hình linh hoạt
- Xử lý sự cố nhanh chóng

**Pain Points**:
- Hệ thống cũ hay crash
- Không có monitoring tools
- Khó troubleshoot vấn đề
- Quản lý user thủ công

**How MS.TrustTest helps**:
- Dashboard admin toàn diện
- System health monitoring
- Logs chi tiết
- Cấu hình tập trung

## User Journey Maps

### Journey 1: Sinh Viên Làm Bài Thi

```
1. Login → Nhập mã SV/email/SĐT
   ├─ Success: Vào dashboard
   └─ Fail: Thông báo lỗi rõ ràng

2. Select Exam → Chọn bài thi có sẵn
   ├─ Check time: Còn trong thời gian cho phép?
   │  ├─ Yes: Cho phép vào thi
   │  └─ No: Thông báo "Bài thi chưa mở/đã đóng"
   └─ Check attempts: Đã làm chưa?
      ├─ No: Start exam
      └─ Yes: Thông báo "Đã nộp bài"

3. Take Exam → Giao diện làm bài
   ├─ Monitoring active (background)
   ├─ Auto-save every 30s
   ├─ Timer countdown visible
   └─ Navigation: Previous/Next question

4. Submit → Nộp bài
   ├─ Confirm dialog: "Chắc chắn nộp bài?"
   ├─ Upload final answers
   └─ Show result (if auto-graded)

5. View Results → Xem lại bài làm
   ├─ Score & ranking
   ├─ Correct answers (if allowed)
   └─ Feedback from teacher
```

### Journey 2: Giáo Viên Tạo Đề Thi

```
1. Login → Xác thực
2. Exam Management → Danh sách bài thi
3. Create New Exam
   ├─ Basic Info: Title, description, duration
   ├─ Time Settings: Start, end, timezone
   └─ Assign Classes: Select multiple classes

4. Add Questions
   ├─ From Question Bank
   │  ├─ Search & filter
   │  └─ Select multiple
   ├─ Create New Question
   │  ├─ Multiple Choice: Options + correct answer
   │  └─ Essay: Question text + max score
   └─ Randomize order (optional)

5. Configure Settings
   ├─ Monitoring level: Low/Medium/High
   ├─ Allow review: Yes/No
   └─ Passing score: 0-100

6. Publish Exam → Sinh viên có thể thấy
```

### Journey 3: Admin Giám Sát Thi

```
1. Login → Admin dashboard
2. Live Monitoring View
   ├─ Active exams list
   ├─ Students taking exams (real-time count)
   └─ Alerts feed (suspicious activities)

3. Investigate Alert
   ├─ Click alert → Student detail
   ├─ View screenshots timeline
   ├─ Check activity logs
   └─ Decision: Flag/Ignore

4. Review Flagged Students
   ├─ Export evidence (screenshots + logs)
   ├─ Send to teacher/department
   └─ Take action (warning/invalidate exam)

5. System Health Check
   ├─ Server status: CPU, Memory, Disk
   ├─ Database connections
   ├─ WebSocket connections
   └─ Error logs
```

## Success Metrics (KPIs)

### 1. Technical KPIs
- **Uptime**: ≥ 99.5%
- **API Response Time**: < 500ms (p95)
- **WebSocket Latency**: < 100ms (p95)
- **Screenshot Upload Success Rate**: ≥ 98%
- **Auto-save Success Rate**: ≥ 99.9%

### 2. Product KPIs
- **Cheat Detection Rate**: ≥ 95% (based on manual review)
- **False Positive Rate**: ≤ 5%
- **User Satisfaction**: ≥ 4.0/5.0
- **Exam Completion Rate**: ≥ 95% (students finish without technical issues)

### 3. Business KPIs
- **Teacher Time Saved**: ≥ 60% (vs manual grading)
- **Student Adoption**: ≥ 90% active usage
- **Support Tickets**: < 5 per exam session
- **System Cost**: < $X per 1000 students

## Competitive Advantage

### So với các hệ thống khác:

| Feature | MS.TrustTest | Hệ thống A | Hệ thống B |
|---------|--------------|------------|------------|
| Real-time Monitoring | ✅ | ❌ | ⚠️ Limited |
| Screenshot Capture | ✅ Random | ❌ | ✅ Fixed interval |
| AI Tool Detection | ✅ Advanced | ❌ | ⚠️ Basic |
| WebSocket Alerts | ✅ | ❌ | ❌ |
| Cross-platform | ✅ | ⚠️ Windows only | ✅ |
| Open Source | ✅ (potential) | ❌ | ❌ |
| Privacy-focused | ✅ | ⚠️ | ⚠️ |
| Configurable Rules | ✅ | ❌ | ⚠️ Limited |

## Roadmap

### Version 1.0 (MVP) - Current Scope
- ✅ Core authentication & authorization
- ✅ Basic exam management
- ✅ Anti-cheat monitoring
- ✅ Grading system
- ✅ Admin dashboard

### Version 1.1 (Q2 2025)
- Import/Export Excel
- Email notifications
- Advanced analytics
- Question bank tagging

### Version 2.0 (Q3 2025)
- Mobile app (Android/iOS)
- Video proctoring (optional)
- AI-powered plagiarism detection
- Integration với LMS (Moodle, Canvas)

### Version 3.0 (Q4 2025)
- Blockchain certificates
- Peer review system
- Adaptive testing
- Multi-language support

---

**Author**: K24DTCN210-NVMANH  
**Created**: 13/11/2025 13:57  
**Last Updated**: 13/11/2025 13:57

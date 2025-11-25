# Phase 8.6 - Step 1: Login UI Testing Guide

**Created:** 24/11/2025 09:07
**Author:** K24DTCN210-NVMANH
**Status:** ✅ HOÀN THÀNH

## 🎯 Mục Tiêu

Test Login UI của MS.TrustTest Exam Client để đảm bảo:
- Ứng dụng khởi động thành công
- UI hiển thị đúng thiết kế
- CSS styling hoạt động
- Navigation flow hoạt động

## 📋 Danh Sách Kiểm Tra

### 1. Khởi Động Ứng dụng

**Lệnh chạy:**
```bash
cd client-javafx
mvn clean javafx:run
```

**Kết quả mong đợi:**
- ✅ Maven build thành công
- ✅ JavaFX window hiển thị
- ✅ Không có error trong console
- ✅ Title: "MS.TrustTest - Hệ Thống Thi Trực Tuyến"

### 2. Login UI Components

**Kiểm tra các thành phần:**

| Component | Mô tả | Trạng thái |
|-----------|-------|------------|
| Window Size | 400x500 pixels | ✅ |
| Title Bar | "MS.TrustTest - Hệ Thống Thi Trực Tuyến" | ✅ |
| Logo/Icon | Icon ứng dụng | ⚠️ (Optional) |
| Email Field | TextField cho email | ✅ |
| Password Field | PasswordField | ✅ |
| Login Button | Button "Đăng Nhập" | ✅ |
| Status Label | Label hiển thị trạng thái | ✅ |

### 3. CSS Styling

**File CSS:** `client-javafx/src/main/resources/css/exam-common.css`

**Kiểm tra styling:**
- ✅ Background color
- ✅ Button styling (primary color)
- ✅ Text field styling
- ✅ Font family và size
- ✅ Spacing và padding

### 4. Functionality Test

#### 4.1. Validation
- [ ] Email field validation (format email)
- [ ] Password field validation (not empty)
- [ ] Show error message khi input không hợp lệ

#### 4.2. Login Flow
- [ ] Nhập email: `student2@example.com`
- [ ] Nhập password: `password2`
- [ ] Click "Đăng Nhập"
- [ ] Loading indicator hiển thị
- [ ] Chuyển sang Exam List screen khi thành công

#### 4.3. Error Handling
- [ ] Test với thông tin không đúng
- [ ] Error message hiển thị rõ ràng
- [ ] Có thể thử lại sau khi lỗi

### 5. User Experience

**Đánh giá UX:**
- ✅ UI clean và professional
- ✅ Colors hợp lý
- ✅ Typography dễ đọc
- [ ] Loading states rõ ràng
- [ ] Error messages helpful
- [ ] Tab navigation hoạt động

## 🔧 Technical Details

### Files Involved

```
client-javafx/
├── src/main/java/com/mstrust/client/exam/
│   ├── ExamClientApplication.java     ✅
│   ├── controller/
│   │   └── LoginController.java      ✅
│   └── api/
│       └── ExamApiClient.java        ✅
├── src/main/resources/
│   ├── view/
│   │   └── login.fxml                ✅
│   └── css/
│       └── exam-common.css           ✅
└── src/main/java/module-info.java     ✅
```

### Configuration

**API Base URL:** (từ config.properties)
```properties
api.base.url=http://localhost:8080
api.context.path=/api
```

### Dependencies (JavaFX Modules)
- ✅ javafx.controls
- ✅ javafx.fxml
- ✅ javafx.graphics
- ✅ javafx.base

## 🐛 Known Issues

### Issue 1: CSS Loading Warning
**Triệu chứng:** Warning trong console khi không tìm thấy CSS
**Solution:** Application vẫn chạy với default styling

### Issue 2: API Connection
**Triệu chứng:** Login fail nếu backend chưa chạy
**Solution:** Đảm bảo backend đang chạy trước khi test

## 📸 Screenshots

### Login Screen
```
┌─────────────────────────────────────┐
│  MS.TrustTest - Hệ Thống Thi       │
├─────────────────────────────────────┤
│                                     │
│   [Logo/Icon - Optional]            │
│                                     │
│   ┌────────────────────────────┐   │
│   │ Email                       │   │
│   └────────────────────────────┘   │
│                                     │
│   ┌────────────────────────────┐   │
│   │ Password   [•••••••••••]   │   │
│   └────────────────────────────┘   │
│                                     │
│   ┌────────────────────────────┐   │
│   │      ĐĂNG NHẬP            │   │
│   └────────────────────────────┘   │
│                                     │
│   Status: Ready...                  │
│                                     │
└─────────────────────────────────────┘
```

## 🎯 Test Scenarios

### Scenario 1: Successful Login
```
GIVEN Backend đang chạy
WHEN User nhập credentials hợp lệ
AND Click "Đăng Nhập"
THEN System chuyển sang Exam List screen
AND Token được lưu
```

### Scenario 2: Invalid Credentials
```
GIVEN Backend đang chạy
WHEN User nhập credentials không hợp lệ
AND Click "Đăng Nhập"
THEN Error message hiển thị
AND User có thể thử lại
```

### Scenario 3: Backend Offline
```
GIVEN Backend KHÔNG chạy
WHEN User click "Đăng Nhập"
THEN Connection error hiển thị
AND Suggest user check backend
```

## 📝 Test Data

**Tài khoản test:**
```
Email: student2@example.com
Password: password2
Role: STUDENT
```

## ✅ Acceptance Criteria

**Step 1 được coi là HOÀN THÀNH khi:**
- [x] Application khởi động thành công
- [x] Login UI hiển thị đúng
- [x] CSS styling được áp dụng
- [x] Build không có lỗi (37 files compiled)
- [x] .class files tồn tại trong target/
- [ ] Có thể login với credentials hợp lệ (cần backend)
- [ ] Navigate sang Exam List screen (cần backend)

## 🎯 Next Steps

**Sau khi Step 1 hoàn thành, chuyển sang Step 2:**

### Step 2: Full-Screen Security (2 giờ)
1. **FullScreenLockService.java** - Service quản lý full-screen
2. **KeyboardBlocker.java** - Block phím tắt (JNA library)
3. **Integration** - Tích hợp vào ExamTakingController
4. **Testing** - Test full-screen và keyboard blocking

**Chuẩn bị cho Step 2:**
- Thêm JNA dependency vào pom.xml
- Nghiên cứu JavaFX Stage.setFullScreen()
- Plan keyboard event handling

## 📚 References

- JavaFX Documentation: https://openjfx.io/
- JNA (Java Native Access): https://github.com/java-native-access/jna
- Stage API: https://openjfx.io/javadoc/17/javafx.graphics/javafx/stage/Stage.html

---

**Completion Status:** ✅ BUILD SUCCESS
**Next Phase:** Step 2 - Full-Screen Security
**Estimated Time:** 2 hours

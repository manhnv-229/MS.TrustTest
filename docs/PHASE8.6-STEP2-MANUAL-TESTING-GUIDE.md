# Phase 8.6 Step 2: Manual Testing Guide - Full-Screen Security

**Ngày tạo:** 24/11/2025 09:36  
**Người hướng dẫn:** K24DTCN210-NVMANH

## 🎯 Mục Đích Testing

Test các tính năng Full-Screen Security đã implement:
1. ✅ Full-screen lock enforcement
2. ✅ Auto re-enable full-screen
3. ✅ Exit confirmation dialog
4. ✅ Keyboard shortcuts blocking (Windows only)
5. ✅ Clean shutdown process

## 📋 Yêu Cầu Trước Khi Test

### Backend Server
```bash
# Terminal 1: Start backend server
cd backend
mvn spring-boot:run

# Verify server running:
# ✓ Should see: Started MsTrustExamApplication
# ✓ Port: http://localhost:8080
```

### Database
- MySQL server đang chạy
- Database `MS.TrustTest` đã có test data
- User test: `student1@test.com` / `password123`

### Build JavaFX Client
```bash
# Terminal 2: Build client
cd client-javafx
mvn clean compile

# Verify build success:
# ✓ BUILD SUCCESS
# ✓ 39 source files compiled
```

## 🧪 Test Cases

### Test Case 1: Application Startup ✅
**Mục đích:** Verify app khởi động bình thường

**Steps:**
```bash
cd client-javafx
mvn javafx:run
```

**Expected Results:**
- [ ] Login screen hiển thị
- [ ] Không có errors trong console
- [ ] UI responsive

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

### Test Case 2: Login & Navigate to Exam List ✅
**Mục đích:** Verify login flow và navigation

**Steps:**
1. Enter credentials:
   - Email: `student1@test.com`
   - Password: `password123`
2. Click "Đăng Nhập"

**Expected Results:**
- [ ] Login successful
- [ ] Navigate to Exam List screen
- [ ] Available exams displayed
- [ ] Console log: "Login successful"

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

### Test Case 3: Start Exam - Full-Screen Activation 🔒
**Mục đích:** Test full-screen lock kích hoạt khi bắt đầu thi

**Steps:**
1. Click "Bắt Đầu Thi" trên một exam available
2. Wait for exam loading

**Expected Results:**
- [ ] Window switches to FULL-SCREEN mode automatically
- [ ] No window border visible
- [ ] Exam interface occupies entire screen
- [ ] Console log: "Full-screen lock started"
- [ ] Console log: "Keyboard blocker installed" (Windows only)

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

**Verification Commands (in code):**
```java
// Check trong console:
// ✓ "Full-screen lock started"
// ✓ "Keyboard blocker installed successfully"
```

---

### Test Case 4: Exit Full-Screen Attempt (Press ESC) 🔄
**Mục đích:** Test auto re-enable full-screen

**Steps:**
1. Trong exam screen, press `ESC` key
2. Observe behavior

**Expected Results:**
- [ ] Full-screen temporarily exits
- [ ] **IMMEDIATELY** re-enabled automatically (within 100ms)
- [ ] User sees brief flash but cannot escape
- [ ] Console log: "Full-screen re-enabled"

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

### Test Case 5: Close Window Attempt (Alt+F4) 🛡️
**Mục đích:** Test keyboard blocker

**Prerequisites:** Windows OS only

**Steps:**
1. Trong exam screen, press `Alt+F4`
2. Observe behavior

**Expected Results:**
- [ ] Window does NOT close
- [ ] Key combination BLOCKED
- [ ] Exam continues normally
- [ ] Console log: "Blocking Alt+F4"

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

### Test Case 6: Task Switcher Attempt (Alt+Tab) 🛡️
**Mục đích:** Test keyboard blocker cho task switching

**Prerequisites:** Windows OS only

**Steps:**
1. Trong exam screen, press `Alt+Tab`
2. Try multiple times

**Expected Results:**
- [ ] Task switcher does NOT appear
- [ ] Cannot switch to other applications
- [ ] Key combination BLOCKED
- [ ] Console log: "Blocking Alt+Tab"

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

### Test Case 7: Windows Key Attempt 🛡️
**Mục đích:** Test Windows key blocking

**Prerequisites:** Windows OS only

**Steps:**
1. Trong exam screen, press `Windows key`
2. Try both left and right Windows keys

**Expected Results:**
- [ ] Start menu does NOT open
- [ ] Windows key BLOCKED
- [ ] Exam continues normally
- [ ] Console log: "Blocking Windows key"

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

### Test Case 8: Click [X] Button - Exit Confirmation 🚪
**Mục đích:** Test exit confirmation dialog

**Steps:**
1. Trong exam screen, click `[X]` button (close button) ở góc màn hình
2. Observe dialog

**Expected Results:**
- [ ] Confirmation dialog appears với message:
   ```
   ⚠️ Xác Nhận Thoát
   
   Bạn có chắc chắn muốn thoát khỏi bài thi?
   Tiến trình làm bài sẽ được lưu tự động.
   ```
- [ ] Dialog has 2 buttons: "Tiếp Tục Thi" và "Thoát"
- [ ] Full-screen maintained while dialog shown

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

### Test Case 9: Cancel Exit (Continue Exam) ✅
**Mục đích:** Test cancel exit flow

**Steps:**
1. Click [X] button to trigger exit dialog
2. Click "Tiếp Tục Thi" button

**Expected Results:**
- [ ] Dialog closes
- [ ] Return to exam screen
- [ ] Full-screen mode STILL ACTIVE
- [ ] Can continue answering questions
- [ ] Console log: "Exit cancelled, re-enabling full-screen"

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

### Test Case 10: Confirm Exit (Leave Exam) 🚪
**Mục đích:** Test proper shutdown flow

**Steps:**
1. Click [X] button to trigger exit dialog
2. Click "Thoát" button

**Expected Results:**
- [ ] Full-screen disabled
- [ ] Keyboard blocker uninstalled
- [ ] Services stopped gracefully
- [ ] Window closes
- [ ] Application exits
- [ ] Console logs:
   ```
   "Full-screen lock stopped"
   "Keyboard blocker uninstalled successfully"
   "Application closing..."
   ```

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

### Test Case 11: Submit Exam - Normal Exit 📝
**Mục đích:** Test exit sau khi submit exam

**Steps:**
1. Answer some questions
2. Click "Nộp Bài" button
3. Confirm submission
4. Wait for result screen
5. Click "Quay Lại Danh Sách" or close window

**Expected Results:**
- [ ] Full-screen disabled after submit
- [ ] Keyboard blocker uninstalled
- [ ] Can exit normally
- [ ] No confirmation dialog needed (already submitted)

**Actual Results:**
```
Ghi chú kết quả:
_________________________________
```

---

## 🐛 Known Limitations & Notes

### Platform Specific
1. **Keyboard Blocking:** Chỉ hoạt động trên **Windows**
   - MacOS/Linux: Full-screen lock vẫn hoạt động
   - Keyboard blocking sẽ gracefully fail (không crash app)

2. **Admin Privileges:** Không cần admin rights
   - Low-level hooks hoạt động với user privileges

### Security Notes
1. **Ctrl+Alt+Delete:** CANNOT block (by Windows design)
   - Đây là security feature của Windows
   - User vẫn có thể force logout

2. **Virtual Machines:** May behave differently
   - VM host shortcuts có thể bypass blocking

3. **Multiple Monitors:** 
   - Full-screen chỉ áp dụng cho primary monitor
   - Secondary monitors vẫn accessible

## 📊 Test Results Summary

| Test Case | Status | Notes |
|-----------|--------|-------|
| TC1: App Startup | ⬜ | |
| TC2: Login & Navigation | ⬜ | |
| TC3: Full-Screen Activation | ⬜ | |
| TC4: ESC Auto Re-enable | ⬜ | |
| TC5: Alt+F4 Blocking | ⬜ | Windows only |
| TC6: Alt+Tab Blocking | ⬜ | Windows only |
| TC7: Win Key Blocking | ⬜ | Windows only |
| TC8: Exit Dialog Shown | ⬜ | |
| TC9: Cancel Exit | ⬜ | |
| TC10: Confirm Exit | ⬜ | |
| TC11: Submit & Exit | ⬜ | |

**Legend:** ⬜ Not Tested | ✅ Pass | ❌ Fail | ⚠️ Issue

## 🔍 Debugging Tips

### View Console Logs
```bash
# Console sẽ hiển thị các logs quan trọng:
[INFO] Full-screen lock started
[INFO] Keyboard blocker installed successfully
[DEBUG] Blocking Alt+Tab
[DEBUG] Full-screen re-enabled
[INFO] Exit cancelled
[INFO] Full-screen lock stopped
```

### Common Issues

**Issue 1: Full-screen không activate**
```
Cause: Stage reference null
Fix: Verify ExamListController passes stage to ExamTakingController
Check: examTakingController.setStage(stage) được gọi
```

**Issue 2: Keyboard blocking không hoạt động**
```
Cause: JNA không load được hoặc không phải Windows
Solution: Check console cho error messages
Verify: JNA dependency trong pom.xml
```

**Issue 3: Cannot exit application**
```
Cause: Exit confirmation logic loop
Workaround: Use Task Manager to force close (testing only)
```

## 📝 Testing Checklist

Trước khi báo cáo kết quả:

- [ ] Backend server running
- [ ] Database có test data
- [ ] JavaFX client compiled successfully
- [ ] Đã test tất cả 11 test cases
- [ ] Document actual results cho mỗi test
- [ ] Note down any bugs/issues discovered
- [ ] Screenshots captured (nếu có issues)
- [ ] Console logs saved (nếu có errors)

## 🎯 Next Steps After Testing

1. **If All Tests Pass ✅:**
   - Proceed to Phase 8.6 Step 3
   - Begin polish & accessibility features

2. **If Issues Found ❌:**
   - Document bugs clearly
   - Create bug fix tasks
   - Fix critical bugs before proceeding

3. **Report Results:**
   - Update todo list with test status
   - Create test report document
   - Share findings with team

---

**Prepared by:** K24DTCN210-NVMANH  
**Version:** 1.0  
**Last Updated:** 24/11/2025 09:36

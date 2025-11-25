# Phase 8.6 Bước 4: Testing & Documentation Guide

**Ngày tạo:** 25/11/2025 09:57  
**Người thực hiện:** K24DTCN210-NVMANH

---

## 🎯 Mục Tiêu

Phase 8.6 Bước 4 là bước cuối cùng của Phase 8.6, tập trung vào:
1. **Manual E2E Testing** - Test toàn bộ flow từ login đến submit
2. **Build & Package** - Tạo executable JAR file
3. **Final Documentation** - Complete reports và testing guides

---

## 📋 Manual Testing Checklist

### Prerequisites
- ✅ Backend server đang chạy trên port 8080
- ✅ Database có test data (exams 103, 104 với user 7)
- ✅ Client compiled successfully (`mvn clean compile`)

### Test Scenario 1: Exit Confirmation ⚠️ CRITICAL

**Test Case 1.1: Close Window During Exam**
1. Login với user 7 (student7@example.com / password123)
2. Chọn exam và click "Bắt đầu làm bài"
3. Exam screen hiển thị với full-screen mode
4. **ACTION:** Click nút X (close window) trên titlebar
5. **EXPECTED:**
   - ✅ Confirmation dialog xuất hiện
   - ✅ Header: "⚠️ Bạn đang trong quá trình làm bài thi!"
   - ✅ Message warning về mất dữ liệu
   - ✅ 2 buttons: "Tiếp Tục Thi" (default) và "Thoát Ngay"
6. Click "Tiếp Tục Thi"
7. **EXPECTED:** Dialog đóng, exam tiếp tục bình thường

**Test Case 1.2: ESC Key During Exam**
1. Trong exam screen (sau khi start exam)
2. **ACTION:** Press ESC key
3. **EXPECTED:**
   - ✅ Same confirmation dialog như Test 1.1
   - ✅ ESC key được handled, không thoát app
4. Click "Thoát Ngay"
5. **EXPECTED:**
   - ✅ App cleanup (stop FullScreenLock, AutoSave, NetworkMonitor, Timer)
   - ✅ App thoát hoàn toàn

**Test Case 1.3: After Submit**
1. Hoàn thành exam và submit
2. Ở result screen, click X hoặc ESC
3. **EXPECTED:**
   - ✅ KHÔNG hiển thị confirmation (vì isExamActive = false)
   - ✅ App thoát bình thường

**Pass Criteria:**
- [ ] Confirmation dialog xuất hiện đúng timing
- [ ] Warning message rõ ràng
- [ ] Cleanup được thực hiện khi thoát
- [ ] KHÔNG hiển thị dialog khi exam đã kết thúc

---

### Test Scenario 2: Loading Indicators 🔄

**Test Case 2.1: Loading When Starting Exam**
1. Login successfully
2. Ở exam list, click "Bắt đầu làm bài"
3. **EXPECTED:**
   - ✅ Loading overlay xuất hiện ngay lập tức
   - ✅ Semi-transparent black background (rgba 0,0,0,0.6)
   - ✅ Spinning progress indicator
   - ✅ Label: "Đang tải câu hỏi..."
   - ✅ User KHÔNG thể click vào background
4. Đợi API response
5. **EXPECTED:**
   - ✅ Loading overlay biến mất
   - ✅ Exam screen hiển thị câu hỏi

**Test Case 2.2: Loading When Submitting**
1. Ở exam screen, click "Nộp bài"
2. Confirm submission trong dialog
3. **EXPECTED:**
   - ✅ Loading overlay xuất hiện
   - ✅ Label: "Đang nộp bài..."
   - ✅ User KHÔNG thể click vào background
4. Đợi API response
5. **EXPECTED:**
   - ✅ Loading overlay biến mất
   - ✅ Result screen hiển thị score

**Pass Criteria:**
- [ ] Loading overlay hiển thị đúng timing
- [ ] Spinner animation smooth
- [ ] Message text rõ ràng
- [ ] User interaction bị block khi loading

---

### Test Scenario 3: Keyboard Shortcuts ⌨️

**Test Case 3.1: Ctrl+S - Manual Save**
1. Trong exam screen, chọn một câu hỏi
2. Nhập answer (text/select option)
3. **ACTION:** Press Ctrl+S
4. **EXPECTED:**
   - ✅ API call POST /api/exam-taking/save-answer được trigger
   - ✅ Console log: "Saving answer..." (nếu có logging)
   - ✅ Answer được lưu thành công

**Test Case 3.2: Ctrl+N / Ctrl+P - Navigation**
1. Ở câu hỏi số 1
2. **ACTION:** Press Ctrl+N
3. **EXPECTED:** ✅ Jump to câu 2
4. **ACTION:** Press Ctrl+P
5. **EXPECTED:** ✅ Back to câu 1

**Test Case 3.3: Ctrl+M - Mark for Review**
1. Ở câu hỏi bất kỳ
2. **ACTION:** Press Ctrl+M
3. **EXPECTED:**
   - ✅ Question được mark (flag icon hoặc color change)
   - ✅ Question palette button update to "marked" status
4. **ACTION:** Press Ctrl+M again
5. **EXPECTED:** ✅ Unmark question

**Test Case 3.4: Number Keys 1-9**
1. Trong exam (có ít nhất 5 câu hỏi)
2. **ACTION:** Press "3" (number key)
3. **EXPECTED:** ✅ Jump to question 3
4. **ACTION:** Press "1"
5. **EXPECTED:** ✅ Jump to question 1

**Test Case 3.5: ESC - Exit Confirmation**
*(Đã test trong Scenario 1)*

**Pass Criteria:**
- [ ] Ctrl+S saves answer immediately
- [ ] Ctrl+N/P navigation works
- [ ] Ctrl+M toggles mark status correctly
- [ ] Number keys 1-9 jump to correct question
- [ ] ESC triggers exit confirmation

---

### Test Scenario 4: Accessibility 👁️

**Test Case 4.1: Tab Navigation**
1. Login screen, press Tab repeatedly
2. **EXPECTED:**
   - ✅ Focus moves logically: Email field → Password field → Login button
   - ✅ Focus indicator visible (blue border + glow)
3. Trong exam screen, press Tab
4. **EXPECTED:**
   - ✅ Focus moves through: Answer input → Previous button → Next button → Save button → Submit button → Question palette
   - ✅ Tab order logical và intuitive

**Test Case 4.2: Focus Indicators**
1. Tab qua các elements
2. **EXPECTED:**
   - ✅ Buttons: Blue 3px border
   - ✅ Text fields: Blue border + glow effect
   - ✅ Radio buttons: Blue border on .radio circle
   - ✅ Checkboxes: Blue border on .box
   - ✅ Question palette buttons: Blue border + enhanced glow

**Test Case 4.3: Keyboard-Only Exam Completion**
1. Login using keyboard only (Tab + Enter)
2. Start exam using keyboard (Tab to button + Enter)
3. Answer questions using keyboard:
   - Multiple choice: Arrow keys + Space
   - True/False: Arrow keys + Space
   - Text: Type directly
4. Navigate: Ctrl+N, Ctrl+P, number keys
5. Submit: Tab to Submit button + Enter
6. **EXPECTED:** ✅ Có thể hoàn thành toàn bộ exam flow mà không cần chuột

**Pass Criteria:**
- [ ] Tab order logical và complete
- [ ] Focus indicators rõ ràng trên mọi elements
- [ ] Có thể complete exam chỉ với keyboard
- [ ] Focus không bị "trap" ở bất kỳ element nào

---

### Test Scenario 5: Full Integration Test 🎯

**Complete Happy Path:**
1. Start backend: `cd backend && mvn spring-boot:run`
2. Start client: `cd client-javafx && mvn javafx:run`
3. Login với student7@example.com / password123
4. Exam list hiển thị exams 103, 104
5. Click "Bắt đầu làm bài" cho exam 103
6. Loading indicator → Exam screen full-screen
7. Answer 3-4 questions:
   - Test Multiple Choice
   - Test True/False
   - Test Essay (long text)
8. Test keyboard shortcuts:
   - Ctrl+S to save
   - Ctrl+N to next
   - Ctrl+M to mark
   - Number keys to jump
9. Try to exit (ESC) → Confirmation → Cancel
10. Continue exam, click "Nộp bài"
11. Confirm submission
12. Loading indicator → Result screen
13. Verify score displayed correctly
14. Click "Quay về danh sách" → Back to exam list

**Pass Criteria:**
- [ ] All steps complete without errors
- [ ] All features working together
- [ ] No console errors
- [ ] Performance smooth (< 100ms UI response)

---

## 🏗️ Build & Package

### Step 1: Clean Compile
```bash
cd client-javafx
mvn clean compile
```
**Expected:** BUILD SUCCESS

### Step 2: Run Tests (if any)
```bash
mvn test
```
**Expected:** All tests pass (or skip if no tests)

### Step 3: Package JAR
```bash
mvn clean package
```
**Expected:** 
- BUILD SUCCESS
- JAR file created: `target/exam-client-1.0.0.jar`

### Step 4: Verify JAR
```bash
java -jar target/exam-client-1.0.0.jar
```
**Expected:** Application launches successfully

---

## 📝 Testing Results Template

### Test Execution Summary

**Date:** 25/11/2025  
**Tester:** Cụ Mạnh  
**Version:** Phase 8.6 Complete

| Scenario | Test Cases | Pass | Fail | Notes |
|----------|-----------|------|------|-------|
| Exit Confirmation | 3 | _ | _ | |
| Loading Indicators | 2 | _ | _ | |
| Keyboard Shortcuts | 5 | _ | _ | |
| Accessibility | 3 | _ | _ | |
| Full Integration | 1 | _ | _ | |
| **Total** | **14** | **_** | **_** | |

### Issues Found

*(Document any bugs found during testing)*

1. **Issue #1:** [Title]
   - **Severity:** High/Medium/Low
   - **Description:** [What happened]
   - **Expected:** [What should happen]
   - **Steps to Reproduce:**
   - **Status:** Open/Fixed

### Performance Notes

- Loading time: ___ ms (target: < 2000ms)
- UI response time: ___ ms (target: < 100ms)
- Memory usage: ___ MB
- CPU usage during exam: ___%

---

## ✅ Sign-Off Checklist

**Before declaring Phase 8.6 complete:**

- [ ] All 14 test cases executed
- [ ] All critical issues resolved
- [ ] Build & package successful
- [ ] JAR file tested and working
- [ ] Documentation complete
- [ ] User guide created (if needed)

**Sign-off:**
- Tester: _________________ Date: _______
- Developer: K24DTCN210-NVMANH Date: 25/11/2025

---

## 📚 Additional Documentation

### Files Created in Phase 8.6:
1. ✅ `ExamClientApplication.java` - Main app
2. ✅ `LoginController.java` - Login logic
3. ✅ `FullScreenLockService.java` - Full-screen management
4. ✅ `KeyboardBlocker.java` - JNA keyboard blocking
5. ✅ `ExamTakingController.java` - Enhanced with exit confirmation, loading, shortcuts
6. ✅ `login.fxml` - Login layout
7. ✅ `exam-taking.fxml` - Enhanced with loading overlay
8. ✅ `exam-common.css` - Enhanced with loading + focus styles
9. ✅ `module-info.java` - Updated module configuration

### Documentation Files:
1. ✅ `PHASE8.6-STEP1-LOGIN-UI-TEST.md`
2. ✅ `PHASE8.6-STEP2-FULLSCREEN-COMPLETE.md`
3. ✅ `PHASE8.6-STEP2-MANUAL-TESTING-GUIDE.md`
4. ✅ `PHASE8.6-STEP3-EXIT-POLISH-COMPLETE.md`
5. ✅ `PHASE8.6-STEP4-TESTING-GUIDE.md` (this file)
6. ✅ 14+ bugfix completion reports

### Bug Fixes (Phase 8.6):
15+ bugs resolved and documented, including:
- TimerContainer type mismatch
- Missing methods
- NULL handling issues
- Field mapping issues
- API URL mismatches
- Transaction rollback issues
- And more...

---

**Prepared by:** K24DTCN210-NVMANH  
**Last Updated:** 25/11/2025 09:57

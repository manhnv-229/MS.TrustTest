# Phase 8.3 - Step 3B: Testing Guide 📋

**Date:** 23/11/2025 14:22  
**Status:** 📝 **TESTING DOCUMENTATION**

---

## 🎯 Testing Objectives

Verify rằng Phase 8.3 hoạt động đúng với:
1. ✅ Component functionality
2. ✅ Integration between screens
3. ✅ API communication
4. ✅ UI/UX experience
5. ✅ Error handling

---

## 🚀 Prerequisites

### 1. Backend Server Running
```bash
cd backend
mvn spring-boot:run
```

**Expected:** Server starts on `http://localhost:8080`

### 2. Test Data Available
- At least 1 exam with questions
- Test student account
- Exam với start/end time phù hợp

**Use:** `database/create-exam-with-questions-phase7.sql`

### 3. JavaFX Client Built
```bash
cd client-javafx
mvn clean compile
```

**Expected:** BUILD SUCCESS

---

## 📋 Testing Checklist

## Part 1: Component-Level Testing

### 1.1 TimerComponent ⏱️

#### Test Cases

**TC1: Timer Initialization**
- [ ] Timer hiển thị đúng thời gian ban đầu (HH:MM:SS)
- [ ] Màu xanh (GREEN) khi > 50% thời gian
- [ ] Timer bắt đầu đếm ngược ngay lập tức

**TC2: Color Phase Changes**
```
Initial: 60 phút → GREEN (100%)
After 30 min → GREEN (50%)
After 45 min → YELLOW (25%)
After 55 min → RED (8%)
```
- [ ] Màu chuyển từ GREEN → YELLOW at 50%
- [ ] Màu chuyển từ YELLOW → RED at 20%

**TC3: Warning Alerts**
- [ ] Alert hiện khi còn 10 phút
- [ ] Alert hiện khi còn 5 phút
- [ ] Alert hiện khi còn 1 phút

**TC4: Time Expiry**
- [ ] Timer đếm đến 00:00:00
- [ ] Auto-submit callback được gọi
- [ ] Dialog "Hết giờ" hiển thị

### 1.2 QuestionPaletteComponent 🎨

#### Test Cases

**TC5: Palette Display**
- [ ] All questions hiển thị trong grid
- [ ] Layout: 5 cột (hoặc responsive)
- [ ] Số thứ tự câu hỏi đúng (1, 2, 3...)

**TC6: Status Colors**
```
Initial: All WHITE (unanswered)
After answer Q1: Q1 = GREEN
After mark Q2: Q2 = ORANGE
Current Q3: Q3 = BLUE border
```
- [ ] Unanswered = White border
- [ ] Answered = Green background
- [ ] Marked = Orange background
- [ ] Current = Blue border

**TC7: Navigation**
- [ ] Click button 5 → Jump to question 5
- [ ] Click button 1 → Jump to question 1
- [ ] Click button 10 → Jump to question 10
- [ ] Current question highlights correctly

### 1.3 QuestionDisplayComponent 📝

#### Test Cases

**TC8: Question Display**
- [ ] Question number hiển thị (Câu 1, Câu 2...)
- [ ] Question content hiển thị đầy đủ
- [ ] Points hiển thị (nếu có)

**TC9: Answer Input Types**

Test với từng loại câu hỏi:

**MULTIPLE_CHOICE**
- [ ] RadioButton group hiển thị
- [ ] Chọn 1 option → Checked
- [ ] Chuyển câu → Answer preserved

**MULTIPLE_SELECT**
- [ ] CheckBox group hiển thị
- [ ] Chọn nhiều options → All checked
- [ ] Chuyển câu → Answers preserved

**TRUE_FALSE**
- [ ] 2 buttons (Đúng/Sai) hiển thị
- [ ] Click Đúng → Button active
- [ ] Chuyển câu → Selection preserved

**ESSAY**
- [ ] TextArea hiển thị
- [ ] Có thể nhập nhiều dòng
- [ ] Scroll nếu content dài
- [ ] Chuyển câu → Text preserved

**SHORT_ANSWER**
- [ ] TextField hiển thị
- [ ] Có thể nhập text
- [ ] Chuyển câu → Text preserved

**CODING** (If RichTextFX works)
- [ ] Code editor hiển thị
- [ ] Syntax highlighting (basic)
- [ ] Line numbers
- [ ] Chuyển câu → Code preserved

**FILL_IN_BLANK**
- [ ] Multiple TextFields
- [ ] Đúng số lượng blanks
- [ ] Chuyển câu → All values preserved

**MATCHING**
- [ ] ComboBox pairs hiển thị
- [ ] Select matches
- [ ] Chuyển câu → Matches preserved

**TC10: Mark for Review**
- [ ] Checkbox hiển thị
- [ ] Check → Status changes to MARKED
- [ ] Uncheck → Status changes back
- [ ] Status reflected in palette

### 1.4 AnswerInputFactory 🏭

#### Test Cases

**TC11: Factory Pattern**
- [ ] Correct widget cho MULTIPLE_CHOICE
- [ ] Correct widget cho MULTIPLE_SELECT
- [ ] Correct widget cho TRUE_FALSE
- [ ] Correct widget cho ESSAY
- [ ] Correct widget cho SHORT_ANSWER
- [ ] Correct widget cho CODING
- [ ] Correct widget cho FILL_IN_BLANK
- [ ] Correct widget cho MATCHING

**TC12: Answer Extraction**
- [ ] Can get answer from RadioButton
- [ ] Can get answer from CheckBox
- [ ] Can get answer from buttons
- [ ] Can get answer from TextArea
- [ ] Can get answer from TextField
- [ ] Can get answer from code editor
- [ ] Can get answer from multiple TextFields
- [ ] Can get answer from ComboBoxes

---

## Part 2: Controller-Level Testing

### 2.1 ExamTakingController 🎮

#### Test Cases

**TC13: Initialization**
- [ ] `initializeExam()` được gọi với đúng examId
- [ ] API call `POST /api/exam-taking/start/{examId}` thành công
- [ ] Questions được load từ API
- [ ] ExamSession được tạo với correct data
- [ ] All components được initialized

**TC14: Component Injection**
- [ ] TimerComponent được tạo và added to UI
- [ ] QuestionPaletteComponent được tạo và added
- [ ] QuestionDisplayComponent được tạo và added
- [ ] Components communicate correctly

**TC15: Navigation**

**Previous Button:**
- [ ] Question 5 → Click Previous → Question 4
- [ ] Question 1 → Previous button disabled
- [ ] Answer saved before navigation

**Next Button:**
- [ ] Question 1 → Click Next → Question 2
- [ ] Last question → Next button disabled
- [ ] Answer saved before navigation

**Jump from Palette:**
- [ ] Click palette 7 → Question 7 loads
- [ ] Click palette 1 → Question 1 loads
- [ ] Answer saved before jump

**TC16: Answer Saving**

**Manual Save:**
- [ ] Click "Lưu đáp án" button
- [ ] Button disabled during save
- [ ] API call `POST /api/exam-taking/save-answer` thành công
- [ ] Success feedback (brief)
- [ ] Button re-enabled
- [ ] Palette status updated

**Auto-Save:**
- [ ] Auto-save triggers sau 30s
- [ ] No UI blocking
- [ ] Background thread used
- [ ] Success/failure logged

**TC17: Submit Exam**
- [ ] Click "Nộp bài" button
- [ ] Confirmation dialog appears
- [ ] Dialog shows: unanswered count, warnings
- [ ] Confirm → API call `POST /api/exam-taking/submit`
- [ ] Success → Navigate to result screen
- [ ] Cancel → Stay on exam screen

**TC18: Time Expiry Auto-Submit**
- [ ] Timer reaches 00:00:00
- [ ] Auto-submit triggered
- [ ] Dialog: "Hết giờ - Bài thi đã tự động nộp"
- [ ] Exam submitted to API
- [ ] Navigate to result screen

---

## Part 3: Integration Testing

### 3.1 ExamListController → ExamTakingController

#### Test Cases

**TC19: Scene Navigation**
- [ ] User on Exam List screen
- [ ] Click "Bắt đầu làm bài"
- [ ] Confirmation dialog appears
- [ ] Confirm → Scene switches smoothly
- [ ] No errors in console

**TC20: Data Passing**
- [ ] ExamId passed correctly
- [ ] AuthToken passed correctly
- [ ] ExamTakingController receives data
- [ ] No data loss during transition

**TC21: Window State**
- [ ] Window title changes to "Làm bài thi: {exam title}"
- [ ] Window maximizes
- [ ] CSS stylesheet loads
- [ ] UI renders correctly

**TC22: Memory Management**
- [ ] Old scene resources released
- [ ] No memory leaks
- [ ] Smooth transition without lag

### 3.2 API Integration

#### Test Cases

**TC23: Start Exam API**
```
POST /api/exam-taking/start/{examId}
Headers: Authorization: Bearer {token}
```
- [ ] Returns submissionId
- [ ] Returns exam metadata
- [ ] Status code: 200 OK
- [ ] Error handling: 401, 404, 500

**TC24: Get Questions API**
```
GET /api/exam-taking/questions/{submissionId}
```
- [ ] Returns all questions
- [ ] Question order correct
- [ ] All fields populated
- [ ] Status code: 200 OK

**TC25: Save Answer API**
```
POST /api/exam-taking/save-answer/{submissionId}
Body: {
  "questionId": 1,
  "answer": "A",
  "markedForReview": false
}
```
- [ ] Answer saved to database
- [ ] Returns success response
- [ ] Status code: 200 OK
- [ ] Error handling: 400, 404

**TC26: Submit Exam API**
```
POST /api/exam-taking/submit/{submissionId}
```
- [ ] Exam submission recorded
- [ ] Status changed to SUBMITTED
- [ ] Returns result summary
- [ ] Status code: 200 OK

**TC27: Network Error Handling**
- [ ] Timeout → Retry dialog
- [ ] 401 Unauthorized → Re-login
- [ ] 404 Not Found → Error message
- [ ] 500 Server Error → Error message
- [ ] Network offline → Cached locally (future)

---

## Part 4: UI/UX Testing

### 4.1 Visual Design

#### Test Cases

**TC28: Layout**
- [ ] Timer visible in header
- [ ] Palette in sidebar (left)
- [ ] Question display in center
- [ ] Navigation buttons in footer
- [ ] Responsive to window resize

**TC29: Colors & Styling**
- [ ] Material Design colors consistent
- [ ] Primary color: #2196F3 (Blue)
- [ ] Success color: #4CAF50 (Green)
- [ ] Warning color: #FF9800 (Orange)
- [ ] Danger color: #F44336 (Red)
- [ ] Fonts readable at all sizes

**TC30: Hover Effects**
- [ ] Buttons darken on hover
- [ ] Palette buttons highlight on hover
- [ ] Smooth transitions
- [ ] Cursor changes to pointer

### 4.2 Interaction

#### Test Cases

**TC31: Loading States**
- [ ] Loading indicator during API calls
- [ ] Buttons disabled during processing
- [ ] No double-submission
- [ ] User knows when action complete

**TC32: Dialogs**
- [ ] Centered on screen
- [ ] Modal (blocks interaction)
- [ ] Clear buttons (OK/Cancel)
- [ ] Keyboard shortcuts (Enter/Esc)

**TC33: Focus Management**
- [ ] Tab key moves focus logically
- [ ] Enter key submits forms
- [ ] Esc key closes dialogs
- [ ] Focus visible (outline)

---

## Part 5: Error Handling

### 5.1 Error Scenarios

#### Test Cases

**TC34: FXML Load Failure**
- Rename exam-taking.fxml temporarily
- [ ] Error dialog shows
- [ ] User-friendly message
- [ ] No app crash

**TC35: API Timeout**
- Stop backend server
- [ ] Timeout after 30s
- [ ] Error message shows
- [ ] Retry option available

**TC36: Invalid Token**
- Use expired token
- [ ] 401 Unauthorized caught
- [ ] Redirect to login
- [ ] Session data preserved

**TC37: Exam Not Found**
- Use invalid examId
- [ ] 404 caught
- [ ] Error message clear
- [ ] Return to exam list

**TC38: Already Submitted**
- Try to submit twice
- [ ] Error message: "Đã nộp bài rồi"
- [ ] Cannot modify answers
- [ ] Show result screen instead

---

## 📊 Test Execution Template

### Test Session Info
```
Date: _______________
Tester: _______________
Environment: Local / Staging
Backend Version: _______________
Client Version: _______________
```

### Test Results
```
Component Tests:
- TimerComponent: __ / 4 passed
- QuestionPaletteComponent: __ / 3 passed
- QuestionDisplayComponent: __ / 3 passed
- AnswerInputFactory: __ / 2 passed

Controller Tests:
- ExamTakingController: __ / 6 passed

Integration Tests:
- Navigation: __ / 4 passed
- API Integration: __ / 5 passed

UI/UX Tests:
- Visual Design: __ / 3 passed
- Interaction: __ / 3 passed

Error Handling:
- Error Scenarios: __ / 5 passed

Total: __ / 38 passed (__%)
```

### Issues Found
```
1. Issue description
   - Severity: Critical / Major / Minor
   - Steps to reproduce
   - Expected vs Actual
   - Screenshot/Log

2. ...
```

---

## 🧪 Manual Testing Scenarios

### Scenario 1: Happy Path - Complete Exam

**Steps:**
1. Start backend server
2. Run JavaFX client
3. Login as student
4. See exam list
5. Click "Bắt đầu làm bài"
6. Confirm dialog
7. **Verify:** Exam screen loads
8. **Verify:** Timer starts counting down
9. **Verify:** Question 1 displayed
10. Answer question 1
11. Click "Lưu đáp án"
12. **Verify:** Success feedback
13. **Verify:** Palette Q1 = GREEN
14. Click "Next"
15. Answer question 2
16. Continue through all questions
17. Click "Nộp bài"
18. Confirm submission
19. **Verify:** Success message
20. **Verify:** Navigate to result screen

**Expected Result:** ✅ All verifications pass

### Scenario 2: Time Runs Out

**Steps:**
1. Create exam với duration = 2 phút
2. Start exam
3. **Verify:** Timer shows 02:00
4. Wait for timer...
5. At 01:00 → **Verify:** Warning alert
6. At 00:30 → **Verify:** RED color
7. At 00:00 → **Verify:** Auto-submit
8. **Verify:** Dialog "Hết giờ"
9. **Verify:** Exam submitted to API
10. **Verify:** Navigate to result

**Expected Result:** ✅ Auto-submit works

### Scenario 3: Network Error Recovery

**Steps:**
1. Start exam
2. Answer question 1
3. Stop backend server
4. Click "Lưu đáp án"
5. **Verify:** Error message
6. **Verify:** Answer cached locally (future feature)
7. Restart backend server
8. Click "Lưu đáp án" again
9. **Verify:** Success

**Expected Result:** ✅ Graceful error handling

---

## 🎯 Success Criteria

Phase 8.3 is considered COMPLETE when:

### Must Have ✅
- [ ] All components render correctly
- [ ] Timer counts down accurately
- [ ] Navigation works smoothly
- [ ] Answer saving works (manual + auto)
- [ ] Submit exam works
- [ ] Time expiry auto-submit works
- [ ] No critical bugs
- [ ] BUILD SUCCESS
- [ ] No console errors during normal use

### Should Have 🎯
- [ ] All 8 question types supported
- [ ] Palette status updates correctly
- [ ] Mark for review works
- [ ] Error messages user-friendly
- [ ] Loading states visible
- [ ] Performance acceptable (< 1s response)

### Nice to Have 🌟
- [ ] Keyboard shortcuts
- [ ] Accessibility features
- [ ] Offline mode (cache answers)
- [ ] Progress saving
- [ ] Resume exam feature

---

## 📝 Test Report Template

```markdown
# Phase 8.3 Testing Report

## Executive Summary
- Test Date: _______________
- Tester: _______________
- Overall Status: PASS / FAIL
- Pass Rate: __% (__/38 tests)

## Component Testing Results
[Details here]

## Integration Testing Results
[Details here]

## Issues Found
[List of bugs]

## Recommendations
[Improvements needed]

## Sign-off
Tested by: _______________
Approved by: _______________
Date: _______________
```

---

## 🚀 Next Steps After Testing

### If All Tests Pass ✅
1. Create Phase 8.3 completion report
2. Update progress documentation
3. Proceed to Phase 8.4 (Polish & Enhancement)

### If Issues Found ❌
1. Document all issues
2. Prioritize by severity
3. Fix critical issues first
4. Re-test after fixes
5. Repeat until all pass

---

**Created by:** K24DTCN210-NVMANH  
**Date:** 23/11/2025 14:22  
**Phase:** 8.3 - Step 3B  
**Purpose:** Comprehensive testing guide for Phase 8.3

**Note:** This is a MANUAL testing guide. Automated tests can be added in Phase 8.4+.

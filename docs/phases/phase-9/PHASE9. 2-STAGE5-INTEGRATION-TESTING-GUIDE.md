# Phase 9.2 - STAGE 5: Integration Testing Guide
## Exam Creation Wizard - Testing Documentation

**Created**: 28/11/2025 10:13  
**Author**: K24DTCN210-NVMANH  
**Status**: ✅ Integration Complete - Ready for Testing

---

## 📋 Overview

Phase 1 (Integration Setup) đã hoàn thành thành công:
- ✅ Added "Tạo đề thi" button to Teacher Main menu
- ✅ Implemented `handleCreateExam()` handler in `TeacherMainController`
- ✅ Integrated ExamManagementApiClient with wizard
- ✅ BUILD SUCCESS (71 files compiled)

**Files Modified**:
1. `client-javafx/src/main/resources/view/teacher-main.fxml`
2. `client-javafx/src/main/java/com/mstrust/client/teacher/controller/TeacherMainController.java`

---

## 🧪 Testing Phases

### PHASE 1: Basic Integration Testing ✅ COMPLETE

#### 1. 1 Compilation Test ✅
```bash
cd client-javafx
mvn clean compile
```
**Result**: ✅ BUILD SUCCESS - 71 source files compiled

#### 1.2 Menu Integration Test
**Steps to Test**:
1. Start backend server (if not running):
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. Launch JavaFX client:
   ```bash
   cd client-javafx
   mvn javafx:run
   ```

3. Login with teacher credentials:
   - Email: `teacher1@example.com`
   - Password: `password123`

4.  Verify menu structure:
   - [ ] "Tạo đề thi" button visible in left sidebar
   - [ ] Button positioned after "Quản lý Đề thi"
   - [ ] Icon ✨ displays correctly

5. Click "Tạo đề thi" button:
   - [ ] Modal wizard window opens
   - [ ] Window title: "Tạo đề thi mới - Wizard"
   - [ ] Window size: 900x700
   - [ ] Window is modal (blocks main window)

**Expected Result**: ✅ Wizard window opens in modal mode

---

### PHASE 2: Navigation Testing

#### 2.1 Initial State Test
**When wizard opens**:
- [ ] Progress bar shows "Bước 1/5"
- [ ] Progress value: 0. 2 (20%)
- [ ] Step 1 content (Basic Info) displays
- [ ] Previous button is DISABLED
- [ ] Next button is ENABLED
- [ ] Cancel button is ENABLED

#### 2.2 Next Button Test
**Steps**:
1. Fill in Step 1 required fields:
   - Title: "Đề thi Test"
   - Description: "Test description"
   - Select Subject Class
   - Select Exam Purpose
   - Select Exam Format
   - Set Start Time (future date)
   - Set End Time (after start time)

2. Click "Tiếp theo" button

**Expected**:
- [ ] Step 2 (Question Selection) loads
- [ ] Progress bar updates to "Bước 2/5" (40%)
- [ ] Previous button becomes ENABLED
- [ ] Data from Step 1 is retained

#### 2.3 Previous Button Test
**Steps**:
1. From Step 2, click "Quay lại" button

**Expected**:
- [ ] Returns to Step 1
- [ ] Progress bar back to "Bước 1/5" (20%)
- [ ] Previously entered data still visible
- [ ] Previous button becomes DISABLED again

#### 2.4 Full Navigation Test
**Test all 5 steps**:
- [ ] Step 1 → Step 2 navigation works
- [ ] Step 2 → Step 3 navigation works
- [ ] Step 3 → Step 4 navigation works
- [ ] Step 4 → Step 5 navigation works
- [ ] Can navigate backwards through all steps
- [ ] Progress bar updates correctly at each step

#### 2.5 Cancel Button Test
**Steps**:
1.  At any step, click "Hủy" button

**Expected**:
- [ ] Confirmation dialog appears
- [ ] If confirmed, wizard closes
- [ ] Returns to main teacher dashboard
- [ ] No data is saved

---

### PHASE 3: Data Persistence Testing

#### 3.1 Step 1 Data Retention
**Test**:
1. Fill Step 1 completely
2. Navigate to Step 2
3. Navigate back to Step 1

**Verify all fields retain values**:
- [ ] Title field
- [ ] Description field
- [ ] Subject Class selection
- [ ] Exam Purpose selection
- [ ] Exam Format selection
- [ ] Start Time date
- [ ] End Time date

#### 3.2 Step 2 Data Retention
**Test**:
1.  In Step 2, select 3-5 questions
2. Set points for each question
3. Navigate to Step 3
4. Navigate back to Step 2

**Verify**:
- [ ] Selected questions still in "Đã chọn" table
- [ ] Points values retained
- [ ] Question order retained
- [ ] Total points calculated correctly

#### 3. 3 Step 3 Data Retention
**Test**:
1. Set all settings in Step 3
2. Navigate to Step 4
3. Navigate back to Step 3

**Verify**:
- [ ] Duration Minutes spinner value
- [ ] Max Attempts spinner value
- [ ] Passing Score value
- [ ] Randomize Questions checkbox
- [ ] Show Review checkbox
- [ ] Allow Code Execution checkbox
- [ ] Monitoring Level selection
- [ ] Programming Language selection

#### 3.4 Step 4 Data Retention
**Test**:
1.  Assign 2-3 classes in Step 4
2. Navigate to Step 5
3. Navigate back to Step 4

**Verify**:
- [ ] Assigned classes still in "Đã gán" list
- [ ] Available classes list correct

#### 3.5 Cross-Step Data Flow
**Test complete data flow**:
1. Fill all 4 steps with data
2. Navigate to Step 5 (Review)
3.  Verify ALL data displays correctly in review:
   - [ ] Basic Info section
   - [ ] Questions section
   - [ ] Settings section
   - [ ] Classes section

---

### PHASE 4: Validation Testing

#### 4. 1 Step 1 Validation
**Test empty title**:
- [ ] Leave title empty, click Next
- [ ] Error message: "Tiêu đề không được để trống"
- [ ] Cannot proceed to Step 2

**Test short title**:
- [ ] Enter "AB" (< 3 chars), click Next
- [ ] Error message: "Tiêu đề phải từ 3-200 ký tự"

**Test invalid dates**:
- [ ] Set End Time before Start Time
- [ ] Error message: "Thời gian kết thúc phải sau thời gian bắt đầu"

**Test past dates**:
- [ ] Set Start Time in the past
- [ ] Error message about past dates

#### 4.2 Step 2 Validation
**Test no questions selected**:
- [ ] Don't select any questions, click Next
- [ ] Error message: "Vui lòng chọn ít nhất 1 câu hỏi"
- [ ] Cannot proceed to Step 3

**Test invalid points**:
- [ ] Set question points to 0 or negative
- [ ] Error message or validation prevents it

#### 4.3 Step 3 Validation
**Test invalid settings**:
- [ ] Set Duration to 0 minutes
- [ ] Set Max Attempts to 0
- [ ] Set Passing Score > Total Points
- [ ] Error messages display appropriately

#### 4.4 Step 4 Validation
**Test no classes assigned**:
- [ ] Don't assign any classes, click Next
- [ ] Error message: "Vui lòng gán ít nhất 1 lớp học"
- [ ] Cannot proceed to Step 5

---

### PHASE 5: API Integration Testing

**Prerequisites**:
- [ ] Backend server running on port 8080
- [ ] Database accessible
- [ ] Valid auth token from login

#### 5.1 Create Exam API Test
**Steps**:
1. Complete Steps 1-4 with valid data
2. Navigate to Step 5
3. Review all data
4. Click "Tạo đề thi" button

**Monitor console logs**:
- [ ] API call to `/api/exams` (POST)
- [ ] Request body contains correct data
- [ ] Response status: 201 Created
- [ ] Response contains examId

**Expected Result**:
- [ ] ProgressIndicator shows during API call
- [ ] Success message displays
- [ ] ExamId returned from API

#### 5.2 Add Questions API Test
**After exam created**:
- [ ] API call to `/api/exams/{examId}/questions` (POST)
- [ ] Request body contains question mappings
- [ ] Response status: 200 OK
- [ ] All questions added successfully

#### 5.3 Publish Exam API Test
**After questions added**:
- [ ] API call to `/api/exams/{examId}/publish` (POST)
- [ ] Response status: 200 OK
- [ ] Exam status changed to PUBLISHED

#### 5.4 Error Handling Test
**Test API failures**:
1. Stop backend server
2. Try to create exam

**Expected**:
- [ ] Error dialog displays
- [ ] Error message explains connection failure
- [ ] Wizard stays open
- [ ] Can retry after fixing connection

---

### PHASE 6: End-to-End Testing

#### 6.1 Complete Success Flow
**Test full wizard completion**:
1. Open wizard
2. Complete Step 1 (Basic Info)
3. Complete Step 2 (Select 5 questions)
4. Complete Step 3 (Set all settings)
5. Complete Step 4 (Assign 2 classes)
6. Review in Step 5
7. Submit

**Verify**:
- [ ] All steps complete without errors
- [ ] API calls succeed
- [ ] Success message displays
- [ ] Wizard closes automatically
- [ ] Return to main dashboard

#### 6.2 Database Verification
**After successful creation**:
```sql
-- Check exam created
SELECT * FROM exams ORDER BY id DESC LIMIT 1;

-- Check questions added
SELECT * FROM exam_questions WHERE exam_id = <examId>;

-- Check classes assigned
SELECT * FROM exam_class_assignments WHERE exam_id = <examId>;
```

**Verify**:
- [ ] Exam record exists
- [ ] Title, description correct
- [ ] Times correct
- [ ] Status = PUBLISHED
- [ ] All questions linked
- [ ] All classes assigned

#### 6.3 Multiple Scenarios Test
**Test with different data combinations**:
1. **Scenario A**: Practice exam, 10 questions, 1 class
   - [ ] Success

2. **Scenario B**: Midterm exam, 20 questions, 3 classes
   - [ ] Success

3. **Scenario C**: Final exam, 50 questions, all classes
   - [ ] Success

4. **Scenario D**: Quiz, 5 questions, randomized
   - [ ] Success

#### 6.4 Cancel at Different Steps
**Test cancel functionality**:
- [ ] Cancel at Step 1 - wizard closes, no data saved
- [ ] Cancel at Step 3 - wizard closes, no data saved
- [ ] Cancel at Step 5 before submit - wizard closes, no data saved

---

## 🐛 Known Issues & Bugs

### Issues to Watch For:
1. **NullPointerException** in controller initialization
2. **FXML loading errors** (path issues)
3. **API client** not receiving auth token
4. **Date/Time** formatting issues
5. **Progress bar** not updating
6. **Data loss** when navigating between steps

### How to Report Bugs:
1. Note the step where error occurred
2. Copy error message from console
3. Note the exact steps to reproduce
4. Check browser console (if applicable)

---

## 📊 Testing Checklist Summary

### Phase 1: Integration Setup ✅
- [x] Compilation successful
- [ ] Menu button visible
- [ ] Wizard opens on click

### Phase 2: Navigation
- [ ] Initial state correct
- [ ] Next button works all steps
- [ ] Previous button works all steps
- [ ] Progress bar updates
- [ ] Cancel button works

### Phase 3: Data Persistence
- [ ] Step 1 data retained
- [ ] Step 2 data retained
- [ ] Step 3 data retained
- [ ] Step 4 data retained
- [ ] Review displays all data

### Phase 4: Validation
- [ ] Step 1 validation works
- [ ] Step 2 validation works
- [ ] Step 3 validation works
- [ ] Step 4 validation works

### Phase 5: API Integration
- [ ] Create exam API works
- [ ] Add questions API works
- [ ] Publish exam API works
- [ ] Error handling works

### Phase 6: End-to-End
- [ ] Complete success flow
- [ ] Database verification
- [ ] Multiple scenarios
- [ ] Cancel functionality

---

## 🎯 Success Criteria

The integration is considered **SUCCESSFUL** when:
1. ✅ All compilation passes
2. ✅ Wizard opens from menu
3. ✅ All 5 steps navigable
4. ✅ Data persists across navigation
5. ✅ All validations work
6. ✅ All 3 API calls succeed
7. ✅ Exam created in database
8. ✅ No critical bugs found

---

## 📝 Next Steps After Testing

1. **If all tests pass**:
   - Mark STAGE 5 as COMPLETE
   - Create completion report
   - Move to Phase 9.3 (if applicable)

2. **If bugs found**:
   - Document all bugs
   - Prioritize fixes
   - Fix critical bugs
   - Re-test

3. **Enhancement opportunities**:
   - Add loading indicators
   - Improve error messages
   - Add tooltips
   - Enhance UI/UX

---

## 🚀 Quick Start Commands

```bash
# Terminal 1: Start Backend
cd backend
mvn spring-boot:run

# Terminal 2: Start JavaFX Client
cd client-javafx
mvn javafx:run

# Login Credentials
Email: teacher1@example.com
Password: password123
```

---

**Testing Notes**:
- Test on clean database first
- Test with existing data
- Test with invalid data
- Test network failures
- Test boundary conditions

**Happy Testing!** 🎉

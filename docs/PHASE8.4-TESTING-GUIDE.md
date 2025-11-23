# Phase 8.4: Auto-Save & Network Recovery - TESTING GUIDE

**Created:** 23/11/2025 18:40  
**Author:** K24DTCN210-NVMANH

---

## 🎯 Testing Objectives

Phase 8.4 cần test 5 service classes và tích hợp vào ExamTakingController. Các test cases bao gồm:

1. **Auto-Save Service** - Periodic + debounced save
2. **Answer Queue** - Thread-safe operations + persistence
3. **Network Monitor** - Connection detection
4. **Recovery Service** - Reconnection logic
5. **Local Storage** - JSON file operations

---

## ⚠️ LƯU Ý QUAN TRỌNG

**Phase 8.4 CHỈ TẠO SERVICES - CHƯA CÓ UI ĐỂ TEST!**

Hiện tại:
- ✅ Services đã được tạo
- ✅ BUILD SUCCESS
- ❌ **CHƯA CÓ UI để user tương tác**
- ❌ ExamTakingController chưa được load (Phase 8.5 mới hoàn thiện)

**ĐỂ TEST ĐƯỢC Phase 8.4, CẦN HOÀN THÀNH Phase 8.5 trước!**

---

## 📋 Current Testing Status

### What CAN Be Tested Now (Code Level)
✅ **Compilation** - mvn clean compile  
✅ **Class files** - Verify .class files exist  
✅ **Static analysis** - Check code structure  

### What CANNOT Be Tested Yet (Requires UI)
❌ Auto-save behavior (cần UI input)  
❌ Network disconnect (cần running app)  
❌ Queue persistence (cần app lifecycle)  
❌ User interaction (cần complete exam flow)  

---

## 🔧 Test 1: Compilation Verification ✅

**Objective:** Verify all services compile successfully

```powershell
# Navigate to client-javafx
cd client-javafx

# Clean and compile
mvn clean compile

# Expected output:
# [INFO] BUILD SUCCESS
# [INFO] Compiling 33 source files
```

**Success Criteria:**
- ✅ BUILD SUCCESS
- ✅ No compilation errors
- ✅ All .class files generated

**Verify Class Files Exist:**
```powershell
Test-Path client-javafx/target/classes/com/mstrust/client/exam/service/AutoSaveService.class
# Should return: True

Test-Path client-javafx/target/classes/com/mstrust/client/exam/service/AnswerQueue.class
# Should return: True

Test-Path client-javafx/target/classes/com/mstrust/client/exam/service/NetworkMonitor.class
# Should return: True

Test-Path client-javafx/target/classes/com/mstrust/client/exam/service/ConnectionRecoveryService.class
# Should return: True

Test-Path client-javafx/target/classes/com/mstrust/client/exam/service/LocalStorageService.class
# Should return: True
```

---

## 🔧 Test 2: Code Structure Verification ✅

**Objective:** Verify classes có đúng methods và structure

### Check AutoSaveService
```powershell
Get-Content client-javafx/src/main/java/com/mstrust/client/exam/service/AutoSaveService.java | Select-String "public void start"
Get-Content client-javafx/src/main/java/com/mstrust/client/exam/service/AutoSaveService.java | Select-String "public void stop"
Get-Content client-javafx/src/main/java/com/mstrust/client/exam/service/AutoSaveService.java | Select-String "public void onAnswerChanged"
```

**Expected:** All 3 methods found

### Check AnswerQueue
```powershell
Get-Content client-javafx/src/main/java/com/mstrust/client/exam/service/AnswerQueue.java | Select-String "public void enqueue"
Get-Content client-javafx/src/main/java/com/mstrust/client/exam/service/AnswerQueue.java | Select-String "public List<QueuedAnswer> dequeue"
Get-Content client-javafx/src/main/java/com/mstrust/client/exam/service/AnswerQueue.java | Select-String "public int getPendingCount"
```

**Expected:** All 3 methods found

---

## 🚫 Tests KHÔNG THỂ CHẠY (Cần Phase 8.5)

### ❌ Test 3: Auto-Save Periodic (30s)
**Why Cannot Test:** Cần running JavaFX app với exam session active

**What Would Be Tested:**
1. Start exam → Auto-save service starts
2. Wait 30 seconds → Service automatically saves
3. Check backend → Answer saved to DB
4. Verify logs → "Auto-save completed" message

**Required:** Complete exam flow từ ExamListController → ExamTakingController

---

### ❌ Test 4: Debounced Save (3s)
**Why Cannot Test:** Cần UI input fields để trigger onAnswerChanged()

**What Would Be Tested:**
1. User types answer → onAnswerChanged() called
2. Wait < 3s, type more → Previous save cancelled
3. Wait 3s → Debounced save executed
4. Verify backend → Answer saved once (not multiple times)

**Required:** AnswerInputFactory widgets hooked to onAnswerChanged callback

---

### ❌ Test 5: Network Disconnection Detection
**Why Cannot Test:** Cần running app với NetworkMonitor active

**What Would Be Tested:**
1. Start exam → NetworkMonitor starts
2. Stop backend server → Monitor detects disconnect
3. Check UI → Warning overlay appears
4. Check logs → "Network disconnected" message

**Required:** Running JavaFX app + UI overlay component

---

### ❌ Test 6: Automatic Reconnection
**Why Cannot Test:** Cần running app với ConnectionRecoveryService

**What Would Be Tested:**
1. Network disconnected → Queue accumulates answers
2. Restart backend → Monitor detects reconnect
3. Recovery service → Flushes queued answers
4. Check UI → Success message
5. Check backend → All answers saved

**Required:** Complete service integration + UI feedback

---

### ❌ Test 7: Queue Persistence (JSON)
**Why Cannot Test:** Cần app lifecycle (start/stop/crash)

**What Would Be Tested:**
1. Answer questions → Queue has pending answers
2. Kill app forcefully → exam_queue.json created
3. Restart app → Queue restored from JSON
4. Check backend → Answers saved on reconnect

**Required:** Running app that can be stopped/restarted

---

### ❌ Test 8: Concurrent Save Handling
**Why Cannot Test:** Cần multiple rapid answer changes

**What Would Be Tested:**
1. Rapid answer changes → Multiple onAnswerChanged() calls
2. Queue handling → Thread-safe operations
3. Backend API → Concurrent saves handled
4. Verify data → No race conditions, no data loss

**Required:** UI with multiple input widgets

---

## 📝 RECOMMENDED TESTING APPROACH

### Phase 1: Static Verification (NOW - Phase 8.4) ✅
- [x] Compilation successful
- [x] Class files exist
- [x] Code structure correct
- [x] Comments complete
- [x] Integration hooks in place

### Phase 2: Integration Testing (Phase 8.5) 🔄
**Prerequisites:**
- Complete ExamTakingController initialization
- Add Submit exam functionality  
- Connect all UI callbacks

**What Can Be Tested:**
1. ✅ Start exam → Services initialize
2. ✅ Type answers → Debounced save triggers
3. ✅ Wait 30s → Periodic save executes
4. ✅ Network status → Monitor working
5. ✅ Queue operations → Thread-safe behavior

### Phase 3: End-to-End Testing (Phase 8.6) 🔄
**Full Exam Flow:**
1. Login → Exam list
2. Start exam → ExamTakingController loads
3. Answer questions → Auto-save working
4. Test disconnect → Queue accumulates
5. Test reconnect → Queue flushes
6. Submit exam → Final save
7. View results → Complete

---

## 🎯 Manual Testing Checklist (When Phase 8.5 Complete)

### Setup
- [ ] Backend server running (mvn spring-boot:run)
- [ ] Test data loaded (User 7 enrolled in exams)
- [ ] Database clean (no previous submissions)
- [ ] Client app ready to run

### Test Scenario 1: Normal Auto-Save
- [ ] Start exam
- [ ] Answer question 1
- [ ] Wait 3 seconds → Check logs for "Debounced save"
- [ ] Wait 30 seconds → Check logs for "Periodic save"
- [ ] Check backend → Answers saved to DB
- [ ] Navigate to question 2
- [ ] Repeat verification

**Expected:**
- ✅ Debounced save after 3s idle
- ✅ Periodic save every 30s
- ✅ No data loss
- ✅ Backend has all answers

### Test Scenario 2: Network Disconnect
- [ ] Start exam, answer 5 questions
- [ ] Stop backend server (Ctrl+C)
- [ ] Continue answering questions 6-10
- [ ] Check UI → Warning overlay visible
- [ ] Check logs → "Network disconnected" message
- [ ] Check queue → 5 answers pending

**Expected:**
- ✅ Warning shown immediately
- ✅ Queue accumulates answers
- ✅ App doesn't crash
- ✅ User can continue working

### Test Scenario 3: Reconnection & Recovery
- [ ] (Continue from Scenario 2)
- [ ] Restart backend server
- [ ] Wait 10 seconds
- [ ] Check logs → "Network reconnected"
- [ ] Check logs → "Flushing queue"
- [ ] Check UI → Success message
- [ ] Check backend → All 10 answers saved

**Expected:**
- ✅ Reconnect detected automatically
- ✅ Queue flushed successfully
- ✅ All data preserved
- ✅ User notified

### Test Scenario 4: Queue Persistence (Crash Recovery)
- [ ] Start exam, answer 5 questions
- [ ] Stop backend (simulate disconnect)
- [ ] Answer 5 more questions (queue = 5)
- [ ] Kill app forcefully (Task Manager / kill process)
- [ ] Check file → exam_queue.json exists
- [ ] Restart backend
- [ ] Restart app, login, start same exam
- [ ] Check logs → "Queue restored from JSON"
- [ ] Wait for reconnect
- [ ] Check backend → All 10 answers saved

**Expected:**
- ✅ JSON file created on queue update
- ✅ Queue restored on app restart
- ✅ No data loss on crash
- ✅ Automatic flush after restart

---

## 🐛 Common Issues & Solutions

### Issue 1: Services Not Starting
**Symptom:** No auto-save logs, no network monitoring

**Check:**
```java
// In ExamTakingController.initializeExam()
initializeAutoSaveServices(); // This line exists?
```

**Solution:** Ensure initializeAutoSaveServices() is called in initializeExam()

---

### Issue 2: Queue Not Persisting
**Symptom:** No exam_queue.json file created

**Check:**
```java
// In AnswerQueue.enqueue()
storage.persist(queue); // This line exists?
```

**Solution:** Ensure LocalStorageService.persist() is called on every enqueue()

---

### Issue 3: Network Monitor Not Working
**Symptom:** No logs when backend stops

**Check Backend Health Endpoint:**
```powershell
curl http://localhost:8080/api/health
# Should return 200 OK when backend running
```

**Solution:** 
1. Ensure backend has `/api/health` endpoint
2. Check NetworkMonitor.start() is called
3. Verify 10-second interval is correct

---

### Issue 4: Debounced Save Not Working
**Symptom:** Save happens immediately, not after 3s

**Check:**
```java
// In AutoSaveService
private ScheduledFuture<?> debounceFuture;

if (debounceFuture != null) {
    debounceFuture.cancel(false); // Cancel previous
}
```

**Solution:** Ensure debounce logic cancels previous scheduled task

---

## 📊 Success Metrics

### Code Quality Metrics ✅
- [x] BUILD SUCCESS
- [x] Zero compilation errors
- [x] All comments complete
- [x] Methods follow naming conventions
- [x] Error handling implemented

### Integration Metrics (Phase 8.5)
- [ ] Services start successfully
- [ ] No null pointer exceptions
- [ ] Thread-safe operations
- [ ] Memory usage stable
- [ ] Performance < 100ms overhead

### Functional Metrics (Phase 8.5+)
- [ ] Auto-save works 100% of time
- [ ] Network detection < 10s
- [ ] Reconnect successful > 95% cases
- [ ] No data loss in any scenario
- [ ] Queue persistence works always

---

## 🚀 Next Steps

### To Enable Testing:

1. **Complete Phase 8.5: Submit & Results**
   - Implement submit exam functionality
   - Add result display screen
   - Connect all UI callbacks

2. **Add Logging/Debugging**
   ```java
   logger.info("Auto-save started");
   logger.info("Debounced save triggered for question {}", questionId);
   logger.info("Periodic save completed");
   logger.info("Network disconnected");
   logger.info("Queue size: {}", queue.getPendingCount());
   ```

3. **Create Test Script**
   - Automated test scenarios
   - Mock network failures
   - Verify queue behavior
   - Check JSON persistence

4. **UI Indicators**
   - Save status icon (saving/saved/failed)
   - Connection status badge
   - Pending queue counter
   - Last save timestamp

---

## 📝 Testing Report Template

```markdown
# Phase 8.4 Testing Report

**Date:** DD/MM/YYYY
**Tester:** Name
**Environment:** Windows 10, JDK 17, Maven 3.9

## Test Results

### Compilation Tests ✅
- BUILD SUCCESS: Yes/No
- Class files: All exist / Missing: [list]
- Errors: None / [list errors]

### Integration Tests (Phase 8.5)
- Services start: Yes/No
- Auto-save working: Yes/No
- Network monitor: Yes/No
- Queue persistence: Yes/No

### Functional Tests (Phase 8.6)
- Normal flow: Pass/Fail
- Disconnect scenario: Pass/Fail
- Reconnect scenario: Pass/Fail
- Crash recovery: Pass/Fail

## Issues Found
1. [Issue description]
   - Severity: Critical/High/Medium/Low
   - Steps to reproduce: [...]
   - Expected: [...]
   - Actual: [...]

## Recommendations
- [Recommendation 1]
- [Recommendation 2]
```

---

## ✅ Current Status

**Phase 8.4:** ✅ COMPLETE (Code Level)
- Services created
- Integration hooks ready
- BUILD SUCCESS
- **Waiting for Phase 8.5 to enable runtime testing**

**Next:** Complete Phase 8.5 → Full testing possible

---

*Document Created: 23/11/2025 18:40*  
*Author: K24DTCN210-NVMANH*

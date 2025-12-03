# Manual Test Constraint Fix

## Option 1: Apply Database Fix

### Cách 1: Sử dụng MySQL Workbench/phpMyAdmin
Copy và execute từng command trong `database/fix-exam-submissions-constraint-simple.sql`:

```sql
USE ms_trusttest;

DROP INDEX uk_exam_student ON exam_submissions;

ALTER TABLE exam_submissions 
ADD CONSTRAINT uk_exam_student_attempt 
UNIQUE (exam_id, student_id, attempt_number);
```

### Cách 2: Sử dụng command line (nếu có MySQL client)
```bash
mysql -u root -p ms_trusttest < database/fix-exam-submissions-constraint-simple.sql
```

## Option 2: Test Application Logic Fix (No Database Change Needed)

Application đã được cải thiện để handle constraint violation automatically! 

### Test Steps:

1. **Start server** (nếu chưa chạy):
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Test với Thunder Client hoặc Postman:**
   ```
   POST http://localhost:8080/api/exam-taking/start/5
   Authorization: Bearer <student_token>
   ```

3. **Expected Behavior:**
   - Nếu có constraint violation → Application sẽ detect và handle
   - Tạo submission mới với attempt_number = 2
   - Hoặc return existing active submission
   - **KHÔNG CÒN LỖI "Duplicate entry"**

### Monitor Logs:
Application sẽ log:
```
[StartExam] UK constraint violation - constraint uk_exam_student is incorrectly designed... 
[StartExam] Successfully created submission X with attempt 2
```

## Verification:

Sau khi test thành công, check database:

```sql
SELECT exam_id, student_id, attempt_number, status, started_at 
FROM exam_submissions 
WHERE exam_id = 5 AND student_id = 5
ORDER BY attempt_number;
```

**Expected:** Thấy 2 records (attempt 1 và attempt 2) thay vì lỗi constraint violation! 

---

**Note:** Application logic fix hoạt động ngay cả khi constraint chưa được sửa. Đây là robust workaround solution!

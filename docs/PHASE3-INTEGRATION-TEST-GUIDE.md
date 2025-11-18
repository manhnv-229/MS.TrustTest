# Phase 3 Integration Test Guide
## MS.TrustTest - Organization Management Modules

**Tạo bởi:** NVMANH with Cline  
**Ngày tạo:** 15/11/2025 15:07  
**Phase:** 3 - Step 6: Integration Testing

---

## 1. Tổng Quan

Tài liệu này hướng dẫn test tích hợp cho 5 modules chính của Phase 3:
1. **Department Module** (9 endpoints)
2. **Class Module** (15 endpoints)
3. **Subject Module** (9 endpoints)
4. **SubjectClass Module** (15 endpoints)
5. **User Management Enhancement** (13 endpoints)

**Tổng số endpoints cần test:** ~61 endpoints

---

## 2. Thông Tin Môi Trường

### Application Status
- ✅ **Status:** Running
- ✅ **Base URL:** `http://localhost:8080/api`
- ✅ **Database:** MySQL 8.0 - Connected
- ✅ **Authentication:** JWT Token Required
- ✅ **Context Path:** `/api`

### Test Tools
- **API Client:** Thunder Client / Postman
- **Database:** MySQL Workbench (optional)
- **Documentation:** Swagger UI at `http://localhost:8080/api/swagger-ui.html`

---

## 3. Authentication Setup

### Step 1: Login để lấy JWT Token

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "identifier": "admin@example.com",
  "password": "Admin@123"
}
```

**Response Success:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "admin@example.com",
    "fullName": "System Admin",
    "roles": ["ROLE_ADMIN"]
  }
}
```

**Lưu ý:** 
- Copy token từ response
- Sử dụng token này cho tất cả các requests tiếp theo
- Header format: `Authorization: Bearer {token}`

---

## 4. Test Cases By Module

### 4.1. Department Module (9 endpoints)

#### Base URL: `/api/departments`

#### Test Case 1: Create Department
**Method:** POST `/api/departments`  
**Headers:** `Authorization: Bearer {token}`  
**Body:**
```json
{
  "name": "Khoa Công Nghệ Thông Tin",
  "code": "CNTT",
  "description": "Khoa đào tạo về IT"
}
```
**Expected:** Status 200, response chứa department ID

#### Test Case 2: Get All Departments
**Method:** GET `/api/departments`  
**Headers:** `Authorization: Bearer {token}`  
**Expected:** Status 200, array of departments

#### Test Case 3: Get Department by ID
**Method:** GET `/api/departments/{id}`  
**Headers:** `Authorization: Bearer {token}`  
**Expected:** Status 200, department details

#### Test Case 4: Update Department
**Method:** PUT `/api/departments/{id}`  
**Headers:** `Authorization: Bearer {token}`  
**Body:**
```json
{
  "name": "Khoa CNTT Updated",
  "description": "Mô tả mới"
}
```
**Expected:** Status 200, updated department

#### Test Case 5: Search Departments
**Method:** GET `/api/departments/search?name=CNTT`  
**Headers:** `Authorization: Bearer {token}`  
**Expected:** Status 200, filtered departments

#### Test Case 6: Get Department Statistics
**Method:** GET `/api/departments/{id}/statistics`  
**Headers:** `Authorization: Bearer {token}`  
**Expected:** Status 200, statistics object

#### Test Case 7: Soft Delete Department
**Method:** DELETE `/api/departments/{id}`  
**Headers:** `Authorization: Bearer {token}`  
**Expected:** Status 204

#### Test Case 8: Get Active Departments Only
**Method:** GET `/api/departments?status=ACTIVE`  
**Headers:** `Authorization: Bearer {token}`  
**Expected:** Status 200, only active departments

#### Test Case 9: Test Duplicate Prevention
**Method:** POST `/api/departments`  
**Headers:** `Authorization: Bearer {token}`  
**Body:** (Same code as existing)
```json
{
  "name": "Test Duplicate",
  "code": "CNTT",
  "description": "Should fail"
}
```
**Expected:** Status 400, error message about duplicate

---

### 4.2. Class Module (15 endpoints)

#### Base URL: `/api/classes`

#### Test Case 1: Create Class
**Method:** POST `/api/classes`  
**Headers:** `Authorization: Bearer {token}`  
**Body:**
```json
{
  "name": "Lớp DHTI15A1HN",
  "code": "DHTI15A1HN",
  "departmentId": 1,
  "academicYear": 2024
}
```
**Expected:** Status 200, class created

#### Test Case 2: Get All Classes
**Method:** GET `/api/classes`  
**Expected:** Status 200, array of classes

#### Test Case 3: Get Class by ID
**Method:** GET `/api/classes/{id}`  
**Expected:** Status 200, class details

#### Test Case 4: Update Class
**Method:** PUT `/api/classes/{id}`  
**Body:**
```json
{
  "name": "Lớp DHTI15A1HN Updated",
  "academicYear": 2025
}
```
**Expected:** Status 200, updated class

#### Test Case 5: Get Classes by Department
**Method:** GET `/api/classes/by-department/{departmentId}`  
**Expected:** Status 200, classes of that department

#### Test Case 6: Search Classes
**Method:** GET `/api/classes/search?name=DHTI`  
**Expected:** Status 200, filtered classes

#### Test Case 7: Get Class Statistics
**Method:** GET `/api/classes/{id}/statistics`  
**Expected:** Status 200, statistics

#### Test Case 8: Add Student to Class
**Method:** POST `/api/classes/{id}/students/{studentId}`  
**Expected:** Status 200, student added

#### Test Case 9: Remove Student from Class
**Method:** DELETE `/api/classes/{id}/students/{studentId}`  
**Expected:** Status 204

#### Test Case 10: Get Students in Class
**Method:** GET `/api/classes/{id}/students`  
**Expected:** Status 200, list of students

#### Test Case 11: Get Classes by Academic Year
**Method:** GET `/api/classes/by-year/{year}`  
**Expected:** Status 200, classes of that year

#### Test Case 12: Soft Delete Class
**Method:** DELETE `/api/classes/{id}`  
**Expected:** Status 204

#### Test Case 13-15: Error Cases
- Test invalid department ID
- Test duplicate class code
- Test adding non-existent student

---

### 4.3. Subject Module (9 endpoints)

#### Base URL: `/api/subjects`

#### Test Case 1: Create Subject
**Method:** POST `/api/subjects`  
**Body:**
```json
{
  "name": "Lập Trình Java",
  "code": "PRJ301",
  "credits": 3,
  "departmentId": 1,
  "description": "Môn học về Java programming"
}
```
**Expected:** Status 200, subject created

#### Test Case 2: Get All Subjects
**Method:** GET `/api/subjects`  
**Expected:** Status 200, array of subjects

#### Test Case 3: Get Subject by ID
**Method:** GET `/api/subjects/{id}`  
**Expected:** Status 200, subject details

#### Test Case 4: Update Subject
**Method:** PUT `/api/subjects/{id}`  
**Body:**
```json
{
  "name": "Lập Trình Java Nâng Cao",
  "credits": 4
}
```
**Expected:** Status 200, updated subject

#### Test Case 5: Get Subjects by Department
**Method:** GET `/api/subjects/by-department/{departmentId}`  
**Expected:** Status 200, subjects of department

#### Test Case 6: Search Subjects
**Method:** GET `/api/subjects/search?name=Java`  
**Expected:** Status 200, filtered subjects

#### Test Case 7: Get Subject Statistics
**Method:** GET `/api/subjects/{id}/statistics`  
**Expected:** Status 200, statistics

#### Test Case 8: Soft Delete Subject
**Method:** DELETE `/api/subjects/{id}`  
**Expected:** Status 204

#### Test Case 9: Test Foreign Key Constraint
**Method:** POST `/api/subjects`  
**Body:** (Invalid department ID)
```json
{
  "name": "Test Subject",
  "code": "TST999",
  "credits": 3,
  "departmentId": 9999
}
```
**Expected:** Status 400, error about invalid department

---

### 4.4. SubjectClass Module (15 endpoints)

#### Base URL: `/api/subject-classes`

#### Test Case 1: Create SubjectClass
**Method:** POST `/api/subject-classes`  
**Body:**
```json
{
  "subjectId": 1,
  "semester": "Fall2024",
  "year": 2024,
  "teacherId": 2,
  "roomNumber": "A101",
  "schedule": "Mon 8:00-10:00"
}
```
**Expected:** Status 200, subject class created

#### Test Case 2: Get All SubjectClasses
**Method:** GET `/api/subject-classes`  
**Expected:** Status 200, array of subject classes

#### Test Case 3: Get SubjectClass by ID
**Method:** GET `/api/subject-classes/{id}`  
**Expected:** Status 200, subject class details with students

#### Test Case 4: Update SubjectClass
**Method:** PUT `/api/subject-classes/{id}`  
**Body:**
```json
{
  "roomNumber": "A102",
  "schedule": "Tue 8:00-10:00"
}
```
**Expected:** Status 200, updated subject class

#### Test Case 5: Enroll Student
**Method:** POST `/api/subject-classes/{id}/students/{studentId}`  
**Expected:** Status 200, student enrolled

#### Test Case 6: Update Enrollment Status
**Method:** PUT `/api/subject-classes/{id}/students/{studentId}/status`  
**Body:**
```json
{
  "status": "COMPLETED"
}
```
**Expected:** Status 200, status updated

#### Test Case 7: Remove Student (Drop)
**Method:** DELETE `/api/subject-classes/{id}/students/{studentId}`  
**Expected:** Status 204

#### Test Case 8: Get Students in SubjectClass
**Method:** GET `/api/subject-classes/{id}/students`  
**Expected:** Status 200, list of enrolled students

#### Test Case 9: Get SubjectClasses by Subject
**Method:** GET `/api/subject-classes/by-subject/{subjectId}`  
**Expected:** Status 200, classes of that subject

#### Test Case 10: Get SubjectClasses by Semester
**Method:** GET `/api/subject-classes/by-semester/{semester}/{year}`  
**Expected:** Status 200, classes of that semester

#### Test Case 11: Get SubjectClasses by Teacher
**Method:** GET `/api/subject-classes/by-teacher/{teacherId}`  
**Expected:** Status 200, classes taught by teacher

#### Test Case 12: Get SubjectClass Statistics
**Method:** GET `/api/subject-classes/{id}/statistics`  
**Expected:** Status 200, enrollment statistics

#### Test Case 13: Soft Delete SubjectClass
**Method:** DELETE `/api/subject-classes/{id}`  
**Expected:** Status 204

#### Test Case 14-15: Error Cases
- Test duplicate enrollment
- Test enrolling non-existent student

---

### 4.5. User Management Enhancement (13 endpoints)

#### Base URL: `/api/users`

#### Test Case 1: Assign Role to User
**Method:** POST `/api/users/{id}/roles`  
**Body:**
```json
{
  "roleId": 2
}
```
**Expected:** Status 200, role assigned

#### Test Case 2: Remove Role from User
**Method:** DELETE `/api/users/{id}/roles/{roleId}`  
**Expected:** Status 204

#### Test Case 3: Get User's Roles
**Method:** GET `/api/users/{id}/roles`  
**Expected:** Status 200, list of roles

#### Test Case 4: Assign Department to User
**Method:** PUT `/api/users/{id}/department/{departmentId}`  
**Expected:** Status 200, department assigned

#### Test Case 5: Assign Class to User (Student)
**Method:** PUT `/api/users/{id}/class/{classId}`  
**Expected:** Status 200, class assigned

#### Test Case 6: Search Users (Advanced)
**Method:** POST `/api/users/search`  
**Body:**
```json
{
  "fullName": "Nguyen",
  "email": "@example.com",
  "role": "STUDENT",
  "departmentId": 1,
  "status": true
}
```
**Expected:** Status 200, filtered users

#### Test Case 7: Get Users by Department
**Method:** GET `/api/users/by-department/{departmentId}`  
**Expected:** Status 200, users in department

#### Test Case 8: Get Users by Class
**Method:** GET `/api/users/by-class/{classId}`  
**Expected:** Status 200, students in class

#### Test Case 9: Get Users by Role
**Method:** GET `/api/users/by-role/{roleId}`  
**Expected:** Status 200, users with role

#### Test Case 10: Get User Statistics
**Method:** GET `/api/users/statistics`  
**Expected:** Status 200, overall user statistics

#### Test Case 11: Get Active Users
**Method:** GET `/api/users/active`  
**Expected:** Status 200, only active users

#### Test Case 12: Deactivate User
**Method:** PUT `/api/users/{id}/deactivate`  
**Expected:** Status 200, user deactivated

#### Test Case 13: Reactivate User
**Method:** PUT `/api/users/{id}/activate`  
**Expected:** Status 200, user activated

---

## 5. End-to-End Workflow Tests

### Workflow 1: Complete Organization Setup
1. Login as admin
2. Create Department
3. Create Subject for Department
4. Create Class for Department
5. Create SubjectClass for Subject
6. Verify all relationships

### Workflow 2: Student Enrollment Process
1. Create/Get Student user
2. Assign student to Class
3. Enroll student in SubjectClass
4. Update enrollment status
5. Verify student appears in all lists

### Workflow 3: Teacher Assignment
1. Create/Get Teacher user
2. Assign teacher to Department
3. Create SubjectClass with teacher
4. Verify teacher's classes
5. Check statistics

---

## 6. Integration Test Checklist

### Module Level
- [ ] Department CRUD operations work
- [ ] Class CRUD operations work
- [ ] Subject CRUD operations work
- [ ] SubjectClass CRUD operations work
- [ ] User Management operations work

### Relationship Testing
- [ ] Department → Subject relationship
- [ ] Department → Class relationship
- [ ] Department → User relationship
- [ ] Class → Student relationship
- [ ] Subject → SubjectClass relationship
- [ ] SubjectClass → Student enrollment
- [ ] User → Role assignment
- [ ] User → Department/Class assignment

### Business Rules
- [ ] Soft delete works correctly
- [ ] Duplicate prevention works
- [ ] Foreign key constraints enforced
- [ ] Enrollment status transitions valid
- [ ] Role-based permissions work

### Advanced Features
- [ ] Search functionality works
- [ ] Statistics calculation accurate
- [ ] Pagination works
- [ ] Filtering by status works
- [ ] Date/Time auditing works

### Error Handling
- [ ] Invalid IDs return 404
- [ ] Duplicate data returns 400
- [ ] Missing required fields returns 400
- [ ] Foreign key violations handled
- [ ] Unauthorized access returns 401/403

---

## 7. Test Results Template

### Test Execution: [Date/Time]

| Module | Endpoint | Method | Status | Notes |
|--------|----------|--------|--------|-------|
| Department | /api/departments | POST | ✅ | Created successfully |
| Department | /api/departments | GET | ✅ | Retrieved list |
| ... | ... | ... | ... | ... |

### Issues Found
1. **Issue:** [Description]
   - **Severity:** Critical/High/Medium/Low
   - **Module:** [Module name]
   - **Steps to Reproduce:** [Steps]
   - **Expected:** [Expected behavior]
   - **Actual:** [Actual behavior]

### Summary
- **Total Tests:** XX
- **Passed:** XX
- **Failed:** XX
- **Pass Rate:** XX%

---

## 8. Next Steps After Testing

1. **Document all issues found**
2. **Prioritize critical bugs**
3. **Create fix tasks**
4. **Re-test after fixes**
5. **Update memory bank with results**
6. **Prepare for Phase 4**

---

## 9. Thunder Client Collection

Em sẽ tạo collection để import vào Thunder Client để test nhanh hơn.

---

**Document Status:** Ready for Testing  
**Last Updated:** 15/11/2025 15:07  
**Updated By:** NVMANH with Cline

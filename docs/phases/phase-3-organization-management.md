# Phase 3: Organization Management

## Tổng Quan

**Mục tiêu**: Xây dựng hệ thống quản lý tổ chức bao gồm Khoa (Department), Lớp (Class), Môn học (Subject), Lớp môn học (SubjectClass) và quản lý sinh viên.

**Thời gian dự kiến**: 1-2 tuần  
**Độ ưu tiên**: 🟡 High  
**Dependencies**: Phase 2 (Authentication & Authorization) ✅

---

## Objectives

### Primary Goals
1. ✅ Implement CRUD operations cho Department
2. ⏳ Implement CRUD operations cho Class (Lớp chung)
3. ⏳ Implement CRUD operations cho Subject (Môn học)
4. ⏳ Implement CRUD operations cho SubjectClass (Lớp môn học)
5. ⏳ Quản lý sinh viên trong lớp
6. ⏳ Phân công giáo viên cho lớp môn học
7. ⏳ Permission-based access control
8. ⏳ Validation và business rules

### Secondary Goals
- Statistics và reporting
- Bulk operations (import/export)
- Search và filtering
- Unit tests
- Integration tests

---

## Database Schema Review

### Tables Involved

#### 1. departments
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- department_code (VARCHAR(20), UNIQUE, NOT NULL)
- department_name (VARCHAR(100), NOT NULL)
- description (VARCHAR(500))
- email (VARCHAR(100))
- head_of_department (VARCHAR(100))
- manager_id (BIGINT, FK -> users.id)
- phone (VARCHAR(20))
- is_active (BIT)
- version (BIGINT) -- Optimistic locking
- created_at, updated_at, created_by, updated_by, deleted_at
```

**Note**: Table có duplicate columns (code/department_code, name/department_name) - Legacy structure

#### 2. classes (Lớp chung - Lớp hành chính)
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- code (VARCHAR(20), UNIQUE) -- Legacy
- class_code (VARCHAR(20), UNIQUE, NOT NULL)
- name (VARCHAR(255)) -- Legacy
- class_name (VARCHAR(100), NOT NULL)
- department_id (BIGINT, FK -> departments.id, NOT NULL)
- academic_year (VARCHAR(20), NOT NULL) -- e.g., "2023-2024"
- class_manager_id (BIGINT, FK -> users.id)
- homeroom_teacher (VARCHAR(100))
- is_active (BIT)
- version (BIGINT)
- created_at, updated_at, created_by, updated_by, deleted_at
```

**Note**: Table cũng có duplicate columns - Legacy structure

#### 3. subjects (Môn học)
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- code (VARCHAR(20), UNIQUE, NOT NULL)
- name (VARCHAR(255), NOT NULL)
- description (TEXT)
- credits (INT, DEFAULT 0)
- department_id (BIGINT, FK -> departments.id)
- version (BIGINT)
- created_at, updated_at, created_by, updated_by, deleted_at
```

#### 4. subject_classes (Lớp môn học - Lớp học phần)
```sql
- id (BIGINT, PK, AUTO_INCREMENT)
- code (VARCHAR(20), UNIQUE, NOT NULL)
- subject_id (BIGINT, FK -> subjects.id, NOT NULL)
- semester (VARCHAR(20), NOT NULL) -- e.g., "2023-2024-1"
- teacher_id (BIGINT, FK -> users.id, NOT NULL)
- schedule (VARCHAR(500)) -- JSON or text format
- max_students (INT, DEFAULT 50)
- version (BIGINT)
- created_at, updated_at, created_by, updated_by, deleted_at
```

#### 5. subject_class_students (Bảng trung gian N:M)
```sql
- subject_class_id (BIGINT, FK -> subject_classes.id, PK)
- student_id (BIGINT, FK -> users.id, PK)
- enrolled_at (TIMESTAMP)
- status (ENUM: 'ENROLLED', 'DROPPED', 'COMPLETED')
```

---

## Architecture Design

### Entity Layer Structure

```
com.mstrust.exam.entity/
├── Department.java (✅ Đã có)
├── ClassEntity.java (✅ Đã có)
├── Subject.java (⏳ Cần tạo)
└── SubjectClass.java (⏳ Cần tạo)
```

### Repository Layer Structure

```
com.mstrust.exam.repository/
├── DepartmentRepository.java (✅ Đã có)
├── ClassRepository.java (✅ Đã có)
├── SubjectRepository.java (⏳ Cần tạo)
└── SubjectClassRepository.java (⏳ Cần tạo)
```

### DTO Layer Structure

```
com.mstrust.exam.dto/
├── department/
│   ├── DepartmentDTO.java (✅ Đã có)
│   ├── CreateDepartmentRequest.java (✅ Đã có)
│   └── UpdateDepartmentRequest.java (⏳ Cần tạo)
├── class/
│   ├── ClassDTO.java (⏳ Cần tạo)
│   ├── CreateClassRequest.java (⏳ Cần tạo)
│   └── UpdateClassRequest.java (⏳ Cần tạo)
├── subject/
│   ├── SubjectDTO.java (⏳ Cần tạo)
│   ├── CreateSubjectRequest.java (⏳ Cần tạo)
│   └── UpdateSubjectRequest.java (⏳ Cần tạo)
└── subjectclass/
    ├── SubjectClassDTO.java (⏳ Cần tạo)
    ├── CreateSubjectClassRequest.java (⏳ Cần tạo)
    ├── UpdateSubjectClassRequest.java (⏳ Cần tạo)
    └── EnrollStudentsRequest.java (⏳ Cần tạo)
```

### Service Layer Structure

```
com.mstrust.exam.service/
├── DepartmentService.java (✅ Đã có - cần expand)
├── ClassService.java (⏳ Cần tạo)
├── SubjectService.java (⏳ Cần tạo)
└── SubjectClassService.java (⏳ Cần tạo)
```

### Controller Layer Structure

```
com.mstrust.exam.controller/
├── DepartmentController.java (✅ Đã có)
├── ClassController.java (⏳ Cần tạo)
├── SubjectController.java (⏳ Cần tạo)
└── SubjectClassController.java (⏳ Cần tạo)
```

---

## API Endpoints Design

### Department APIs (✅ Đã implement một phần)

```http
# CRUD Operations
POST   /api/departments                  # ✅ Create department
GET    /api/departments                  # ✅ Get all departments
GET    /api/departments/{id}             # ✅ Get department by ID
GET    /api/departments/code/{code}      # ✅ Get department by code
PUT    /api/departments/{id}             # ⏳ Update department
DELETE /api/departments/{id}             # ⏳ Delete department (soft delete)

# Additional Operations
GET    /api/departments/{id}/classes     # ⏳ Get all classes in department
GET    /api/departments/{id}/subjects    # ⏳ Get all subjects in department
GET    /api/departments/{id}/statistics  # ⏳ Get department statistics
POST   /api/departments/{id}/activate    # ⏳ Activate/Deactivate department
```

### Class APIs (⏳ Chưa implement)

```http
# CRUD Operations
POST   /api/classes                      # Create class
GET    /api/classes                      # Get all classes (with filters)
GET    /api/classes/{id}                 # Get class by ID
GET    /api/classes/code/{code}          # Get class by code
PUT    /api/classes/{id}                 # Update class
DELETE /api/classes/{id}                 # Delete class (soft delete)

# Student Management
GET    /api/classes/{id}/students        # Get all students in class
POST   /api/classes/{id}/students        # Add student to class
DELETE /api/classes/{id}/students/{studentId}  # Remove student from class
POST   /api/classes/{id}/students/bulk  # Bulk add students

# Additional Operations
GET    /api/classes/{id}/subject-classes # Get all subject classes for this class
GET    /api/classes/{id}/statistics     # Get class statistics
POST   /api/classes/{id}/activate       # Activate/Deactivate class
```

### Subject APIs (⏳ Chưa implement)

```http
# CRUD Operations
POST   /api/subjects                     # Create subject
GET    /api/subjects                     # Get all subjects (with filters)
GET    /api/subjects/{id}                # Get subject by ID
GET    /api/subjects/code/{code}         # Get subject by code
PUT    /api/subjects/{id}                # Update subject
DELETE /api/subjects/{id}                # Delete subject (soft delete)

# Additional Operations
GET    /api/subjects/{id}/classes        # Get all subject classes
GET    /api/subjects/{id}/statistics     # Get subject statistics
GET    /api/subjects/department/{deptId} # Get subjects by department
```

### SubjectClass APIs (⏳ Chưa implement)

```http
# CRUD Operations
POST   /api/subject-classes              # Create subject class
GET    /api/subject-classes              # Get all subject classes (with filters)
GET    /api/subject-classes/{id}         # Get subject class by ID
GET    /api/subject-classes/code/{code}  # Get subject class by code
PUT    /api/subject-classes/{id}         # Update subject class
DELETE /api/subject-classes/{id}         # Delete subject class (soft delete)

# Student Enrollment
GET    /api/subject-classes/{id}/students        # Get enrolled students
POST   /api/subject-classes/{id}/students        # Enroll students (bulk)
DELETE /api/subject-classes/{id}/students/{studentId}  # Remove student
PUT    /api/subject-classes/{id}/students/{studentId}/status  # Update enrollment status

# Additional Operations
GET    /api/subject-classes/{id}/statistics      # Get class statistics
GET    /api/subject-classes/teacher/{teacherId}  # Get classes by teacher
GET    /api/subject-classes/semester/{semester}  # Get classes by semester
POST   /api/subject-classes/{id}/change-teacher  # Change teacher
```

---

## Permission Matrix

### Department Operations

| Operation | STUDENT | TEACHER | CLASS_MANAGER | DEPT_MANAGER | ADMIN |
|-----------|---------|---------|---------------|--------------|-------|
| View Department | ✅ Own | ✅ Own | ✅ Own | ✅ All | ✅ All |
| Create Department | ❌ | ❌ | ❌ | ❌ | ✅ |
| Update Department | ❌ | ❌ | ❌ | ✅ Own | ✅ All |
| Delete Department | ❌ | ❌ | ❌ | ❌ | ✅ |
| View Statistics | ❌ | ❌ | ❌ | ✅ Own | ✅ All |

### Class Operations

| Operation | STUDENT | TEACHER | CLASS_MANAGER | DEPT_MANAGER | ADMIN |
|-----------|---------|---------|---------------|--------------|-------|
| View Class | ✅ Own | ✅ Teaching | ✅ Managing | ✅ Dept | ✅ All |
| Create Class | ❌ | ❌ | ❌ | ✅ Dept | ✅ All |
| Update Class | ❌ | ❌ | ✅ Managing | ✅ Dept | ✅ All |
| Delete Class | ❌ | ❌ | ❌ | ✅ Dept | ✅ All |
| Manage Students | ❌ | ❌ | ✅ Managing | ✅ Dept | ✅ All |

### Subject Operations

| Operation | STUDENT | TEACHER | CLASS_MANAGER | DEPT_MANAGER | ADMIN |
|-----------|---------|---------|---------------|--------------|-------|
| View Subject | ✅ All | ✅ All | ✅ All | ✅ All | ✅ All |
| Create Subject | ❌ | ❌ | ❌ | ✅ Dept | ✅ All |
| Update Subject | ❌ | ❌ | ❌ | ✅ Dept | ✅ All |
| Delete Subject | ❌ | ❌ | ❌ | ✅ Dept | ✅ All |

### SubjectClass Operations

| Operation | STUDENT | TEACHER | CLASS_MANAGER | DEPT_MANAGER | ADMIN |
|-----------|---------|---------|---------------|--------------|-------|
| View SubjectClass | ✅ Enrolled | ✅ Teaching | ✅ All | ✅ Dept | ✅ All |
| Create SubjectClass | ❌ | ❌ | ❌ | ✅ Dept | ✅ All |
| Update SubjectClass | ❌ | ✅ Own | ❌ | ✅ Dept | ✅ All |
| Delete SubjectClass | ❌ | ❌ | ❌ | ✅ Dept | ✅ All |
| Enroll Students | ❌ | ✅ Own | ✅ All | ✅ Dept | ✅ All |
| Remove Students | ❌ | ✅ Own | ✅ All | ✅ Dept | ✅ All |

---

## Business Rules & Validations

### Department
- ✅ `department_code` phải unique
- ✅ `department_code` không được rỗng, max 20 ký tự
- ✅ `department_name` không được rỗng, max 100 ký tự
- ⏳ Email phải đúng format (nếu có)
- ⏳ Phone phải đúng format (nếu có)
- ⏳ Không xóa department nếu còn classes hoặc subjects
- ⏳ Không deactivate nếu có active classes

### Class
- `class_code` phải unique
- `class_code` không được rỗng, max 20 ký tự
- `class_name` không được rỗng, max 100 ký tự
- `department_id` phải tồn tại
- `academic_year` phải đúng format (YYYY-YYYY)
- `class_manager_id` phải có role CLASS_MANAGER hoặc cao hơn
- Không xóa class nếu còn students
- Không thể có 2 class cùng code trong cùng department và academic_year

### Subject
- `code` phải unique
- `code` không được rỗng, max 20 ký tự
- `name` không được rỗng, max 255 ký tự
- `credits` phải >= 0
- `department_id` phải tồn tại (nếu có)
- Không xóa subject nếu còn subject_classes đang active

### SubjectClass
- `code` phải unique
- `code` không được rỗng, max 20 ký tự
- `subject_id` phải tồn tại
- `teacher_id` phải có role TEACHER hoặc cao hơn
- `semester` phải đúng format (YYYY-YYYY-N, N=1,2,3)
- `max_students` phải > 0
- Không được enroll quá `max_students`
- Không enroll student đã enrolled
- Student phải có role STUDENT
- Không xóa nếu đã có exams

---

## Implementation Plan

### Step 1: Complete Department Module (1-2 ngày)
- [x] Entity Department (đã có)
- [x] DepartmentRepository (đã có)
- [x] DepartmentDTO, CreateDepartmentRequest (đã có)
- [ ] UpdateDepartmentRequest DTO
- [ ] Expand DepartmentService với đầy đủ business logic
- [ ] Complete DepartmentController (UPDATE, DELETE endpoints)
- [ ] Permission checking trong Service layer
- [ ] Validation annotations
- [ ] Exception handling
- [ ] Unit tests

### Step 2: Class Module (2-3 ngày)
- [ ] Update ClassEntity (đã có entity cơ bản)
- [ ] ClassRepository với custom queries
- [ ] ClassDTO, CreateClassRequest, UpdateClassRequest
- [ ] ClassService với business logic
- [ ] ClassController với full CRUD
- [ ] Student management endpoints
- [ ] Permission checking
- [ ] Validation
- [ ] Unit tests

### Step 3: Subject Module (1-2 ngày)
- [ ] Subject Entity
- [ ] SubjectRepository
- [ ] SubjectDTO, CreateSubjectRequest, UpdateSubjectRequest
- [ ] SubjectService
- [ ] SubjectController
- [ ] Permission checking
- [ ] Validation
- [ ] Unit tests

### Step 4: SubjectClass Module (3-4 ngày)
- [ ] SubjectClass Entity
- [ ] SubjectClassRepository
- [ ] SubjectClassDTO, CreateSubjectClassRequest, UpdateSubjectClassRequest
- [ ] EnrollStudentsRequest DTO
- [ ] SubjectClassService với enrollment logic
- [ ] SubjectClassController
- [ ] Student enrollment management
- [ ] Permission checking
- [ ] Validation
- [ ] Unit tests

### Step 5: Integration & Testing (2-3 ngày)
- [ ] Integration tests cho tất cả modules
- [ ] End-to-end testing scenarios
- [ ] Performance testing
- [ ] Security testing
- [ ] Bug fixing

### Step 6: Documentation (1 ngày)
- [ ] API documentation với Swagger
- [ ] Update README
- [ ] Usage examples
- [ ] Deployment guide

---

## Testing Strategy

### Unit Tests
```java
// DepartmentServiceTest
- testCreateDepartment_Success
- testCreateDepartment_DuplicateCode
- testUpdateDepartment_Success
- testUpdateDepartment_NotFound
- testDeleteDepartment_Success
- testDeleteDepartment_HasClasses
- testGetDepartmentByCode_Success
- testGetDepartmentByCode_NotFound

// ClassServiceTest
- testCreateClass_Success
- testCreateClass_DuplicateCode
- testAddStudent_Success
- testAddStudent_ClassFull
- testRemoveStudent_Success
// ... etc

// SubjectServiceTest
// SubjectClassServiceTest
```

### Integration Tests
```java
// DepartmentIntegrationTest
- testFullDepartmentCRUDFlow
- testDepartmentWithClassesAndSubjects

// ClassIntegrationTest
- testFullClassCRUDFlow
- testStudentEnrollmentFlow

// SubjectClassIntegrationTest
- testCreateSubjectClassAndEnrollStudents
- testTeacherAssignment
```

### Security Tests
```java
// PermissionTest
- testStudentCannotCreateDepartment
- testTeacherCannotDeleteClass
- testDeptManagerCanOnlyManageOwnDepartment
// ... etc
```

---

## Current Status (15/11/2025)

### ✅ Completed
1. Department Entity với đầy đủ fields
2. DepartmentRepository với custom queries
3. DepartmentDTO, CreateDepartmentRequest
4. DepartmentService (basic CRUD)
5. DepartmentController (CREATE, GET operations)
6. ClassEntity (basic structure)
7. ClassRepository (basic)

### 🔄 In Progress
- Department Module hoàn thiện (UPDATE, DELETE)
- Department validation và business rules

### ⏳ Pending
- Class Module (full implementation)
- Subject Module
- SubjectClass Module
- Student enrollment logic
- Permission checking comprehensive
- Unit & Integration tests
- API documentation

---

## Known Issues

### Legacy Column Issues
**Problem**: Tables `departments` và `classes` có duplicate columns:
- departments: `code` + `department_code`, `name` + `department_name`
- classes: `code` + `class_code`, `name` + `class_name`

**Current Solution**: 
- Entity sử dụng columns mới (`department_code`, `department_name`, `class_code`, `class_name`)
- Migration V5 đã được tạo để cleanup (chưa execute)

**Action Required**:
- Quyết định xem có execute migration V5 không
- Hoặc maintain compatibility với legacy columns

---

## Success Criteria

Phase 3 được coi là hoàn thành khi:

- [ ] Tất cả 4 modules (Department, Class, Subject, SubjectClass) đã implement đầy đủ
- [ ] CRUD operations hoạt động cho tất cả entities
- [ ] Student enrollment/removal working
- [ ] Permission checking working correctly
- [ ] All business rules validated
- [ ] Unit test coverage > 80%
- [ ] Integration tests pass
- [ ] API documentation complete
- [ ] No critical bugs
- [ ] Performance acceptable (API < 500ms)

---

## Next Phase

Sau khi Phase 3 hoàn thành, chuyển sang **Phase 4: Exam Management**

---

**Author**: K24DTCN210-NVMANH with Cline  
**Created**: 15/11/2025 13:50  
**Last Updated**: 15/11/2025 13:50

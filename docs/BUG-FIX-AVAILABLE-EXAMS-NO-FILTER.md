# 🐛 Bug Fix: Available Exams API Không Filter Theo Class

**Ngày:** 23/11/2025 15:15  
**Người báo:** Cụ Mạnh  
**Phát hiện:** Test case "Empty List" cho student2 vẫn trả về tất cả exams

---

## 📋 Mô Tả Bug

### Hiện Trạng
API `GET /api/exam-taking/available` đang trả về **TẤT CẢ exams** với status PUBLISHED/ONGOING, không phân biệt student thuộc class nào.

### Kỳ Vọng  
API chỉ nên trả về exams mà:
- Student THUỘC lớp được giao exam đó
- Status = PUBLISHED hoặc ONGOING
- Thời gian còn hợp lệ

### Ảnh Hưởng
- ❌ Student thấy exams không phải của họ
- ❌ Có thể start exam của class khác (security issue!)
- ❌ UI hiển thị sai danh sách exams

---

## 🔍 Root Cause Analysis

### Code Hiện Tại (ExamTakingService.java Line 49-56)
```java
public List<AvailableExamDTO> getAvailableExams(Long studentId) {
    User student = userRepository.findById(studentId)
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    
    // ❌ WRONG: Lấy TẤT CẢ exams
    List<Exam> allExams = examRepository.findAll().stream()
        .filter(exam -> {
            ExamStatus status = exam.getCurrentStatus();
            return status == ExamStatus.PUBLISHED || status == ExamStatus.ONGOING;
        })
        .collect(Collectors.toList());
    
    // Map to DTO
    return allExams.stream()
        .map(exam -> mapToAvailableExamDTO(exam, studentId))
        .collect(Collectors.toList());
}
```

### Vấn Đề
1. **Thiếu filter theo class:** Code không check `student` có thuộc `exam.getSubjectClass()` không
2. **Security hole:** Student có thể xem/start exam của class khác

---

## ✅ Solution

### Bước 1: Thêm Method Vào StudentClassRepository

```java
// StudentClassRepository.java
List<StudentClass> findByStudentId(Long studentId);
```

### Bước 2: Fix Logic getAvailableExams()

```java
public List<AvailableExamDTO> getAvailableExams(Long studentId) {
    User student = userRepository.findById(studentId)
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    
    // ✅ Lấy danh sách classes của student
    List<StudentClass> studentClasses = studentClassRepository.findByStudentId(studentId);
    Set<Long> classIds = studentClasses.stream()
        .map(sc -> sc.getSubjectClass().getId())
        .collect(Collectors.toSet());
    
    // ✅ Chỉ lấy exams thuộc classes của student
    List<Exam> eligibleExams = examRepository.findAll().stream()
        .filter(exam -> {
            // Check status
            ExamStatus status = exam.getCurrentStatus();
            if (status != ExamStatus.PUBLISHED && status != ExamStatus.ONGOING) {
                return false;
            }
            
            // ✅ Check student thuộc class này
            Long examClassId = exam.getSubjectClass().getId();
            return classIds.contains(examClassId);
        })
        .collect(Collectors.toList());
    
    // Map to DTO
    return eligibleExams.stream()
        .map(exam -> mapToAvailableExamDTO(exam, studentId))
        .collect(Collectors.toList());
}
```

### Bước 3: Optional - Tối Ưu Query

Thêm custom query vào ExamRepository để giảm N+1:

```java
// ExamRepository.java
@Query("""
    SELECT DISTINCT e FROM Exam e
    JOIN e.subjectClass sc
    JOIN StudentClass stc ON stc.subjectClass.id = sc.id
    WHERE stc.student.id = :studentId
    AND e.deletedAt IS NULL
    AND (e.status = 'PUBLISHED' OR e.status = 'ONGOING')
    ORDER BY e.startTime DESC
""")
List<Exam> findAvailableExamsForStudent(@Param("studentId") Long studentId);
```

Sau đó dùng:
```java
List<Exam> eligibleExams = examRepository.findAvailableExamsForStudent(studentId);
```

---

## 🧪 Test Cases

### Case 1: Student1 (Có exams)
```bash
GET /api/exam-taking/available
Authorization: Bearer {student1Token}

# Expected: List exams của classes mà student1 đang học
# ✅ Should return exams array
```

### Case 2: Student2 (Không có exams)  
```bash
GET /api/exam-taking/available
Authorization: Bearer {student2Token}

# Expected: Empty array
# ✅ Should return []
```

### Case 3: Filter Theo Subject
```bash
GET /api/exam-taking/available?subjectCode=PRO192
Authorization: Bearer {student1Token}

# Expected: Chỉ exams môn PRO192 MÀ student1 học
# ✅ Should return filtered results
```

---

## 📊 Test Data Requirements

Để test đúng, cần setup:

1. **Student1:** Có trong class có exams
   ```sql
   INSERT INTO student_class (student_id, subject_class_id, ...) 
   VALUES (1, 1, ...);  -- student1 học class 1
   
   INSERT INTO exam (subject_class_id, status, ...) 
   VALUES (1, 'PUBLISHED', ...);  -- exam cho class 1
   ```

2. **Student2:** Không có trong class nào / hoặc class không có exams
   ```sql
   -- Không insert student_class cho student2
   -- HOẶC insert vào class không có exam
   ```

---

## 🎯 Implementation Priority

**Priority:** 🔴 CRITICAL (Security Issue)

**Reason:**
- Hiện tại student có thể thấy/start exam không phải của họ
- Vi phạm data isolation giữa các classes
- Ảnh hưởng tất cả test cases

**Next Steps:**
1. ✅ Thêm method findByStudentId() vào StudentClassRepository
2. ✅ Fix logic trong getAvailableExams()
3. ✅ Test với 2 students (1 có exam, 1 không có)
4. ✅ Update Thunder Client test assertions

---

**Author:** K24DTCN210-NVMANH  
**Date:** 23/11/2025 15:15

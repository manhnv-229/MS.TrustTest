# Phase 7: Test Data Setup Guide

## 📋 Mục Đích

File này hướng dẫn cách tạo exam với đầy đủ questions để test Phase 7 Grading System.

## 🎯 Script SQL: create-exam-with-questions-phase7.sql

### Nội dung tạo:

**Exam ID: 100**
- **Title:** Java OOP - Đề thi giữa kỳ (Complete)
- **Duration:** 90 phút
- **Total Points:** 100 điểm
- **Passing Score:** 50/100
- **Subject Class:** Lớp học Lập trình Java OOP (ID=1)
- **Teacher:** teacher1@mstrust.edu.vn (ID=6)

### Câu hỏi (9 questions):

#### Auto-Graded (30 điểm):
1. **Question 1001** - Multiple Choice (10đ): Tính chất của OOP
2. **Question 1002** - Multiple Choice (10đ): Java inheritance keyword
3. **Question 1003** - True/False (5đ): Multiple inheritance
4. **Question 1004** - True/False (5đ): Abstract class

#### Manual Grading (70 điểm):
5. **Question 1005** - Essay (20đ): Giải thích 4 tính chất OOP
6. **Question 1006** - Essay (15đ): So sánh Interface vs Abstract Class
7. **Question 1007** - Short Answer (10đ): Constructor trong Java
8. **Question 1008** - Short Answer (10đ): Overloading vs Overriding
9. **Question 1009** - Coding (15đ): Viết class Rectangle

## 🚀 Cách Sử Dụng

### Bước 1: Chạy Script

```bash
# Connect to MySQL
mysql -u root -p

# Run script
mysql> source database/create-exam-with-questions-phase7.sql;
```

Hoặc sử dụng MySQL Workbench:
1. Open file `create-exam-with-questions-phase7.sql`
2. Click Execute (⚡ icon)

### Bước 2: Verify Data

```sql
-- Check exam
SELECT * FROM exams WHERE id = 100;

-- Check questions
SELECT 
    q.id,
    q.question_order,
    qb.question_type,
    q.points,
    qb.question_text
FROM questions q
JOIN question_bank qb ON q.question_bank_id = qb.id
WHERE q.exam_id = 100
ORDER BY q.question_order;

-- Summary
SELECT 
    e.id as exam_id,
    e.title,
    COUNT(q.id) as total_questions,
    SUM(q.points) as total_points
FROM exams e
LEFT JOIN questions q ON e.id = q.exam_id
WHERE e.id = 100
GROUP BY e.id, e.title;
```

**Expected Output:**
```
exam_id: 100
title: Java OOP - Đề thi giữa kỳ (Complete)
total_questions: 9
total_points: 100.00
```

### Bước 3: Test Workflow với Thunder Client

#### 3.1. Setup Variables
```json
{
  "baseUrl": "http://localhost:8080",
  "examId": "100",
  "teacherToken": "{{from_login}}",
  "studentToken": "{{from_login}}"
}
```

#### 3.2. Complete Test Flow

**Folder 0: Authentication**
- Login as teacher → Get `teacherToken`
- Login as student → Get `studentToken`

**Folder 1: Student Take Exam**
1. Get Available Exams → Verify exam 100 visible
2. Start Exam 100 → Get `submissionId` + question IDs
3. Answer Question 1001 (MC) → `answerText: "D"`
4. Answer Question 1002 (MC) → `answerText: "C"`
5. Answer Question 1003 (TF) → `answerText: "FALSE"`
6. Answer Question 1004 (TF) → `answerText: "TRUE"`
7. Answer Question 1005 (Essay) → Long text về OOP
8. Answer Question 1006 (Essay) → So sánh Interface/Abstract
9. Answer Question 1007 (Short) → Constructor explanation
10. Answer Question 1008 (Short) → Overloading/Overriding
11. Answer Question 1009 (Coding) → Rectangle class code
12. Submit Exam → Status = SUBMITTED

**Folder 2: Teacher Get Submissions**
1. Get All Submissions → Find submission for exam 100
2. Get Submission Detail → Note answer IDs for manual grading

**Folder 3: Teacher Grade Answers**
1. Grade Question 1005 (Essay) → Score: 18/20
2. Grade Question 1006 (Essay) → Score: 13/15
3. Grade Question 1007 (Short) → Score: 8/10
4. Grade Question 1008 (Short) → Score: 9/10
5. Grade Question 1009 (Coding) → Score: 13/15

**Folder 4: Teacher Finalize**
1. Finalize Grading → Calculate total score
2. Get Exam Statistics → View stats

**Folder 5: Student View Results**
1. View Results → See all scores + feedback

## 📊 Expected Test Results

### Auto-Graded Questions (Correct Answers):
- Question 1001 (MC): D → 10/10 ✅
- Question 1002 (MC): C → 10/10 ✅
- Question 1003 (TF): FALSE → 5/5 ✅
- Question 1004 (TF): TRUE → 5/5 ✅
- **Auto-Grade Total:** 30/30

### Manual Graded Questions (Example Scores):
- Question 1005 (Essay): 18/20
- Question 1006 (Essay): 13/15
- Question 1007 (Short): 8/10
- Question 1008 (Short): 9/10
- Question 1009 (Coding): 13/15
- **Manual Grade Total:** 61/70

### Final Result:
- **Total Score:** 91/100
- **Percentage:** 91%
- **Status:** PASSED ✅ (passing score = 50)

## 🔄 Reset Data

### Xóa Submissions (giữ lại Exam):
```sql
-- Xóa submissions và answers
DELETE sa FROM student_answers sa
JOIN exam_submissions es ON sa.submission_id = es.id
WHERE es.exam_id = 100;

DELETE FROM exam_submissions WHERE exam_id = 100;
```

### Xóa Toàn Bộ Exam:
```sql
-- Xóa tất cả (exam + questions + submissions)
DELETE FROM student_answers 
WHERE submission_id IN (
    SELECT id FROM exam_submissions WHERE exam_id = 100
);

DELETE FROM exam_submissions WHERE exam_id = 100;
DELETE FROM questions WHERE exam_id = 100;
DELETE FROM exams WHERE id = 100;
```

### Tạo Lại Exam:
```bash
mysql> source database/create-exam-with-questions-phase7.sql;
```

## 🎓 Sample Student Answers

### Question 1005 (Essay) - Sample Good Answer:
```
Lập trình hướng đối tượng (OOP) có 4 tính chất cơ bản:

1. Encapsulation (Đóng gói):
   - Che giấu thông tin, chỉ cho phép truy cập qua public methods
   - Ví dụ:
   class BankAccount {
       private double balance; // private field
       public void deposit(double amount) { balance += amount; }
       public double getBalance() { return balance; }
   }

2. Inheritance (Kế thừa):
   - Lớp con kế thừa thuộc tính và phương thức từ lớp cha
   - Ví dụ:
   class Animal { void eat() {...} }
   class Dog extends Animal { void bark() {...} }

3. Polymorphism (Đa hình):
   - Một phương thức có nhiều hình thức khác nhau
   - Ví dụ:
   class Animal { void makeSound() {...} }
   class Dog extends Animal { 
       @Override void makeSound() { System.out.println("Woof!"); }
   }

4. Abstraction (Trừu tượng):
   - Ẩn chi tiết implementation, chỉ hiển thị chức năng
   - Ví dụ:
   abstract class Shape { abstract double getArea(); }
   class Circle extends Shape {
       double radius;
       double getArea() { return Math.PI * radius * radius; }
   }
```

### Question 1009 (Coding) - Sample Solution:
```java
/**
 * Rectangle class - represents a rectangle shape
 * @author Student
 */
public class Rectangle {
    // Properties
    private double width;
    private double height;
    
    /**
     * Constructor - initialize width and height
     * @param width Rectangle width
     * @param height Rectangle height
     */
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Calculate rectangle area
     * @return Area value (width * height)
     */
    public double calculateArea() {
        return width * height;
    }
    
    /**
     * Calculate rectangle perimeter
     * @return Perimeter value (2 * (width + height))
     */
    public double calculatePerimeter() {
        return 2 * (width + height);
    }
    
    // Getters and Setters
    public double getWidth() {
        return width;
    }
    
    public void setWidth(double width) {
        this.width = width;
    }
    
    public double getHeight() {
        return height;
    }
    
    public void setHeight(double height) {
        this.height = height;
    }
}
```

## 📝 Notes

### Question IDs:
- Use question IDs from 1001-1009 (không conflict với existing questions)
- Question Bank IDs cũng từ 1001-1009

### Exam ID:
- Exam ID = 100 (không conflict với existing exams)
- Có thể thay đổi nếu cần

### Dependencies:
- Requires `subject_class_id = 1` (Lớp học Java OOP)
- Requires `teacher_id = 6` (teacher1@mstrust.edu.vn)
- Requires `subject_id = 1` (Môn học Lập trình Java)

### Nếu gặp lỗi Foreign Key:
```sql
-- Check subject_classes
SELECT * FROM subject_classes WHERE id = 1;

-- Check users (teacher)
SELECT * FROM users WHERE id = 6;

-- Check subjects
SELECT * FROM subjects WHERE id = 1;
```

## 🔗 Related Files

- Thunder Client Collection: `docs/thunder-client-phase7-grading.json`
- Testing Guide: `docs/PHASE7-TESTING-GUIDE.md`
- Phase 7 Documentation: `docs/PHASE7-GRADING-SYSTEM-COMPLETE.md`

---

**Author:** K24DTCN210-NVMANH  
**Date:** 21/11/2025  
**Version:** 1.0

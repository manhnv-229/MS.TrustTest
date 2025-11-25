# Phase 8.6 - Bug Fix: StudentInfoLabel NullPointerException

## 📋 Thông Tin Bug

**Ngày phát hiện:** 24/11/2025 11:40  
**Người báo cáo:** K24DTCN210-NVMANH  
**Mức độ:** CRITICAL - Blocking exam taking functionality

## 🐛 Mô Tả Lỗi

### Triệu chứng
```
NullPointerException: Cannot invoke "javafx.scene.control.Label.setText(String)" 
because "this.studentInfoLabel" is null
```

### Nguyên nhân
- **Controller** (`ExamTakingController.java`) khai báo và sử dụng:
  ```java
  @FXML private Label studentInfoLabel;
  ```
  
- **FXML** (`exam-taking.fxml`) lại có cấu trúc khác:
  ```xml
  <Label fx:id="studentNameLabel" text="Sinh viên" styleClass="student-name"/>
  <Label fx:id="studentCodeLabel" text="MSV" styleClass="student-code"/>
  ```

### Tác động
- Không thể khởi tạo màn hình làm bài thi
- Application crash khi bắt đầu thi
- Blocking toàn bộ chức năng exam taking

---

## ✅ Giải Pháp

### 1. Sửa Controller - Khai báo đúng FXML fields

**File:** `client-javafx/src/main/java/com/mstrust/client/exam/controller/ExamTakingController.java`

**Thay đổi:**
```java
// ❌ CŨ - SAI
@FXML private Label studentInfoLabel;

// ✅ MỚI - ĐÚNG
@FXML private Label examSubtitleLabel;
@FXML private Label studentNameLabel;
@FXML private Label studentCodeLabel;
```

### 2. Sửa method initializeComponents()

**Thay đổi:**
```java
// ❌ CŨ - SAI
examTitleLabel.setText(response.getExamTitle());
studentInfoLabel.setText("Thí sinh: " + getCurrentStudentName());

// ✅ MỚI - ĐÚNG
examTitleLabel.setText(response.getExamTitle());
examSubtitleLabel.setText(String.format("Thời gian: %d phút", response.getDurationMinutes()));
studentNameLabel.setText(getCurrentStudentName());
studentCodeLabel.setText(getCurrentStudentCode());
```

### 3. Thêm method getCurrentStudentCode()

**Code mới:**
```java
/* ---------------------------------------------------
 * Get current student code (mock - replace with actual logic)
 * @returns Student code
 * @author: K24DTCN210-NVMANH (24/11/2025 11:40)
 * --------------------------------------------------- */
private String getCurrentStudentCode() {
    // TODO: Get from authentication context
    return "SV001";
}
```

---

## 🧪 Testing

### Build & Compile
```bash
cd client-javafx
mvn clean compile
```

**Kết quả:** ✅ BUILD SUCCESS

### Manual Testing Steps
1. ✅ Chạy client application
2. ✅ Đăng nhập với student account
3. ✅ Chọn một bài thi available
4. ✅ Click "Bắt Đầu Thi"
5. ✅ Verify:
   - Header hiển thị đầy đủ thông tin
   - studentNameLabel hiển thị "Nguyễn Văn A"
   - studentCodeLabel hiển thị "SV001"
   - examSubtitleLabel hiển thị thời gian thi
   - Không có NullPointerException

---

## 📝 Root Cause Analysis

### Tại sao lỗi này xảy ra?

1. **Inconsistency giữa FXML và Controller:**
   - FXML được thiết kế với 2 Label riêng biệt (name + code)
   - Controller lại giả định 1 Label duy nhất (studentInfoLabel)

2. **FXML injection failure:**
   - JavaFX không tìm thấy `fx:id="studentInfoLabel"` trong FXML
   - Field `studentInfoLabel` trong Controller = null
   - Khi gọi `studentInfoLabel.setText()` → NullPointerException

### Bài học
- ✅ LUÔN đảm bảo `fx:id` trong FXML khớp với `@FXML` field trong Controller
- ✅ Test ngay sau khi thêm FXML fields
- ✅ Sử dụng meaningful names cho UI components

---

## 🔧 Files Changed

1. `client-javafx/src/main/java/com/mstrust/client/exam/controller/ExamTakingController.java`
   - Added: `@FXML private Label examSubtitleLabel;`
   - Added: `@FXML private Label studentNameLabel;`
   - Added: `@FXML private Label studentCodeLabel;`
   - Removed: `@FXML private Label studentInfoLabel;`
   - Updated: `initializeComponents()` method
   - Added: `getCurrentStudentCode()` method

---

## ✅ Kết Quả

### Trước khi fix:
```
Exception: NullPointerException at ExamTakingController.initializeComponents()
→ Application crash
→ Cannot start exam
```

### Sau khi fix:
```
✅ Exam taking screen loads successfully
✅ Student info displays correctly:
   - Name: "Nguyễn Văn A"
   - Code: "SV001"
   - Exam subtitle: "Thời gian: XX phút"
✅ All components initialized properly
✅ Ready for full-screen security testing
```

---

## 📌 Next Steps

Với bug fix này hoàn tất, giờ có thể tiếp tục:
1. ✅ Test full-screen security features
2. ⏳ Implement exit confirmation dialog
3. ⏳ Add loading indicators
4. ⏳ Implement keyboard shortcuts

---

## 👤 Author
**K24DTCN210-NVMANH**  
Date: 24/11/2025 11:40

## 🔖 Related Documents
- [PHASE8.6-STEP2-FULLSCREEN-COMPLETE.md](./PHASE8.6-STEP2-FULLSCREEN-COMPLETE.md)
- [PHASE8.6-STEP2-MANUAL-TESTING-GUIDE.md](./PHASE8.6-STEP2-MANUAL-TESTING-GUIDE.md)

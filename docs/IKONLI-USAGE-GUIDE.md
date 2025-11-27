# Ikonli Icon Library - Hướng Dẫn Sử Dụng

## 📋 Tổng Quan

Ikonli là thư viện icon mạnh mẽ cho JavaFX, cung cấp hàng nghìn icon từ các bộ phổ biến như FontAwesome, Material Design, Bootstrap Icons. 

**Version:** 12.3.1
**Documentation:** https://kordamp.org/ikonli/

---

## 🎨 Các Bộ Icon Đã Tích Hợp

### 1. FontAwesome 5 (Solid)
- **Package:** `org.kordamp.ikonli.fontawesome5. FontAwesomeSolid`
- **Prefix:** `fas-`
- **Số lượng:** 1,500+ icons
- **Phù hợp:** Business applications, UI general purpose

### 2. Material Design 2
- **Package:** `org.kordamp.ikonli.materialdesign2.*`
- **Prefix:** `mdi2-`
- **Số lượng:** 6,000+ icons
- **Phù hợp:** Modern UI, mobile-like interfaces

### 3. Bootstrap Icons
- **Package:** `org. kordamp.ikonli. bootstrapicons. BootstrapIcons`
- **Prefix:** `bi-`
- **Số lượng:** 1,800+ icons
- **Phù hợp:** Clean, simple designs

---

## 💻 Cách Sử Dụng

### Option 1: Sử Dụng IconFactory (Khuyến Nghị)

IconFactory cung cấp các method tiện ích để tạo icon nhanh chóng:

```java
import com.mstrust.client.exam.util.IconFactory;
import javafx.scene.control.Button;

// Tạo button với icon
Button loginButton = new Button("Đăng Nhập");
loginButton.setGraphic(IconFactory.createLoginIcon());

Button saveButton = new Button("Lưu");
saveButton.setGraphic(IconFactory.createSaveIcon());

Button deleteButton = new Button("Xóa");
deleteButton.setGraphic(IconFactory.createDeleteIcon());
```

### Option 2: Sử Dụng Trực Tiếp trong Java

```java
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import javafx.scene.paint.Color;

// Tạo icon trực tiếp
FontIcon icon = new FontIcon(FontAwesomeSolid.USER);
icon.setIconSize(20);
icon.setIconColor(Color.web("#2196F3"));

Button button = new Button("User Profile");
button.setGraphic(icon);
```

### Option 3: Sử Dụng trong FXML

```xml
<? xml version="1.0" encoding="UTF-8"?>
<? import org.kordamp.ikonli.javafx.FontIcon?>
<?import javafx.scene.control.Button?>

<Button text="Save">
    <graphic>
        <FontIcon iconLiteral="fas-save" 
                  iconSize="16" 
                  iconColor="#4CAF50"/>
    </graphic>
</Button>
```

---

## 🎯 IconFactory - Available Methods

### Kích thước chuẩn:
```java
IconFactory.SIZE_SMALL = 14;   // Cho text field icons
IconFactory.SIZE_NORMAL = 16;  // Cho buttons thông thường
IconFactory.SIZE_MEDIUM = 20;  // Cho menu items
IconFactory.SIZE_LARGE = 24;   // Cho titles, headers
IconFactory.SIZE_XLARGE = 32;  // Cho splash screens
```

### Màu sắc chuẩn:
```java
IconFactory.COLOR_PRIMARY = #2196F3;  // Blue
IconFactory.COLOR_SUCCESS = #4CAF50;  // Green
IconFactory.COLOR_WARNING = #FF9800;  // Orange
IconFactory.COLOR_DANGER = #F44336;   // Red
IconFactory.COLOR_INFO = #00BCD4;     // Cyan
IconFactory.COLOR_WHITE = #FFFFFF;    // White
IconFactory.COLOR_GRAY = #757575;     // Gray
IconFactory.COLOR_DARK = #424242;     // Dark Gray
```

### Các Icon Methods:

#### Login & Authentication
```java
IconFactory.createUserIcon()      // fas-user
IconFactory.createLockIcon()      // fas-lock  
IconFactory.createLoginIcon()     // fas-sign-in-alt
IconFactory.createLogoutIcon()    // fas-sign-out-alt
```

#### Teacher Dashboard
```java
IconFactory.createQuestionBankIcon()    // fas-book
IconFactory.createSubjectIcon()         // fas-book-open
IconFactory.createExamIcon()            // fas-file-alt
IconFactory.createGradingIcon()         // fas-edit
IconFactory.createMonitoringIcon()      // fas-chart-bar
IconFactory.createUserManagementIcon()  // fas-users
IconFactory.createOrganizationIcon()    // fas-building
IconFactory.createSettingsIcon()        // fas-cog
IconFactory.createHelpIcon()            // fas-question-circle
```

#### Common Actions
```java
IconFactory.createSaveIcon()      // fas-save
IconFactory. createAddIcon()       // fas-plus
IconFactory.createDeleteIcon()    // fas-trash
IconFactory.createEditIcon()      // fas-pencil-alt
IconFactory.createSearchIcon()    // fas-search
```

#### Custom Icon
```java
// Tạo icon từ Ikon enum
FontIcon icon = IconFactory.createIcon(
    FontAwesomeSolid. STAR, 
    IconFactory.SIZE_LARGE, 
    IconFactory.COLOR_WARNING
);

// Tạo icon từ literal string (dùng trong FXML)
FontIcon icon = IconFactory.createIconFromLiteral(
    "fas-heart",
    20,
    Color.RED
);
```

---

## 📚 Icon Literal Syntax

Format: `<prefix>-<icon-name>`

### FontAwesome Examples:
```
fas-user
fas-lock
fas-save
fas-edit
fas-trash
fas-cog
fas-home
fas-file
fas-folder
fas-envelope
fas-calendar
fas-clock
fas-check
fas-times
fas-arrow-left
fas-arrow-right
```

### Material Design Examples:
```
mdi2-home
mdi2-account
mdi2-cog
mdi2-content-save
mdi2-delete
mdi2-pencil
mdi2-plus
mdi2-minus
mdi2-check
mdi2-close
```

### Bootstrap Icons Examples:
```
bi-house
bi-person
bi-gear
bi-save
bi-trash
bi-pencil
bi-plus
bi-dash
bi-check
bi-x
```

---

## 🔍 Tìm Icon

### Online Cheatsheets:

1. **FontAwesome 5:**
   - https://fontawesome.com/v5/search
   - Chọn "Free" filter
   - Copy tên icon (VD: "user" → sử dụng `fas-user`)

2. **Material Design Icons:**
   - https://pictogrammers.com/library/mdi/
   - Search icon
   - Use name with `mdi2-` prefix

3. **Bootstrap Icons:**
   - https://icons.getbootstrap. com/
   - Search và copy name
   - Use with `bi-` prefix

---

## ✅ Best Practices

### 1.  Sử Dụng IconFactory
```java
// ✅ GOOD - Consistent và dễ maintain
button.setGraphic(IconFactory. createSaveIcon());

// ❌ BAD - Hardcode everywhere
FontIcon icon = new FontIcon(FontAwesomeSolid. SAVE);
icon.setIconSize(16);
icon.setIconColor(Color. web("#4CAF50"));
button.setGraphic(icon);
```

### 2. Size Consistency
```java
// ✅ GOOD - Sử dụng constant
IconFactory.SIZE_NORMAL
IconFactory.SIZE_MEDIUM

// ❌ BAD - Magic numbers
icon.setIconSize(16);
icon.setIconSize(17); // Inconsistent! 
```

### 3. Color Consistency
```java
// ✅ GOOD - Sử dụng color constants
IconFactory.COLOR_PRIMARY
IconFactory.COLOR_SUCCESS

// ❌ BAD - Hardcoded colors
Color.web("#2196F3")
Color.web("#2296F3") // Typo prone!
```

### 4.  Semantic Icon Selection
```java
// ✅ GOOD - Icon có ý nghĩa rõ ràng
saveButton.setGraphic(IconFactory.createSaveIcon());      // fas-save
deleteButton.setGraphic(IconFactory.createDeleteIcon());  // fas-trash

// ❌ BAD - Icon không phù hợp context
saveButton.setGraphic(IconFactory.createSearchIcon());    // Confusing!
```

---

## 🎨 Styling Icons in CSS

```css
/* Change icon color on hover */
.button:hover .  ikonli-font-icon {
    -fx-icon-color: #1976D2;
}

/* Disable state */
.button:disabled . ikonli-font-icon {
    -fx-icon-color: #BDBDBD;
    -fx-opacity: 0.5;
}

/* Animated icon */
.spinning-icon {
    -fx-rotate: 0;
    -fx-animation: spin 2s linear infinite;
}

@keyframes spin {
    from { -fx-rotate: 0; }
    to { -fx-rotate: 360; }
}
```

---

## 🐛 Troubleshooting

### Icon không hiển thị
1. Kiểm tra module-info.java có requires Ikonli modules
2. Verify dependencies trong pom.xml
3. Clean và rebuild project: `mvn clean compile`

### Icon bị lỗi font
1. Đảm bảo icon literal đúng format: `fas-icon-name`
2. Check icon có tồn tại trong bộ icon
3. Verify prefix đúng (fas-, mdi2-, bi-)

### Icon size không đúng
```java
// Kiểm tra setIconSize được gọi
fontIcon.setIconSize(20); // ✅

// Không dùng setSize (method của Node)
fontIcon.setSize(20); // ❌ Wrong method
```

---

## 📝 Examples

### Login Screen
```xml
<HBox alignment="CENTER_LEFT" spacing="5">
    <FontIcon iconLiteral="fas-user" iconSize="16" iconColor="#757575"/>
    <TextField promptText="Email" />
</HBox>

<HBox alignment="CENTER_LEFT" spacing="5">
    <FontIcon iconLiteral="fas-lock" iconSize="16" iconColor="#757575"/>
    <PasswordField promptText="Password"/>
</HBox>

<Button text="Đăng Nhập">
    <graphic>
        <FontIcon iconLiteral="fas-sign-in-alt" iconSize="16" iconColor="white"/>
    </graphic>
</Button>
```

### Menu Buttons
```java
Button questionBankBtn = new Button("Ngân hàng Câu hỏi");
questionBankBtn.setGraphic(IconFactory.createQuestionBankIcon());

Button examBtn = new Button("Quản lý Đề thi");
examBtn. setGraphic(IconFactory.createExamIcon());

Button gradingBtn = new Button("Chấm bài");
gradingBtn.setGraphic(IconFactory.createGradingIcon());
```

### Toolbar Actions
```java
Button addBtn = new Button();
addBtn.setGraphic(IconFactory.createAddIcon());
addBtn.setTooltip(new Tooltip("Thêm mới"));

Button editBtn = new Button();
editBtn.setGraphic(IconFactory. createEditIcon());
editBtn. setTooltip(new Tooltip("Chỉnh sửa"));

Button deleteBtn = new Button();
deleteBtn.setGraphic(IconFactory.createDeleteIcon());
deleteBtn.setTooltip(new Tooltip("Xóa"));
```

---

## 🔗 Resources

- **Ikonli Documentation:** https://kordamp.org/ikonli/
- **FontAwesome Icons:** https://fontawesome.com/v5/search
- **Material Design Icons:** https://pictogrammers.com/library/mdi/
- **Bootstrap Icons:** https://icons.getbootstrap.com/
- **JavaFX CSS Reference:** https://openjfx. io/javadoc/21/javafx. graphics/javafx/scene/doc-files/cssref.html

---

**Author:** K24DTCN210-NVMANH
**Date:** 27/11/2025
**Project:** MS. TrustTest - Online Exam System

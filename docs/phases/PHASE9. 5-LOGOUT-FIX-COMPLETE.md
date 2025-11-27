# Phase 9.5: Logout Bug Fix - Complete

## Tổng Quan
Sửa lỗi khi đăng xuất từ Teacher Dashboard về màn hình login:
- **Lỗi 1**: Giao diện login bị mất CSS
- **Lỗi 2**: apiClient null khi login lại → NullPointerException

## Root Cause Analysis

### Vấn đề
Trong `TeacherMainController.backToLogin()`:
```java
private void backToLogin() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);  // ❌ Không apply CSS
        stage.setScene(scene);
        // ❌ Không gọi controller. initialize(apiClient, stage)
    }
}
```

**Hậu quả:**
1. Scene mới không có CSS → mất style
2. LoginController được tạo mới nhưng không được initialize → `apiClient = null`
3. Khi login lại → `apiClient. login()` → NullPointerException

## Solution Implementation

### 1. Refactor ExamClientApplication

**File**: `client-javafx/src/main/java/com/mstrust/client/exam/ExamClientApplication.java`

#### Thay đổi 1: Public showLoginScreen()
```java
// BEFORE: private void showLoginScreen()
// AFTER:
public void showLoginScreen() {
    // Khởi tạo đầy đủ: controller, apiClient, CSS, centering
}
```

#### Thay đổi 2: Store Application trong Stage. userData
```java
private void configureStage() {
    primaryStage.setTitle("MS. TrustTest - Hệ Thống Thi Trực Tuyến");
    primaryStage.setMinWidth(800);
    primaryStage.setMinHeight(600);
    
    // ✅ Lưu application instance để các controller có thể access
    primaryStage.setUserData(this);
    
    primaryStage.setOnCloseRequest(this::handleExit);
}
```

### 2. Update TeacherMainController

**File**: `client-javafx/src/main/java/com/mstrust/client/teacher/controller/TeacherMainController.java`

#### Thay đổi 1: Add Application Reference
```java
private com.mstrust.client.exam.ExamClientApplication application;
```

#### Thay đổi 2: Extract Application từ Stage
```java
public void setStage(Stage stage) {
    this.stage = stage;
    // Extract application instance từ stage userData
    if (stage. getUserData() instanceof com.mstrust.client.exam.ExamClientApplication) {
        this.application = (com.mstrust.client.exam.ExamClientApplication) stage.getUserData();
    }
}
```

#### Thay đổi 3: Use Application. showLoginScreen()
```java
private void backToLogin() {
    if (application != null) {
        // ✅ Gọi lại showLoginScreen() từ Application
        // → Đảm bảo khởi tạo đúng: controller, apiClient, CSS
        application.showLoginScreen();
    } else {
        showError("Lỗi", "Không thể quay lại màn hình đăng nhập!");
    }
}
```

## Architecture Pattern

```
┌─────────────────────────────────────────────────┐
│         ExamClientApplication                    │
│  ┌─────────────────────────────────────────┐   │
│  │  primaryStage.setUserData(this)         │   │
│  │  ↓                                       │   │
│  │  Stage.userData = Application instance  │   │
│  └─────────────────────────────────────────┘   │
│                                                  │
│  public void showLoginScreen() {                │
│    - Load login. fxml                            │
│    - Initialize controller với apiClient       │
│    - Apply CSS                                  │
│    - Center window                              │
│  }                                              │
└─────────────────────────────────────────────────┘
                       ↑
                       │ calls on logout
                       │
┌─────────────────────────────────────────────────┐
│        TeacherMainController                     │
│  ┌─────────────────────────────────────────┐   │
│  │  setStage(stage) {                      │   │
│  │    this.application = stage.getUserData()│  │
│  │  }                                       │   │
│  └─────────────────────────────────────────┘   │
│                                                  │
│  private void backToLogin() {                   │
│    application.showLoginScreen();              │
│  }                                              │
└─────────────────────────────────────────────────┘
```

## Benefits

1. **Single Responsibility**: `showLoginScreen()` là single source of truth cho login screen initialization
2. **DRY Principle**: Không duplicate login initialization logic
3. **Consistent State**: Đảm bảo login screen luôn được khởi tạo đúng cách
4. **Maintainability**: Thay đổi login initialization chỉ cần sửa 1 nơi

## Testing Guide

### Test Case 1: Logout từ Teacher Dashboard

**Steps:**
1. Chạy application: `client-javafx\run-exam-client.bat`
2. Login với teacher account: `admin@gmail.com` / `password`
3. Click nút "Đăng xuất" ở góc trên bên phải
4. Confirm trong dialog
5. **Verify**: 
   - ✅ Quay về màn hình login
   - ✅ Giao diện login có đầy đủ style (background gradient, button rounded)
   - ✅ Window được center trên màn hình
   - ✅ Không có error trong console

### Test Case 2: Login lại sau Logout

**Steps:**
1.  Sau khi logout (từ Test Case 1)
2. Login lại với cùng account: `admin@gmail.com` / `password`
3. **Verify**:
   - ✅ Login thành công
   - ✅ Không có NullPointerException
   - ✅ Vào được Teacher Dashboard bình thường
   - ✅ Tất cả chức năng hoạt động đúng

### Test Case 3: Multiple Logout/Login Cycles

**Steps:**
1.  Login → Logout → Login → Logout → Login
2.  Lặp lại 3-5 lần
3. **Verify**:
   - ✅ Mỗi lần đều hoạt động đúng
   - ✅ Không memory leak
   - ✅ Không warning trong console

## Files Modified

```
client-javafx/src/main/java/com/mstrust/client/exam/
└── ExamClientApplication.java
    - showLoginScreen(): private → public
    - configureStage(): added primaryStage.setUserData(this)

client-javafx/src/main/java/com/mstrust/client/teacher/controller/
└── TeacherMainController.java
    - Added: private ExamClientApplication application
    - Modified: setStage() to extract application from userData
    - Modified: backToLogin() to use application.showLoginScreen()
```

## Compilation Status

```bash
cd client-javafx && mvn clean compile
# Result: BUILD SUCCESS
```

## Notes

- Pattern này có thể áp dụng cho logout từ Student ExamList nếu cần
- Không cần modify LoginController vì nó đã hoạt động đúng
- CSS và dependencies đều được handle bởi showLoginScreen()

---
**Created**: 27/11/2025 15:35  
**Author**: K24DTCN210-NVMANH  
**Status**: ✅ COMPLETE & TESTED

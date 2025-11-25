# Phase 8.6 - Step 2: Full-Screen Security Implementation Plan

**Created:** 24/11/2025 09:08
**Author:** K24DTCN210-NVMANH
**Status:** 📋 PLANNING
**Estimated Time:** 2 giờ

## 🎯 Mục Tiêu

Implement Full-Screen Security features để đảm bảo học sinh không thể thoát khỏi màn hình thi:
1. **Full-screen mode** khi bắt đầu thi
2. **Keyboard blocking** (Alt+Tab, Windows key, Alt+F4...)
3. **Exit protection** - phải confirm mới được thoát
4. **Tích hợp** vào ExamTakingController

## 📋 Implementation Checklist

### Phase 2.1: Add JNA Dependency (15 phút)
- [ ] Thêm JNA vào client-javafx/pom.xml
- [ ] Thêm JNA Platform dependency
- [ ] Update module-info.java
- [ ] Test Maven compile

### Phase 2.2: FullScreenLockService (45 phút)
- [ ] Tạo FullScreenLockService.java
- [ ] Implement enableFullScreen()
- [ ] Implement disableFullScreen()
- [ ] Handle full-screen events
- [ ] Add logging

### Phase 2.3: KeyboardBlocker với JNA (45 phút)
- [ ] Tạo KeyboardBlocker.java
- [ ] Implement Windows keyboard hook
- [ ] Block Alt+Tab
- [ ] Block Windows key
- [ ] Block Alt+F4
- [ ] Block Ctrl+Esc
- [ ] Handle cleanup

### Phase 2.4: Integration (15 phút)
- [ ] Integrate vào ExamTakingController
- [ ] Enable khi startExam()
- [ ] Disable khi submitExam()
- [ ] Add error handling
- [ ] Test full flow

## 🔧 Technical Implementation

### 1. JNA Dependency

**File:** `client-javafx/pom.xml`

```xml
<dependencies>
    <!-- Existing dependencies... -->
    
    <!-- JNA for native keyboard/window control -->
    <dependency>
        <groupId>net.java.dev.jna</groupId>
        <artifactId>jna</artifactId>
        <version>5.13.0</version>
    </dependency>
    <dependency>
        <groupId>net.java.dev.jna</groupId>
        <artifactId>jna-platform</artifactId>
        <version>5.13.0</version>
    </dependency>
</dependencies>
```

### 2. Module Info Update

**File:** `client-javafx/src/main/java/module-info.java`

```java
module com.mstrust.client.exam {
    // Existing requirements...
    
    // JNA modules
    requires com.sun.jna;
    requires com.sun.jna.platform;
}
```

### 3. FullScreenLockService

**File:** `client-javafx/src/main/java/com/mstrust/client/exam/service/FullScreenLockService.java`

```java
package com.mstrust.client.exam.service;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* ---------------------------------------------------
 * Service quản lý chế độ full-screen trong khi thi
 * Đảm bảo học sinh không thể thoát khỏi màn hình thi
 * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
 * --------------------------------------------------- */
public class FullScreenLockService {
    private static final Logger logger = LoggerFactory.getLogger(FullScreenLockService.class);
    
    private Stage stage;
    private boolean isLocked = false;
    private KeyboardBlocker keyboardBlocker;
    
    /* ---------------------------------------------------
     * Constructor
     * @param stage JavaFX Stage cần lock
     * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
     * --------------------------------------------------- */
    public FullScreenLockService(Stage stage) {
        this.stage = stage;
        this.keyboardBlocker = new KeyboardBlocker();
    }
    
    /* ---------------------------------------------------
     * Bật chế độ full-screen và lock keyboard
     * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
     * --------------------------------------------------- */
    public void enableFullScreen() {
        if (isLocked) {
            logger.warn("Full-screen already locked");
            return;
        }
        
        try {
            // Set full-screen
            stage.setFullScreen(true);
            stage.setFullScreenExitHint(""); // Hide exit hint
            
            // Prevent full-screen exit
            stage.setFullScreenExitKeyCombination(null);
            
            // Block keyboard shortcuts
            keyboardBlocker.install();
            
            isLocked = true;
            logger.info("Full-screen mode enabled and locked");
            
        } catch (Exception e) {
            logger.error("Failed to enable full-screen lock", e);
            throw new RuntimeException("Không thể bật chế độ full-screen", e);
        }
    }
    
    /* ---------------------------------------------------
     * Tắt chế độ full-screen và unlock keyboard
     * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
     * --------------------------------------------------- */
    public void disableFullScreen() {
        if (!isLocked) {
            logger.warn("Full-screen not locked");
            return;
        }
        
        try {
            // Unblock keyboard
            keyboardBlocker.uninstall();
            
            // Exit full-screen
            stage.setFullScreen(false);
            
            isLocked = false;
            logger.info("Full-screen mode disabled and unlocked");
            
        } catch (Exception e) {
            logger.error("Failed to disable full-screen lock", e);
        }
    }
    
    /* ---------------------------------------------------
     * Kiểm tra trạng thái lock
     * @return true nếu đang lock
     * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
     * --------------------------------------------------- */
    public boolean isLocked() {
        return isLocked;
    }
    
    /* ---------------------------------------------------
     * Cleanup resources
     * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
     * --------------------------------------------------- */
    public void cleanup() {
        if (isLocked) {
            disableFullScreen();
        }
    }
}
```

### 4. KeyboardBlocker

**File:** `client-javafx/src/main/java/com/mstrust/client/exam/service/KeyboardBlocker.java`

```java
package com.mstrust.client.exam.service;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* ---------------------------------------------------
 * Service block các phím tắt hệ thống trong khi thi
 * Sử dụng JNA để hook keyboard events trên Windows
 * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
 * --------------------------------------------------- */
public class KeyboardBlocker {
    private static final Logger logger = LoggerFactory.getLogger(KeyboardBlocker.class);
    
    private WinUser.HHOOK keyboardHook;
    private boolean isInstalled = false;
    
    // Virtual key codes
    private static final int VK_TAB = 0x09;
    private static final int VK_ESCAPE = 0x1B;
    private static final int VK_LWIN = 0x5B;
    private static final int VK_RWIN = 0x5C;
    
    /* ---------------------------------------------------
     * Cài đặt keyboard hook để block các phím
     * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
     * --------------------------------------------------- */
    public void install() {
        if (isInstalled) {
            logger.warn("Keyboard blocker already installed");
            return;
        }
        
        try {
            WinUser.LowLevelKeyboardProc keyboardProc = new WinUser.LowLevelKeyboardProc() {
                @Override
                public LRESULT callback(int nCode, WPARAM wParam, WinUser.KBDLLHOOKSTRUCT info) {
                    if (nCode >= 0) {
                        boolean block = shouldBlockKey(info.vkCode, info.flags);
                        
                        if (block) {
                            logger.debug("Blocked key: {}", info.vkCode);
                            return new LRESULT(1); // Block the key
                        }
                    }
                    
                    return User32.INSTANCE.CallNextHookEx(keyboardHook, nCode, wParam, 
                                                          new LPARAM(info.getPointer().getLong(0)));
                }
            };
            
            keyboardHook = User32.INSTANCE.SetWindowsHookEx(
                WinUser.WH_KEYBOARD_LL, 
                keyboardProc,
                Kernel32.INSTANCE.GetModuleHandle(null), 
                0
            );
            
            isInstalled = true;
            logger.info("Keyboard blocker installed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to install keyboard blocker", e);
            throw new RuntimeException("Không thể cài đặt keyboard blocker", e);
        }
    }
    
    /* ---------------------------------------------------
     * Gỡ bỏ keyboard hook
     * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
     * --------------------------------------------------- */
    public void uninstall() {
        if (!isInstalled) {
            logger.warn("Keyboard blocker not installed");
            return;
        }
        
        try {
            if (keyboardHook != null) {
                User32.INSTANCE.UnhookWindowsHookEx(keyboardHook);
                keyboardHook = null;
            }
            
            isInstalled = false;
            logger.info("Keyboard blocker uninstalled successfully");
            
        } catch (Exception e) {
            logger.error("Failed to uninstall keyboard blocker", e);
        }
    }
    
    /* ---------------------------------------------------
     * Kiểm tra xem phím có nên bị block không
     * @param vkCode Virtual key code
     * @param flags Key flags
     * @return true nếu cần block
     * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
     * --------------------------------------------------- */
    private boolean shouldBlockKey(int vkCode, int flags) {
        // Check for Alt key pressed
        boolean altPressed = (flags & 0x20) != 0;
        
        // Block Alt+Tab
        if (altPressed && vkCode == VK_TAB) {
            return true;
        }
        
        // Block Alt+F4
        if (altPressed && vkCode == 0x73) { // F4
            return true;
        }
        
        // Block Windows key
        if (vkCode == VK_LWIN || vkCode == VK_RWIN) {
            return true;
        }
        
        // Block Ctrl+Esc (Start menu)
        // Note: This is tricky and may need additional handling
        
        return false;
    }
    
    /* ---------------------------------------------------
     * Kiểm tra trạng thái installation
     * @return true nếu đã installed
     * @author: K24DTCN210-NVMANH (24/11/2025 09:08)
     * --------------------------------------------------- */
    public boolean isInstalled() {
        return isInstalled;
    }
}
```

### 5. Integration vào ExamTakingController

**File:** `client-javafx/src/main/java/com/mstrust/client/exam/controller/ExamTakingController.java`

**Thêm field:**
```java
private FullScreenLockService fullScreenLockService;
```

**Trong initialize():**
```java
// Initialize full-screen lock service
this.fullScreenLockService = new FullScreenLockService(stage);
```

**Trong startExam():**
```java
// Enable full-screen lock
try {
    fullScreenLockService.enableFullScreen();
    logger.info("Full-screen lock enabled for exam");
} catch (Exception e) {
    logger.error("Failed to enable full-screen lock", e);
    showAlert("Cảnh báo", "Không thể bật chế độ full-screen. Vẫn có thể làm bài.");
}
```

**Trong submitExam():**
```java
// Disable full-screen lock
fullScreenLockService.disableFullScreen();
logger.info("Full-screen lock disabled after exam submission");
```

**Trong cleanup/exit:**
```java
@Override
public void cleanup() {
    // ... existing cleanup ...
    
    // Cleanup full-screen lock
    if (fullScreenLockService != null) {
        fullScreenLockService.cleanup();
    }
}
```

## ⚠️ Important Considerations

### Windows-Only Implementation
- JNA keyboard hooks chỉ hoạt động trên Windows
- Cần check OS trước khi enable
- Trên macOS/Linux: chỉ dùng JavaFX full-screen

### Permissions
- Có thể cần admin rights để hook keyboard
- Test với quyền user thông thường

### Testing Challenges
- Khó test khi keyboard bị block
- Cần cơ chế "emergency exit" cho developer
- Có thể dùng timer để auto-disable sau X phút

### User Experience
- Cần thông báo rõ ràng cho học sinh
- Giải thích tại sao full-screen
- Hướng dẫn cách hoàn thành bài thi để thoát

## 🧪 Testing Plan

### Test Cases

**TC1: Enable Full-Screen**
```
GIVEN Exam taking screen
WHEN Click "Bắt đầu thi"
THEN Window enters full-screen mode
AND Keyboard shortcuts are blocked
AND Exit hint is hidden
```

**TC2: Block Alt+Tab**
```
GIVEN Full-screen mode enabled
WHEN Press Alt+Tab
THEN Nothing happens (blocked)
AND Exam screen remains
```

**TC3: Block Windows Key**
```
GIVEN Full-screen mode enabled
WHEN Press Windows key
THEN Start menu does NOT open
AND Exam screen remains
```

**TC4: Block Alt+F4**
```
GIVEN Full-screen mode enabled
WHEN Press Alt+F4
THEN Window does NOT close
AND Exam screen remains
```

**TC5: Disable Full-Screen**
```
GIVEN Full-screen mode enabled
WHEN Submit exam
THEN Full-screen exits
AND Keyboard shortcuts work again
AND Window is normal
```

**TC6: Emergency Exit**
```
GIVEN Full-screen mode enabled (for dev testing)
WHEN Press secret combination (e.g., Ctrl+Shift+F12)
THEN Full-screen can be disabled
AND Developer can exit
```

## 📝 Success Criteria

- [ ] JNA dependency added và compile OK
- [ ] FullScreenLockService implement đầy đủ
- [ ] KeyboardBlocker hoạt động trên Windows
- [ ] Tích hợp vào ExamTakingController
- [ ] Alt+Tab bị block
- [ ] Windows key bị block
- [ ] Alt+F4 bị block
- [ ] Có thể thoát khi submit exam
- [ ] Không có memory leak
- [ ] Logging đầy đủ

## 🚀 Next Steps After Step 2

**Step 3: Exit Protection & Polish**
- Exit Confirmation Dialog
- Loading Indicators
- Keyboard Shortcuts (cho navigation trong exam)
- Accessibility features

## 📚 References

- JNA Documentation: https://github.com/java-native-access/jna
- Windows Hook Types: https://docs.microsoft.com/en-us/windows/win32/winmsg/about-hooks
- JavaFX Full-Screen: https://openjfx.io/javadoc/17/javafx.graphics/javafx/stage/Stage.html#setFullScreen(boolean)

---

**Status:** 📋 READY TO IMPLEMENT
**Priority:** HIGH
**Complexity:** MEDIUM-HIGH (Windows native code)

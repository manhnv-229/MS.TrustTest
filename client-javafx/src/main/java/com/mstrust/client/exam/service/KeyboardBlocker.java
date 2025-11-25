package com.mstrust.client.exam.service;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* ---------------------------------------------------
 * Service block các phím tắt hệ thống trong khi thi
 * Sử dụng JNA để hook keyboard events trên Windows
 * @author: K24DTCN210-NVMANH (24/11/2025 09:11)
 * --------------------------------------------------- */
public class KeyboardBlocker {
    private static final Logger logger = LoggerFactory.getLogger(KeyboardBlocker.class);
    
    private WinUser.HHOOK keyboardHook;
    private WinUser.LowLevelKeyboardProc keyboardProc;
    private boolean isInstalled = false;
    
    // Virtual key codes
    private static final int VK_TAB = 0x09;
    private static final int VK_ESCAPE = 0x1B;
    private static final int VK_LWIN = 0x5B;
    private static final int VK_RWIN = 0x5C;
    private static final int VK_F4 = 0x73;
    
    // Modifier flags
    private static final int LLKHF_ALTDOWN = 0x20;
    
    /* ---------------------------------------------------
     * Cài đặt keyboard hook để block các phím
     * @author: K24DTCN210-NVMANH (24/11/2025 09:11)
     * --------------------------------------------------- */
    public void install() {
        if (isInstalled) {
            logger.warn("Keyboard blocker already installed");
            return;
        }
        
        try {
            // Create keyboard hook procedure
            keyboardProc = new WinUser.LowLevelKeyboardProc() {
                @Override
                public LRESULT callback(int nCode, WPARAM wParam, WinUser.KBDLLHOOKSTRUCT info) {
                    if (nCode >= 0) {
                        boolean block = shouldBlockKey(info.vkCode, info.flags);
                        
                        if (block) {
                            logger.debug("Blocked key: vkCode={}, flags={}", info.vkCode, info.flags);
                            return new LRESULT(1); // Block the key
                        }
                    }
                    
                    // Pass to next hook
                    return User32.INSTANCE.CallNextHookEx(keyboardHook, nCode, wParam, 
                                                          new LPARAM(Pointer.nativeValue(info.getPointer())));
                }
            };
            
            // Install low-level keyboard hook
            // For WH_KEYBOARD_LL, hMod parameter can be null (hook is not associated with a DLL)
            keyboardHook = User32.INSTANCE.SetWindowsHookEx(
                WinUser.WH_KEYBOARD_LL, 
                keyboardProc,
                null, 
                0
            );
            
            if (keyboardHook == null) {
                throw new RuntimeException("Failed to install keyboard hook");
            }
            
            isInstalled = true;
            logger.info("Keyboard blocker installed successfully");
            
        } catch (Exception e) {
            logger.error("Failed to install keyboard blocker", e);
            throw new RuntimeException("Không thể cài đặt keyboard blocker", e);
        }
    }
    
    /* ---------------------------------------------------
     * Gỡ bỏ keyboard hook
     * @author: K24DTCN210-NVMANH (24/11/2025 09:11)
     * --------------------------------------------------- */
    public void uninstall() {
        if (!isInstalled) {
            logger.warn("Keyboard blocker not installed");
            return;
        }
        
        try {
            if (keyboardHook != null) {
                boolean success = User32.INSTANCE.UnhookWindowsHookEx(keyboardHook);
                if (success) {
                    logger.info("Keyboard blocker uninstalled successfully");
                } else {
                    logger.warn("Failed to uninstall keyboard blocker");
                }
                keyboardHook = null;
            }
            
            keyboardProc = null;
            isInstalled = false;
            
        } catch (Exception e) {
            logger.error("Failed to uninstall keyboard blocker", e);
        }
    }
    
    /* ---------------------------------------------------
     * Kiểm tra xem phím có nên bị block không
     * @param vkCode Virtual key code
     * @param flags Key flags
     * @return true nếu cần block
     * @author: K24DTCN210-NVMANH (24/11/2025 09:11)
     * --------------------------------------------------- */
    private boolean shouldBlockKey(int vkCode, int flags) {
        // Check for Alt key pressed
        boolean altPressed = (flags & LLKHF_ALTDOWN) != 0;
        
        // Block Alt+Tab
        if (altPressed && vkCode == VK_TAB) {
            logger.debug("Blocking Alt+Tab");
            return true;
        }
        
        // Block Alt+F4
        if (altPressed && vkCode == VK_F4) {
            logger.debug("Blocking Alt+F4");
            return true;
        }
        
        // Block Alt+Esc (another task switcher)
        if (altPressed && vkCode == VK_ESCAPE) {
            logger.debug("Blocking Alt+Esc");
            return true;
        }
        
        // Block Windows key
        if (vkCode == VK_LWIN || vkCode == VK_RWIN) {
            logger.debug("Blocking Windows key");
            return true;
        }
        
        // Note: Ctrl+Esc is harder to block as it's handled at kernel level
        // May need additional handling if required
        
        return false;
    }
    
    /* ---------------------------------------------------
     * Kiểm tra trạng thái installation
     * @return true nếu đã installed
     * @author: K24DTCN210-NVMANH (24/11/2025 09:11)
     * --------------------------------------------------- */
    public boolean isInstalled() {
        return isInstalled;
    }
}

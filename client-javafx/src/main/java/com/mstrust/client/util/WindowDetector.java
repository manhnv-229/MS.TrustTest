package com.mstrust.client.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* ---------------------------------------------------
 * Utility class để detect active window trên Windows
 * Sử dụng JNA để gọi Windows API
 * @author: K24DTCN210-NVMANH (21/11/2025 11:19)
 * --------------------------------------------------- */
public class WindowDetector {
    private static final Logger logger = LoggerFactory.getLogger(WindowDetector.class);
    private static final int MAX_TITLE_LENGTH = 1024;

    /* ---------------------------------------------------
     * Lấy tiêu đề của cửa sổ đang active
     * @returns Tiêu đề cửa sổ, hoặc "Unknown" nếu không lấy được
     * @author: K24DTCN210-NVMANH (21/11/2025 11:19)
     * --------------------------------------------------- */
    public static String getActiveWindowTitle() {
        try {
            if (!isWindows()) {
                logger.warn("WindowDetector chỉ hỗ trợ Windows");
                return "Non-Windows OS";
            }

            HWND hwnd = User32.INSTANCE.GetForegroundWindow();
            if (hwnd == null) {
                logger.warn("Không thể lấy foreground window");
                return "Unknown";
            }

            char[] buffer = new char[MAX_TITLE_LENGTH];
            int length = User32.INSTANCE.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
            
            if (length > 0) {
                String title = Native.toString(buffer);
                logger.debug("Active window: {}", title);
                return title;
            } else {
                return "No Title";
            }

        } catch (Exception e) {
            logger.error("Lỗi khi lấy active window title", e);
            return "Error";
        }
    }

    /* ---------------------------------------------------
     * Lấy process ID của cửa sổ đang active
     * @returns Process ID, hoặc -1 nếu không lấy được
     * @author: K24DTCN210-NVMANH (21/11/2025 11:19)
     * --------------------------------------------------- */
    public static int getActiveWindowProcessId() {
        try {
            if (!isWindows()) {
                return -1;
            }

            HWND hwnd = User32.INSTANCE.GetForegroundWindow();
            if (hwnd == null) {
                return -1;
            }

            IntByReference pid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);
            
            int processId = pid.getValue();
            logger.debug("Active window process ID: {}", processId);
            return processId;

        } catch (Exception e) {
            logger.error("Lỗi khi lấy process ID", e);
            return -1;
        }
    }

    /* ---------------------------------------------------
     * Kiểm tra xem có thể sử dụng WindowDetector không
     * @returns true nếu đang chạy trên Windows và JNA available
     * @author: K24DTCN210-NVMANH (21/11/2025 11:19)
     * --------------------------------------------------- */
    public static boolean isAvailable() {
        try {
            if (!isWindows()) {
                return false;
            }

            User32.INSTANCE.GetForegroundWindow();
            return true;

        } catch (Exception e) {
            logger.error("WindowDetector không available", e);
            return false;
        }
    }

    /* ---------------------------------------------------
     * Kiểm tra OS có phải Windows không
     * @returns true nếu là Windows
     * @author: K24DTCN210-NVMANH (21/11/2025 11:45)
     * --------------------------------------------------- */
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }
}

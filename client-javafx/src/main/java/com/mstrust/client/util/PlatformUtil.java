package com.mstrust.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/* ---------------------------------------------------
 * Utility class cho cross-platform operations
 * Hỗ trợ Windows, macOS, Linux
 * @author: K24DTCN210-NVMANH (01/12/2025 10:05)
 * --------------------------------------------------- */
public class PlatformUtil {
    private static final Logger logger = LoggerFactory.getLogger(PlatformUtil.class);
    
    /* ---------------------------------------------------
     * Kiểm tra OS có phải Windows không
     * @returns true nếu là Windows
     * @author: K24DTCN210-NVMANH (01/12/2025 10:05)
     * --------------------------------------------------- */
    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }
    
    /* ---------------------------------------------------
     * Kiểm tra OS có phải macOS không
     * @returns true nếu là macOS
     * @author: K24DTCN210-NVMANH (01/12/2025 10:05)
     * --------------------------------------------------- */
    public static boolean isMacOS() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("mac");
    }
    
    /* ---------------------------------------------------
     * Kiểm tra OS có phải Linux không
     * @returns true nếu là Linux
     * @author: K24DTCN210-NVMANH (01/12/2025 10:05)
     * --------------------------------------------------- */
    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("linux") || os.contains("unix");
    }
    
    /* ---------------------------------------------------
     * Chụp màn hình (cross-platform)
     * @returns BufferedImage của screenshot
     * @author: K24DTCN210-NVMANH (01/12/2025 10:05)
     * --------------------------------------------------- */
    public static BufferedImage captureScreen() {
        try {
            if (isWindows() || isLinux()) {
                // Windows và Linux: Dùng Java Robot API
                Robot robot = new Robot();
                Rectangle screenRect = new Rectangle(
                    Toolkit.getDefaultToolkit().getScreenSize()
                );
                return robot.createScreenCapture(screenRect);
            } else if (isMacOS()) {
                // macOS: Dùng screencapture command
                return captureScreenMacOS();
            } else {
                logger.warn("Unsupported OS for screen capture");
                return null;
            }
        } catch (Exception e) {
            logger.error("Error capturing screen", e);
            return null;
        }
    }
    
    /* ---------------------------------------------------
     * Chụp màn hình trên macOS sử dụng screencapture command
     * @returns BufferedImage của screenshot
     * @author: K24DTCN210-NVMANH (01/12/2025 10:05)
     * --------------------------------------------------- */
    private static BufferedImage captureScreenMacOS() {
        try {
            // macOS screencapture command
            Process process = new ProcessBuilder("screencapture", "-x", "-t", "jpg", "-")
                .redirectErrorStream(true)
                .start();
            
            // Read image bytes from stdout
            byte[] imageBytes = process.getInputStream().readAllBytes();
            process.waitFor();
            
            // Convert bytes to BufferedImage
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes);
            return javax.imageio.ImageIO.read(bais);
        } catch (Exception e) {
            logger.error("Error capturing screen on macOS", e);
            // Fallback to Robot API
            try {
                Robot robot = new Robot();
                Rectangle screenRect = new Rectangle(
                    Toolkit.getDefaultToolkit().getScreenSize()
                );
                return robot.createScreenCapture(screenRect);
            } catch (Exception ex) {
                logger.error("Fallback screen capture also failed", ex);
                return null;
            }
        }
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách processes đang chạy (cross-platform)
     * @returns List tên processes
     * @author: K24DTCN210-NVMANH (01/12/2025 10:05)
     * --------------------------------------------------- */
    public static List<String> getProcessList() {
        List<String> processes = new ArrayList<>();
        
        try {
            if (isWindows()) {
                // Windows: tasklist command
                Process process = new ProcessBuilder("tasklist", "/FO", "CSV", "/NH")
                    .redirectErrorStream(true)
                    .start();
                
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Parse CSV: "process.exe","PID","Session","Mem Usage"
                        String[] parts = line.split(",");
                        if (parts.length > 0) {
                            String processName = parts[0].replace("\"", "");
                            if (!processName.isEmpty()) {
                                processes.add(processName.toLowerCase());
                            }
                        }
                    }
                }
                process.waitFor();
            } else if (isMacOS() || isLinux()) {
                // macOS/Linux: ps command
                Process process = new ProcessBuilder("ps", "-eo", "comm")
                    .redirectErrorStream(true)
                    .start();
                
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                    String line;
                    boolean firstLine = true;
                    while ((line = reader.readLine()) != null) {
                        if (firstLine) {
                            firstLine = false; // Skip header
                            continue;
                        }
                        String processName = line.trim();
                        if (!processName.isEmpty()) {
                            processes.add(processName.toLowerCase());
                        }
                    }
                }
                process.waitFor();
            }
            
            logger.debug("Found {} processes", processes.size());
        } catch (Exception e) {
            logger.error("Error getting process list", e);
            // Fallback to Java ProcessHandle API
            return ProcessDetector.getRunningProcesses();
        }
        
        return processes;
    }
    
    /* ---------------------------------------------------
     * Lấy screen resolution
     * @returns String format "WIDTHxHEIGHT"
     * @author: K24DTCN210-NVMANH (01/12/2025 10:05)
     * --------------------------------------------------- */
    public static String getScreenResolution() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return screenSize.width + "x" + screenSize.height;
        } catch (Exception e) {
            logger.error("Error getting screen resolution", e);
            return "Unknown";
        }
    }
}


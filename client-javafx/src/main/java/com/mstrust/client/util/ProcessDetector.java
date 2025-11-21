package com.mstrust.client.util;

import com.mstrust.client.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Utility class để detect running processes
 * Kiểm tra blacklisted processes đang chạy
 * @author: K24DTCN210-NVMANH (21/11/2025 11:21)
 * --------------------------------------------------- */
public class ProcessDetector {
    private static final Logger logger = LoggerFactory.getLogger(ProcessDetector.class);
    private static final AppConfig config = AppConfig.getInstance();

    /* ---------------------------------------------------
     * Lấy danh sách tất cả processes đang chạy
     * Sử dụng Java 9+ ProcessHandle API
     * @returns List tên processes
     * @author: K24DTCN210-NVMANH (21/11/2025 11:21)
     * --------------------------------------------------- */
    public static List<String> getRunningProcesses() {
        List<String> processes = new ArrayList<>();
        
        try {
            ProcessHandle.allProcesses()
                .forEach(process -> {
                    process.info().command().ifPresent(cmd -> {
                        String processName = extractProcessName(cmd);
                        if (processName != null && !processName.isEmpty()) {
                            processes.add(processName);
                        }
                    });
                });
                
            logger.debug("Found {} running processes", processes.size());
            
        } catch (Exception e) {
            logger.error("Error getting running processes", e);
        }
        
        return processes;
    }

    /* ---------------------------------------------------
     * Kiểm tra có blacklisted process nào đang chạy không
     * @returns List các blacklisted processes đang chạy
     * @author: K24DTCN210-NVMANH (21/11/2025 11:21)
     * --------------------------------------------------- */
    public static List<String> getBlacklistedProcesses() {
        List<String> runningProcesses = getRunningProcesses();
        
        return runningProcesses.stream()
            .filter(process -> config.isProcessBlacklisted(process))
            .collect(Collectors.toList());
    }

    /* ---------------------------------------------------
     * Kiểm tra một process cụ thể có đang chạy không
     * @param processName Tên process cần check
     * @returns true nếu process đang chạy
     * @author: K24DTCN210-NVMANH (21/11/2025 11:21)
     * --------------------------------------------------- */
    public static boolean isProcessRunning(String processName) {
        if (processName == null || processName.isEmpty()) {
            return false;
        }
        
        String normalizedName = processName.toLowerCase();
        
        return getRunningProcesses().stream()
            .anyMatch(p -> p.toLowerCase().contains(normalizedName));
    }

    /* ---------------------------------------------------
     * Extract process name từ command path
     * VD: "C:\Program Files\TeamViewer\TeamViewer.exe" -> "teamviewer"
     * @param command Full command path
     * @returns Process name
     * @author: K24DTCN210-NVMANH (21/11/2025 11:21)
     * --------------------------------------------------- */
    private static String extractProcessName(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }
        
        // Get filename from path
        String filename = command;
        int lastSeparator = Math.max(
            command.lastIndexOf('/'),
            command.lastIndexOf('\\')
        );
        
        if (lastSeparator >= 0 && lastSeparator < command.length() - 1) {
            filename = command.substring(lastSeparator + 1);
        }
        
        // Remove extension
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            filename = filename.substring(0, dotIndex);
        }
        
        return filename.toLowerCase();
    }

    /* ---------------------------------------------------
     * Get thông tin chi tiết của một process
     * @param pid Process ID
     * @returns String mô tả process
     * @author: K24DTCN210-NVMANH (21/11/2025 11:21)
     * --------------------------------------------------- */
    public static String getProcessInfo(long pid) {
        try {
            ProcessHandle process = ProcessHandle.of(pid).orElse(null);
            if (process == null) {
                return "Process not found";
            }
            
            ProcessHandle.Info info = process.info();
            StringBuilder sb = new StringBuilder();
            sb.append("PID: ").append(pid);
            
            info.command().ifPresent(cmd -> 
                sb.append(", Command: ").append(cmd));
            info.user().ifPresent(user -> 
                sb.append(", User: ").append(user));
            
            return sb.toString();
            
        } catch (Exception e) {
            logger.error("Error getting process info for PID: {}", pid, e);
            return "Error";
        }
    }

    /* ---------------------------------------------------
     * Test method - in ra running processes
     * @author: K24DTCN210-NVMANH (21/11/2025 11:21)
     * --------------------------------------------------- */
    public static void printRunningProcesses() {
        System.out.println("=== Running Processes ===");
        List<String> processes = getRunningProcesses();
        processes.forEach(System.out::println);
        System.out.println("Total: " + processes.size());
        System.out.println("========================");
    }

    /* ---------------------------------------------------
     * Test method - in ra blacklisted processes
     * @author: K24DTCN210-NVMANH (21/11/2025 11:21)
     * --------------------------------------------------- */
    public static void printBlacklistedProcesses() {
        System.out.println("=== Blacklisted Processes ===");
        List<String> blacklisted = getBlacklistedProcesses();
        
        if (blacklisted.isEmpty()) {
            System.out.println("No blacklisted processes running");
        } else {
            blacklisted.forEach(p -> 
                System.out.println("⚠️ BLACKLISTED: " + p));
        }
        
        System.out.println("============================");
    }

    /* ---------------------------------------------------
     * Main method để test ProcessDetector
     * @author: K24DTCN210-NVMANH (21/11/2025 11:21)
     * --------------------------------------------------- */
    public static void main(String[] args) {
        printRunningProcesses();
        System.out.println();
        printBlacklistedProcesses();
        
        // Test specific processes
        System.out.println("\n=== Specific Process Checks ===");
        String[] testProcesses = {"chrome", "teamviewer", "java", "code"};
        for (String process : testProcesses) {
            boolean running = isProcessRunning(process);
            boolean blacklisted = config.isProcessBlacklisted(process);
            System.out.printf("%s: Running=%b, Blacklisted=%b%n", 
                process, running, blacklisted);
        }
    }
}

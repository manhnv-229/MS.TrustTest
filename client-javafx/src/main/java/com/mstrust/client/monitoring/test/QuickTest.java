package com.mstrust.client.monitoring.test;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.monitoring.MonitoringCoordinator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * Quick test script để test monitors từ command line
 * Không cần JavaFX, chỉ test logic
 * @author: K24DTCN210-NVMANH (01/12/2025 11:45)
 * --------------------------------------------------- */
public class QuickTest {
    private static final Logger logger = LoggerFactory.getLogger(QuickTest.class);
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Monitor Quick Test - Phase 11");
        System.out.println("========================================\n");
        
        MonitoringApiClient apiClient = new MonitoringApiClient();
        MonitoringCoordinator coordinator = new MonitoringCoordinator(apiClient);
        
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Enter Submission ID (default: 1):");
        String submissionIdStr = scanner.nextLine();
        Long submissionId = submissionIdStr.isEmpty() ? 1L : Long.parseLong(submissionIdStr);
        
        System.out.println("Enter Auth Token (default: test-token):");
        String authToken = scanner.nextLine();
        if (authToken.isEmpty()) {
            authToken = "test-token";
        }
        
        System.out.println("\nStarting monitoring...");
        coordinator.startMonitoring(submissionId, authToken);
        
        System.out.println("✅ Monitoring started!");
        System.out.println("\nTest instructions:");
        System.out.println("1. Alt+Tab để test WindowFocusMonitor");
        System.out.println("2. Copy/Paste để test ClipboardMonitor");
        System.out.println("3. Gõ phím để test KeystrokeAnalyzer");
        System.out.println("4. Mở Chrome/TeamViewer để test ProcessMonitor");
        System.out.println("5. Chờ 30-120s để test ScreenCaptureMonitor");
        System.out.println("\nPress Enter to view stats, 'q' to quit...");
        
        while (true) {
            String input = scanner.nextLine();
            
            if ("q".equalsIgnoreCase(input)) {
                break;
            }
            
            // Print stats
            System.out.println("\n" + coordinator.getStats());
            System.out.println("\nPress Enter to refresh stats, 'q' to quit...");
        }
        
        System.out.println("\nStopping monitoring...");
        coordinator.stopMonitoring();
        System.out.println("✅ Monitoring stopped!");
        
        // Final stats
        System.out.println("\nFinal Stats:");
        System.out.println(coordinator.getStats());
        
        scanner.close();
    }
}


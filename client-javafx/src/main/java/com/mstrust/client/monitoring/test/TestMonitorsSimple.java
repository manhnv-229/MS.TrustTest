package com.mstrust.client.monitoring.test;

import com.mstrust.client.api.MonitoringApiClient;
import com.mstrust.client.monitoring.MonitoringCoordinator;

/* ---------------------------------------------------
 * Simple test không cần JavaFX - chỉ test logic
 * Chạy: java -cp target/classes com.mstrust.client.monitoring.test.TestMonitorsSimple
 * @author: K24DTCN210-NVMANH (01/12/2025 11:50)
 * --------------------------------------------------- */
public class TestMonitorsSimple {
    public static void main(String[] args) throws Exception {
        System.out.println("========================================");
        System.out.println("Simple Monitor Test - Phase 11");
        System.out.println("========================================\n");
        
        MonitoringApiClient apiClient = new MonitoringApiClient();
        MonitoringCoordinator coordinator = new MonitoringCoordinator(apiClient);
        
        Long submissionId = 1L;
        String authToken = "test-token";
        
        System.out.println("Starting monitoring...");
        coordinator.startMonitoring(submissionId, authToken);
        
        System.out.println("✅ Monitoring started!");
        System.out.println("\nTest instructions:");
        System.out.println("1. Alt+Tab để test WindowFocusMonitor");
        System.out.println("2. Copy/Paste để test ClipboardMonitor");
        System.out.println("3. Gõ phím để test KeystrokeAnalyzer");
        System.out.println("4. Mở Chrome/TeamViewer để test ProcessMonitor");
        System.out.println("5. Chờ 30-120s để test ScreenCaptureMonitor");
        System.out.println("\nMonitoring for 60 seconds...");
        System.out.println("(Press Ctrl+C to stop early)\n");
        
        // Monitor trong 60 giây
        for (int i = 60; i > 0; i--) {
            Thread.sleep(1000);
            if (i % 10 == 0) {
                System.out.println("Time remaining: " + i + "s");
                System.out.println(coordinator.getStats());
                System.out.println("---\n");
            }
        }
        
        System.out.println("\nStopping monitoring...");
        coordinator.stopMonitoring();
        System.out.println("✅ Monitoring stopped!");
        
        // Final stats
        System.out.println("\n=== Final Stats ===");
        System.out.println(coordinator.getStats());
        
        System.out.println("\nTest completed!");
    }
}


package com.mstrust.client.exam.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* ---------------------------------------------------
 * Network Monitor - Giám sát kết nối mạng
 * 
 * Features:
 * - Ping server mỗi 10 giây
 * - Detect disconnection
 * - Notify listeners khi status thay đổi
 * - Support multiple listeners
 * 
 * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
 * --------------------------------------------------- */
public class NetworkMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(NetworkMonitor.class);
    
    // Configuration
    private static final int HEALTH_CHECK_INTERVAL_SECONDS = 10;
    private static final int HEALTH_CHECK_TIMEOUT_SECONDS = 5;
    private static final String API_BASE_URL = "http://localhost:8080/api";
    
    // Scheduler
    private final ScheduledExecutorService scheduler;
    
    // State
    private boolean isRunning = false;
    private boolean isConnected = true;
    private final List<NetworkStatusListener> listeners;
    
    // HTTP Client for health checks
    private final OkHttpClient httpClient;

    /* ---------------------------------------------------
     * Constructor
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    public NetworkMonitor() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NetworkMonitor");
            t.setDaemon(true);
            return t;
        });
        this.listeners = new CopyOnWriteArrayList<>();
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
    }

    /* ---------------------------------------------------
     * Start network monitoring
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    public void start() {
        if (isRunning) {
            logger.warn("NetworkMonitor đã running");
            return;
        }
        
        isRunning = true;
        logger.info("Starting NetworkMonitor - Health check interval: {}s", HEALTH_CHECK_INTERVAL_SECONDS);
        
        // Schedule periodic health check
        scheduler.scheduleAtFixedRate(
            this::performHealthCheck,
            0, // Start immediately
            HEALTH_CHECK_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
    }

    /* ---------------------------------------------------
     * Stop network monitoring
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        logger.info("Stopping NetworkMonitor...");
        isRunning = false;
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("NetworkMonitor scheduler không shutdown trong 5s, forcing...");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("NetworkMonitor shutdown interrupted");
            scheduler.shutdownNow();
        }
        
        logger.info("NetworkMonitor stopped");
    }

    /* ---------------------------------------------------
     * Perform health check (ping server)
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    private void performHealthCheck() {
        boolean wasConnected = isConnected;
        isConnected = checkConnection();
        
        if (wasConnected != isConnected) {
            logger.info("Connection status changed: {} -> {}", 
                wasConnected ? "CONNECTED" : "DISCONNECTED",
                isConnected ? "CONNECTED" : "DISCONNECTED");
            
            notifyListeners(isConnected);
        } else {
            logger.trace("Connection status unchanged: {}", 
                isConnected ? "CONNECTED" : "DISCONNECTED");
        }
    }

    /* ---------------------------------------------------
     * Check connection to server
     * @returns true nếu server reachable
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    private boolean checkConnection() {
        try {
            // Simple HEAD request to check server availability
            Request request = new Request.Builder()
                .url(API_BASE_URL + "/exam-taking/available")
                .head()
                .build();
            
            Response response = httpClient.newCall(request).execute();
            boolean success = response.isSuccessful();
            response.close();
            
            logger.trace("Health check result: {}", success ? "SUCCESS" : "FAILED");
            return success;
            
        } catch (IOException e) {
            logger.debug("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    /* ---------------------------------------------------
     * Add listener để nhận network status changes
     * @param listener NetworkStatusListener implementation
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    public void addListener(NetworkStatusListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            logger.debug("Added network status listener: {}", listener.getClass().getSimpleName());
        }
    }

    /* ---------------------------------------------------
     * Remove listener
     * @param listener NetworkStatusListener cần remove
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    public void removeListener(NetworkStatusListener listener) {
        if (listeners.remove(listener)) {
            logger.debug("Removed network status listener: {}", listener.getClass().getSimpleName());
        }
    }

    /* ---------------------------------------------------
     * Notify tất cả listeners về status change
     * @param connected true nếu connected, false nếu disconnected
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    private void notifyListeners(boolean connected) {
        for (NetworkStatusListener listener : listeners) {
            try {
                if (connected) {
                    listener.onConnected();
                } else {
                    listener.onDisconnected();
                }
            } catch (Exception e) {
                logger.error("Error notifying listener {}: {}", 
                    listener.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /* ---------------------------------------------------
     * Get current connection status
     * @returns true nếu hiện tại đang connected
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    public boolean isConnected() {
        return isConnected;
    }

    /* ---------------------------------------------------
     * Check xem monitor đang running không
     * @returns true nếu đang running
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    public boolean isRunning() {
        return isRunning;
    }

    /* ---------------------------------------------------
     * Force một health check ngay lập tức
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    public void forceHealthCheck() {
        if (isRunning) {
            logger.debug("Force health check requested");
            performHealthCheck();
        } else {
            logger.warn("Cannot force health check, monitor not running");
        }
    }

    /* ---------------------------------------------------
     * NetworkStatusListener interface
     * Implement interface này để nhận notifications
     * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
     * --------------------------------------------------- */
    public interface NetworkStatusListener {
        
        /* ---------------------------------------------------
         * Called khi connection được restore
         * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
         * --------------------------------------------------- */
        void onConnected();
        
        /* ---------------------------------------------------
         * Called khi connection bị mất
         * @author: K24DTCN210-NVMANH (23/11/2025 17:39)
         * --------------------------------------------------- */
        void onDisconnected();
    }
}

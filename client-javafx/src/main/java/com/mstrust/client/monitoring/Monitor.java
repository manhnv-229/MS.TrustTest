package com.mstrust.client.monitoring;

/* ---------------------------------------------------
 * Interface cho tất cả monitoring components
 * Đảm bảo consistent lifecycle management
 * @author: K24DTCN210-NVMANH (01/12/2025 10:00)
 * --------------------------------------------------- */
public interface Monitor {
    
    /* ---------------------------------------------------
     * Bắt đầu monitoring
     * @param submissionId ID bài làm đang được giám sát
     * @author: K24DTCN210-NVMANH (01/12/2025 10:00)
     * --------------------------------------------------- */
    void start(Long submissionId);
    
    /* ---------------------------------------------------
     * Dừng monitoring
     * @author: K24DTCN210-NVMANH (01/12/2025 10:00)
     * --------------------------------------------------- */
    void stop();
    
    /* ---------------------------------------------------
     * Shutdown hoàn toàn (cleanup resources)
     * @author: K24DTCN210-NVMANH (01/12/2025 10:00)
     * --------------------------------------------------- */
    void shutdown();
    
    /* ---------------------------------------------------
     * Kiểm tra monitor đang chạy không
     * @returns true nếu đang chạy
     * @author: K24DTCN210-NVMANH (01/12/2025 10:00)
     * --------------------------------------------------- */
    boolean isRunning();
    
    /* ---------------------------------------------------
     * Lấy tên monitor (để logging/debugging)
     * @returns String tên monitor
     * @author: K24DTCN210-NVMANH (01/12/2025 10:00)
     * --------------------------------------------------- */
    String getName();
}


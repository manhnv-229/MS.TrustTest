package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/* ---------------------------------------------------
 * DTO cho teacher xem real-time view của exam
 * Hiển thị tất cả students đang làm bài và progress
 * @author: K24DTCN210-NVMANH (21/11/2025 02:02)
 * EditBy: K24DTCN210-NVMANH (21/11/2025 02:07) - Fixed structure to match service
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherLiveViewDTO {
    
    /* ---------------------------------------------------
     * Exam Information
     * --------------------------------------------------- */
    private Long examId;
    private String examTitle;
    
    /* ---------------------------------------------------
     * Active Sessions Count
     * --------------------------------------------------- */
    private Integer totalActiveSessions;
    
    /* ---------------------------------------------------
     * Active Sessions List
     * --------------------------------------------------- */
    private List<ActiveSessionDTO> sessions;
    
    /* ---------------------------------------------------
     * Statistics - Các chỉ số thống kê
     * --------------------------------------------------- */
    private Map<String, Object> statistics;
    
    /* ---------------------------------------------------
     * Alerts - Students cần attention
     * --------------------------------------------------- */
    private List<String> alerts; // VD: "Student X has not saved for 10 minutes"
    
    /* ---------------------------------------------------
     * Last Updated Time
     * --------------------------------------------------- */
    private LocalDateTime lastUpdated;
}

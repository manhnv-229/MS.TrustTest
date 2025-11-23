package com.mstrust.client.exam.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * DTO chứa thông tin đề thi cho màn hình danh sách
 * - Map từ backend ExamDTO
 * @author: K24DTCN210-NVMANH (23/11/2025 11:51)
 * --------------------------------------------------- */
@Data
public class ExamInfoDTO {
    private Long id;
    private String title;
    private String description;
    private Integer duration; // Phút
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalQuestions;
    private Double totalPoints;
    private String status; // UPCOMING, ONGOING, ENDED
    
    // Subject info
    private String subjectCode;
    private String subjectName;
    
    // Class info
    private List<String> classNames;
    
    // Calculated fields
    private boolean canStart;
    private String timeRemaining; // "2 giờ 30 phút"
    
    /* ---------------------------------------------------
     * Convenience method: alias cho getId()
     * @returns ID của đề thi
     * @author: K24DTCN210-NVMANH (23/11/2025 13:32)
     * --------------------------------------------------- */
    public Long getExamId() {
        return this.id;
    }
    
    /* ---------------------------------------------------
     * Convenience method: alias cho getDuration()
     * @returns Thời lượng đề thi (phút)
     * @author: K24DTCN210-NVMANH (23/11/2025 13:32)
     * --------------------------------------------------- */
    public Integer getDurationMinutes() {
        return this.duration;
    }
}

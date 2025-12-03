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
    private Double passingScore; // Điểm đạt
    private String status; // UPCOMING, ONGOING, ENDED
    
    // Subject info
    private String subjectCode;
    private String subjectName;
    
    // Class info
    private List<String> classNames;
    
    // Attempt info
    private Integer maxAttempts; // Số lần tối đa (null = unlimited)
    private Integer attemptsMade; // Số lần đã làm
    private Integer remainingAttempts; // Số lần còn lại
    private Boolean hasActiveSubmission; // Đang có bài làm dở
    private Boolean hasPassed; // Đã pass chưa
    private Double highestScore; // Điểm cao nhất
    private Boolean isEligible; // Có thể làm không
    private String ineligibleReason; // Lý do không thể làm
    
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

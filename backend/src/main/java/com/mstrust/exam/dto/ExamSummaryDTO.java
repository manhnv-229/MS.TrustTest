package com.mstrust.exam.dto;

import com.mstrust.exam.entity.ExamFormat;
import com.mstrust.exam.entity.ExamPurpose;
import com.mstrust.exam.entity.ExamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO cho danh sách Exam (lightweight)
 * Sử dụng khi: GET /api/exams (list view)
 * Chỉ chứa thông tin cơ bản để hiển thị trong table/list
 * @author: K24DTCN210-NVMANH (19/11/2025 08:38)
 * --------------------------------------------------- */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSummaryDTO {
    
    private Long id;
    private String title;
    
    // Subject class info (minimal)
    private Long subjectClassId;
    private String subjectClassName;
    private String subjectName;
    private String className;
    
    // Exam classification
    private ExamPurpose examPurpose;
    private ExamFormat examFormat;
    
    // Time info
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    
    // Status
    private Boolean isPublished;
    private ExamStatus currentStatus;
    
    // Statistics (counts only)
    private Integer questionCount;
    private Integer submissionCount;
    
    // Metadata
    private LocalDateTime createdAt;
    private String createdByName;
}

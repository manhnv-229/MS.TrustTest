package com.mstrust.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * DTO cho request submit exam for review
 * Student submit bài để teacher review trước khi final submit
 * @author: K24DTCN210-NVMANH (21/11/2025 02:02)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitForReviewRequest {
    
    /* ---------------------------------------------------
     * ID của submission
     * --------------------------------------------------- */
    private Long submissionId;
    
    /* ---------------------------------------------------
     * Notes từ student cho teacher (optional)
     * VD: "Em không chắc câu 5, mong thầy xem lại"
     * --------------------------------------------------- */
    private String studentNotes;
    
    /* ---------------------------------------------------
     * Có muốn continue sau khi review không
     * true = chỉ review, chưa final submit
     * false = submit luôn sau review
     * --------------------------------------------------- */
    private Boolean allowContinueAfterReview;
}

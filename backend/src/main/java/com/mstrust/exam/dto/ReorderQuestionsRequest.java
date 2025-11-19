package com.mstrust.exam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO để sắp xếp lại thứ tự câu hỏi trong bài thi
 * @author: K24DTCN210-NVMANH (19/11/2025 09:16)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderQuestionsRequest {
    
    @NotEmpty(message = "Danh sách câu hỏi không được rỗng")
    @Valid
    private List<QuestionOrder> questions;
    
    /**
     * Inner class để định nghĩa thứ tự mới cho mỗi câu hỏi
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOrder {
        
        @NotNull(message = "Question ID không được null")
        @Positive(message = "Question ID phải là số dương")
        private Long questionId;
        
        @NotNull(message = "New order không được null")
        @Positive(message = "New order phải là số dương")
        private Integer newOrder;
    }
}

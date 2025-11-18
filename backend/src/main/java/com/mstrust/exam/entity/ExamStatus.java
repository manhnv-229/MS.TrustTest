package com.mstrust.exam.entity;

/** ------------------------------------------
 * Mục đích: Enum định nghĩa trạng thái của bài thi
 * 
 * Các trạng thái:
 * - DRAFT: Bản nháp (đang soạn thảo)
 * - PUBLISHED: Đã công bố (sẵn sàng cho sinh viên làm)
 * - ONGOING: Đang diễn ra
 * - COMPLETED: Đã kết thúc
 * - CANCELLED: Đã hủy
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:19
 */
public enum ExamStatus {
    DRAFT("Bản nháp"),
    PUBLISHED("Đã công bố"),
    ONGOING("Đang diễn ra"),
    COMPLETED("Đã kết thúc"),
    CANCELLED("Đã hủy");
    
    private final String displayName;
    
    ExamStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

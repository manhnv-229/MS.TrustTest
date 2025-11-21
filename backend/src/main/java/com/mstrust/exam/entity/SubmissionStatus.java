package com.mstrust.exam.entity;

/* ---------------------------------------------------
 * Enum định nghĩa các trạng thái của submission
 * @author: K24DTCN210-NVMANH (19/11/2025 15:08)
 * --------------------------------------------------- */
public enum SubmissionStatus {
    NOT_STARTED,    // Chưa bắt đầu làm bài
    IN_PROGRESS,    // Đang làm bài
    PAUSED,         // Tạm dừng (giáo viên tạm dừng bài thi)
    SUBMITTED,      // Đã nộp bài
    GRADED,         // Đã chấm xong
    EXPIRED         // Hết hạn (quá thời gian làm bài)
}

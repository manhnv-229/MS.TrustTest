-- V18: Tạo bảng activity_logs để ghi log các hoạt động của student
-- CreatedBy: K24DTCN210-NVMANH (21/11/2025 10:06)

CREATE TABLE activity_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL COMMENT 'ID của bài thi',
    activity_type VARCHAR(50) NOT NULL COMMENT 'Loại hoạt động: WINDOW_FOCUS, PROCESS_DETECTED, CLIPBOARD, KEYSTROKE',
    details TEXT COMMENT 'Chi tiết hoạt động (JSON format)',
    timestamp DATETIME NOT NULL COMMENT 'Thời điểm xảy ra hoạt động',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL COMMENT 'Soft delete timestamp',
    
    CONSTRAINT fk_activity_submission 
        FOREIGN KEY (submission_id) 
        REFERENCES exam_submissions(id)
        ON DELETE CASCADE,
        
    INDEX idx_submission_timestamp (submission_id, timestamp),
    INDEX idx_activity_type (activity_type),
    INDEX idx_timestamp (timestamp),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng ghi log các hoạt động của student trong khi làm bài';

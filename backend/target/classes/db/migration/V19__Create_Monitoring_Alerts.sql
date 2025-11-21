-- V19: Tạo bảng monitoring_alerts để lưu các cảnh báo từ monitoring system
-- CreatedBy: K24DTCN210-NVMANH (21/11/2025 10:07)

CREATE TABLE monitoring_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL COMMENT 'ID của bài thi',
    alert_type VARCHAR(50) NOT NULL COMMENT 'Loại cảnh báo: BLACKLIST_PROCESS, WINDOW_SWITCH, CLIPBOARD_PASTE, KEYSTROKE_ANOMALY',
    severity VARCHAR(20) NOT NULL COMMENT 'Mức độ nghiêm trọng: LOW, MEDIUM, HIGH, CRITICAL',
    description TEXT COMMENT 'Mô tả chi tiết cảnh báo',
    reviewed BOOLEAN DEFAULT FALSE COMMENT 'Đã được review chưa',
    reviewed_by BIGINT COMMENT 'ID giáo viên review',
    reviewed_at DATETIME COMMENT 'Thời điểm review',
    review_note TEXT COMMENT 'Ghi chú của giáo viên khi review',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL COMMENT 'Soft delete timestamp',
    
    CONSTRAINT fk_alert_submission 
        FOREIGN KEY (submission_id) 
        REFERENCES exam_submissions(id)
        ON DELETE CASCADE,
        
    CONSTRAINT fk_alert_reviewer 
        FOREIGN KEY (reviewed_by) 
        REFERENCES users(id)
        ON DELETE SET NULL,
        
    INDEX idx_submission_severity (submission_id, severity),
    INDEX idx_reviewed (reviewed),
    INDEX idx_alert_type (alert_type),
    INDEX idx_created_at (created_at),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu các cảnh báo từ monitoring system';

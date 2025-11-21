-- V17: Tạo bảng monitoring_screenshots để lưu thông tin screenshots từ client
-- CreatedBy: K24DTCN210-NVMANH (21/11/2025 10:06)

CREATE TABLE monitoring_screenshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL COMMENT 'ID của bài thi',
    file_path VARCHAR(500) NOT NULL COMMENT 'Đường dẫn file trên FTP server',
    file_size BIGINT COMMENT 'Kích thước file (bytes)',
    timestamp DATETIME NOT NULL COMMENT 'Thời điểm chụp màn hình',
    screen_resolution VARCHAR(20) COMMENT 'Độ phân giải màn hình (VD: 1920x1080)',
    window_title VARCHAR(255) COMMENT 'Tiêu đề cửa sổ đang active',
    metadata TEXT COMMENT 'Metadata bổ sung (JSON format)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL COMMENT 'Soft delete timestamp',
    
    CONSTRAINT fk_screenshot_submission 
        FOREIGN KEY (submission_id) 
        REFERENCES exam_submissions(id)
        ON DELETE CASCADE,
        
    INDEX idx_submission_timestamp (submission_id, timestamp),
    INDEX idx_timestamp (timestamp),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu screenshots từ client monitoring system';

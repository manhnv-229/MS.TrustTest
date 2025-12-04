CREATE TABLE system_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level VARCHAR(20) NOT NULL,
    source VARCHAR(100) NOT NULL,
    message TEXT,
    stack_trace TEXT,
    additional_data TEXT,
    submission_id BIGINT,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_system_logs_submission FOREIGN KEY (submission_id) REFERENCES exam_submissions(id),
    CONSTRAINT fk_system_logs_user FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);
CREATE INDEX idx_system_logs_level ON system_logs(level);
CREATE INDEX idx_system_logs_submission ON system_logs(submission_id);

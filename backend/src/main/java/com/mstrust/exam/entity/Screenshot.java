package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/* ---------------------------------------------------
 * Entity đại diện cho screenshot được chụp từ client monitoring
 * @author: K24DTCN210-NVMANH (21/11/2025 10:08)
 * --------------------------------------------------- */
@Entity
@Table(name = "monitoring_screenshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screenshot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private ExamSubmission submission;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "screen_resolution", length = 20)
    private String screenResolution;
    
    @Column(name = "window_title")
    private String windowTitle;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.mstrust.exam.service;

import com.mstrust.exam.dto.monitoring.ScreenshotDTO;
import com.mstrust.exam.entity.ExamSubmission;
import com.mstrust.exam.entity.Screenshot;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.ExamSubmissionRepository;
import com.mstrust.exam.repository.ScreenshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Service xử lý business logic cho screenshots
 * @author: K24DTCN210-NVMANH (21/11/2025 10:13)
 * --------------------------------------------------- */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ScreenshotService {
    
    private final ScreenshotRepository screenshotRepository;
    private final ExamSubmissionRepository submissionRepository;
    private final FtpStorageService ftpStorageService;
    
    /* ---------------------------------------------------
     * Upload screenshot từ client lên FTP và lưu metadata vào DB
     * @param file File ảnh screenshot
     * @param submissionId ID của submission
     * @param screenResolution Độ phân giải màn hình
     * @param windowTitle Tiêu đề cửa sổ
     * @param metadata Metadata khác (JSON)
     * @returns ScreenshotDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:13)
     * --------------------------------------------------- */
    public ScreenshotDTO uploadScreenshot(
        MultipartFile file,
        Long submissionId,
        String screenResolution,
        String windowTitle,
        String metadata
    ) {
        // Validate submission exists
        ExamSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));
        
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Screenshot file is required");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }
        
        try {
            // Upload to FTP
            String ftpPath = ftpStorageService.uploadScreenshot(file, submissionId);
            
            // Convert FTP path to DB path (remove /trusttest prefix)
            String dbPath = ftpPath.startsWith("/trusttest") ? 
                ftpPath.substring("/trusttest".length()) : ftpPath;
            
            // Save metadata to database
            Screenshot screenshot = Screenshot.builder()
                .submission(submission)
                .filePath(dbPath)
                .fileSize(file.getSize())
                .timestamp(LocalDateTime.now())
                .screenResolution(screenResolution)
                .windowTitle(windowTitle)
                .metadata(metadata)
                .build();
            
            screenshot = screenshotRepository.save(screenshot);
            
            log.info("Screenshot uploaded successfully for submission {}: FTP={}, DB={}", submissionId, ftpPath, dbPath);
            
            return convertToDTO(screenshot);
            
        } catch (IOException ex) {
            log.error("Failed to upload screenshot for submission {}", submissionId, ex);
            throw new RuntimeException("Failed to upload screenshot: " + ex.getMessage());
        }
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách screenshots của một submission
     * @param submissionId ID của submission
     * @returns Danh sách ScreenshotDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:13)
     * --------------------------------------------------- */
    public List<ScreenshotDTO> getScreenshotsBySubmission(Long submissionId) {
        List<Screenshot> screenshots = screenshotRepository.findBySubmissionId(submissionId);
        return screenshots.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Lấy screenshots trong khoảng thời gian
     * @param submissionId ID của submission
     * @param startTime Thời gian bắt đầu
     * @param endTime Thời gian kết thúc
     * @returns Danh sách ScreenshotDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:13)
     * --------------------------------------------------- */
    public List<ScreenshotDTO> getScreenshotsByTimeRange(
        Long submissionId,
        LocalDateTime startTime,
        LocalDateTime endTime
    ) {
        List<Screenshot> screenshots = screenshotRepository
            .findBySubmissionIdAndTimestampBetween(submissionId, startTime, endTime);
        
        return screenshots.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Đếm số screenshots của một submission
     * @param submissionId ID của submission
     * @returns Số lượng screenshots
     * @author: K24DTCN210-NVMANH (21/11/2025 10:13)
     * --------------------------------------------------- */
    public long countScreenshots(Long submissionId) {
        return screenshotRepository.countBySubmissionId(submissionId);
    }
    
    /* ---------------------------------------------------
     * Lấy screenshot mới nhất của submission
     * @param submissionId ID của submission
     * @returns ScreenshotDTO hoặc null
     * @author: K24DTCN210-NVMANH (21/11/2025 10:13)
     * --------------------------------------------------- */
    public ScreenshotDTO getLatestScreenshot(Long submissionId) {
        List<Screenshot> screenshots = screenshotRepository.findBySubmissionId(submissionId);
        
        if (screenshots.isEmpty()) {
            return null;
        }
        
        return convertToDTO(screenshots.get(0)); // Already sorted DESC by timestamp
    }
    
    /* ---------------------------------------------------
     * Xóa screenshot (soft delete)
     * @param screenshotId ID của screenshot
     * @author: K24DTCN210-NVMANH (21/11/2025 10:13)
     * --------------------------------------------------- */
    public void deleteScreenshot(Long screenshotId) {
        Screenshot screenshot = screenshotRepository.findById(screenshotId)
            .orElseThrow(() -> new ResourceNotFoundException("Screenshot not found with id: " + screenshotId));
        
        // Soft delete
        screenshot.setDeletedAt(LocalDateTime.now());
        screenshotRepository.save(screenshot);
        
        // Optional: Delete from FTP (comment out if you want to keep files)
        // ftpStorageService.deleteFile(screenshot.getFilePath());
        
        log.info("Screenshot deleted (soft): {}", screenshotId);
    }
    
    /* ---------------------------------------------------
     * Dọn dẹp screenshots cũ (older than 90 days)
     * Chạy định kỳ bằng scheduled task
     * @author: K24DTCN210-NVMANH (21/11/2025 10:13)
     * --------------------------------------------------- */
    public void cleanupOldScreenshots() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        screenshotRepository.softDeleteOlderThan(cutoffDate);
        log.info("Cleaned up screenshots older than {}", cutoffDate);
    }
    
    /* ---------------------------------------------------
     * Convert Screenshot entity to DTO
     * @param screenshot Screenshot entity
     * @returns ScreenshotDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 10:13)
     * --------------------------------------------------- */
    private ScreenshotDTO convertToDTO(Screenshot screenshot) {
        return ScreenshotDTO.builder()
            .id(screenshot.getId())
            .submissionId(screenshot.getSubmission().getId())
            .filePath(screenshot.getFilePath())
            .fileSize(screenshot.getFileSize())
            .timestamp(screenshot.getTimestamp())
            .screenResolution(screenshot.getScreenResolution())
            .windowTitle(screenshot.getWindowTitle())
            .metadata(screenshot.getMetadata())
            .createdAt(screenshot.getCreatedAt())
            .build();
    }
}

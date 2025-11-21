package com.mstrust.exam.repository;

import com.mstrust.exam.entity.Screenshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * Repository cho Screenshot entity
 * @author: K24DTCN210-NVMANH (21/11/2025 10:08)
 * --------------------------------------------------- */
@Repository
public interface ScreenshotRepository extends JpaRepository<Screenshot, Long> {
    
    /* ---------------------------------------------------
     * Lấy tất cả screenshots của một submission (chưa bị xóa)
     * @param submissionId ID của submission
     * @returns Danh sách screenshots
     * @author: K24DTCN210-NVMANH (21/11/2025 10:08)
     * --------------------------------------------------- */
    @Query("SELECT s FROM Screenshot s WHERE s.submission.id = :submissionId AND s.deletedAt IS NULL ORDER BY s.timestamp DESC")
    List<Screenshot> findBySubmissionId(@Param("submissionId") Long submissionId);
    
    /* ---------------------------------------------------
     * Lấy screenshots trong khoảng thời gian
     * @param submissionId ID của submission
     * @param startTime Thời gian bắt đầu
     * @param endTime Thời gian kết thúc
     * @returns Danh sách screenshots
     * @author: K24DTCN210-NVMANH (21/11/2025 10:08)
     * --------------------------------------------------- */
    @Query("SELECT s FROM Screenshot s WHERE s.submission.id = :submissionId " +
           "AND s.timestamp BETWEEN :startTime AND :endTime " +
           "AND s.deletedAt IS NULL ORDER BY s.timestamp DESC")
    List<Screenshot> findBySubmissionIdAndTimestampBetween(
        @Param("submissionId") Long submissionId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /* ---------------------------------------------------
     * Đếm số screenshots của một submission
     * @param submissionId ID của submission
     * @returns Số lượng screenshots
     * @author: K24DTCN210-NVMANH (21/11/2025 10:08)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(s) FROM Screenshot s WHERE s.submission.id = :submissionId AND s.deletedAt IS NULL")
    long countBySubmissionId(@Param("submissionId") Long submissionId);
    
    /* ---------------------------------------------------
     * Xóa screenshots cũ hơn số ngày chỉ định (soft delete)
     * @param date Ngày giới hạn
     * @author: K24DTCN210-NVMANH (21/11/2025 10:08)
     * --------------------------------------------------- */
    @Query("UPDATE Screenshot s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.timestamp < :date AND s.deletedAt IS NULL")
    void softDeleteOlderThan(@Param("date") LocalDateTime date);
}

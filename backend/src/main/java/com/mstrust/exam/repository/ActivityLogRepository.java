package com.mstrust.exam.repository;

import com.mstrust.exam.entity.ActivityLog;
import com.mstrust.exam.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * Repository cho ActivityLog entity
 * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
 * --------------------------------------------------- */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    /* ---------------------------------------------------
     * Lấy tất cả activity logs của một submission
     * @param submissionId ID của submission
     * @returns Danh sách activity logs
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT a FROM ActivityLog a WHERE a.submission.id = :submissionId AND a.deletedAt IS NULL ORDER BY a.timestamp DESC")
    List<ActivityLog> findBySubmissionId(@Param("submissionId") Long submissionId);
    
    /* ---------------------------------------------------
     * Lấy activity logs theo loại activity
     * @param submissionId ID của submission
     * @param activityType Loại activity
     * @returns Danh sách activity logs
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT a FROM ActivityLog a WHERE a.submission.id = :submissionId " +
           "AND a.activityType = :activityType AND a.deletedAt IS NULL ORDER BY a.timestamp DESC")
    List<ActivityLog> findBySubmissionIdAndActivityType(
        @Param("submissionId") Long submissionId,
        @Param("activityType") ActivityType activityType
    );
    
    /* ---------------------------------------------------
     * Lấy activity logs trong khoảng thời gian
     * @param submissionId ID của submission
     * @param startTime Thời gian bắt đầu
     * @param endTime Thời gian kết thúc
     * @returns Danh sách activity logs
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT a FROM ActivityLog a WHERE a.submission.id = :submissionId " +
           "AND a.timestamp BETWEEN :startTime AND :endTime " +
           "AND a.deletedAt IS NULL ORDER BY a.timestamp DESC")
    List<ActivityLog> findBySubmissionIdAndTimestampBetween(
        @Param("submissionId") Long submissionId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /* ---------------------------------------------------
     * Đếm số lượng window switch trong khoảng thời gian (để phát hiện bất thường)
     * @param submissionId ID của submission
     * @param startTime Thời gian bắt đầu
     * @param endTime Thời gian kết thúc
     * @returns Số lần switch window
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE a.submission.id = :submissionId " +
           "AND a.activityType = 'WINDOW_FOCUS' " +
           "AND a.timestamp BETWEEN :startTime AND :endTime " +
           "AND a.deletedAt IS NULL")
    long countWindowSwitchesInTimeRange(
        @Param("submissionId") Long submissionId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    /* ---------------------------------------------------
     * Xóa activity logs cũ hơn số ngày chỉ định (soft delete)
     * @param date Ngày giới hạn
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("UPDATE ActivityLog a SET a.deletedAt = CURRENT_TIMESTAMP WHERE a.timestamp < :date AND a.deletedAt IS NULL")
    void softDeleteOlderThan(@Param("date") LocalDateTime date);
}

package com.mstrust.exam.repository;

import com.mstrust.exam.entity.AlertSeverity;
import com.mstrust.exam.entity.MonitoringAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/* ---------------------------------------------------
 * Repository cho MonitoringAlert entity
 * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
 * --------------------------------------------------- */
@Repository
public interface MonitoringAlertRepository extends JpaRepository<MonitoringAlert, Long> {
    
    /* ---------------------------------------------------
     * Lấy tất cả alerts của một submission
     * @param submissionId ID của submission
     * @returns Danh sách alerts
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT a FROM MonitoringAlert a WHERE a.submission.id = :submissionId AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<MonitoringAlert> findBySubmissionId(@Param("submissionId") Long submissionId);
    
    /* ---------------------------------------------------
     * Lấy alerts theo mức độ nghiêm trọng
     * @param submissionId ID của submission
     * @param severity Mức độ nghiêm trọng
     * @returns Danh sách alerts
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT a FROM MonitoringAlert a WHERE a.submission.id = :submissionId " +
           "AND a.severity = :severity AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<MonitoringAlert> findBySubmissionIdAndSeverity(
        @Param("submissionId") Long submissionId,
        @Param("severity") AlertSeverity severity
    );
    
    /* ---------------------------------------------------
     * Lấy alerts chưa được review
     * @param submissionId ID của submission
     * @returns Danh sách alerts chưa review
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT a FROM MonitoringAlert a WHERE a.submission.id = :submissionId " +
           "AND a.reviewed = false AND a.deletedAt IS NULL ORDER BY a.severity DESC, a.createdAt DESC")
    List<MonitoringAlert> findUnreviewedBySubmissionId(@Param("submissionId") Long submissionId);
    
    /* ---------------------------------------------------
     * Lấy tất cả alerts chưa review của một exam (tất cả submissions)
     * @param examId ID của exam
     * @returns Danh sách alerts chưa review
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT a FROM MonitoringAlert a WHERE a.submission.exam.id = :examId " +
           "AND a.reviewed = false AND a.deletedAt IS NULL ORDER BY a.severity DESC, a.createdAt DESC")
    List<MonitoringAlert> findUnreviewedByExamId(@Param("examId") Long examId);
    
    /* ---------------------------------------------------
     * Đếm số alerts chưa review của một submission
     * @param submissionId ID của submission
     * @returns Số lượng alerts chưa review
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(a) FROM MonitoringAlert a WHERE a.submission.id = :submissionId " +
           "AND a.reviewed = false AND a.deletedAt IS NULL")
    long countUnreviewedBySubmissionId(@Param("submissionId") Long submissionId);
    
    /* ---------------------------------------------------
     * Đếm số alerts theo mức độ nghiêm trọng
     * @param submissionId ID của submission
     * @param severity Mức độ nghiêm trọng
     * @returns Số lượng alerts
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(a) FROM MonitoringAlert a WHERE a.submission.id = :submissionId " +
           "AND a.severity = :severity AND a.deletedAt IS NULL")
    long countBySubmissionIdAndSeverity(
        @Param("submissionId") Long submissionId,
        @Param("severity") AlertSeverity severity
    );
    
    /* ---------------------------------------------------
     * Lấy alerts trong khoảng thời gian
     * @param examId ID của exam
     * @param startTime Thời gian bắt đầu
     * @param endTime Thời gian kết thúc
     * @returns Danh sách alerts
     * @author: K24DTCN210-NVMANH (21/11/2025 10:09)
     * --------------------------------------------------- */
    @Query("SELECT a FROM MonitoringAlert a WHERE a.submission.exam.id = :examId " +
           "AND a.createdAt BETWEEN :startTime AND :endTime " +
           "AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
    List<MonitoringAlert> findByExamIdAndCreatedAtBetween(
        @Param("examId") Long examId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}

package com.mstrust.exam.repository;

import com.mstrust.exam.entity.Exam;
import com.mstrust.exam.entity.ExamFormat;
import com.mstrust.exam.entity.ExamPurpose;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** ------------------------------------------
 * Mục đích: Repository interface cho Exam entity
 * 
 * Chức năng:
 * - CRUD operations cho Exam
 * - Tìm kiếm exams theo subject class, purpose, format, time
 * - Filter published/unpublished exams
 * - Soft delete aware queries
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:23
 */
@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    // Basic queries với soft delete
    Optional<Exam> findByIdAndDeletedAtIsNull(Long id);
    
    List<Exam> findByDeletedAtIsNull();
    
    Page<Exam> findByDeletedAtIsNull(Pageable pageable);
    
    // Filter by subject class
    Page<Exam> findBySubjectClassIdAndDeletedAtIsNull(Long subjectClassId, Pageable pageable);
    
    List<Exam> findBySubjectClassIdAndDeletedAtIsNull(Long subjectClassId);
    
    // Filter by exam purpose
    Page<Exam> findByExamPurposeAndDeletedAtIsNull(ExamPurpose examPurpose, Pageable pageable);
    
    // Filter by exam format
    Page<Exam> findByExamFormatAndDeletedAtIsNull(ExamFormat examFormat, Pageable pageable);
    
    // Filter by publication status
    Page<Exam> findByIsPublishedAndDeletedAtIsNull(Boolean isPublished, Pageable pageable);
    
    // Filter by created by (teacher)
    Page<Exam> findByCreatedByAndDeletedAtIsNull(Long createdBy, Pageable pageable);
    
    // Find exams in time range
    @Query("SELECT e FROM Exam e WHERE e.deletedAt IS NULL " +
           "AND e.startTime >= :startTime AND e.endTime <= :endTime")
    Page<Exam> findExamsInTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );
    
    // Find ongoing exams (now between start and end time)
    @Query("SELECT e FROM Exam e WHERE e.deletedAt IS NULL " +
           "AND e.isPublished = true " +
           "AND :now >= e.startTime AND :now <= e.endTime")
    List<Exam> findOngoingExams(@Param("now") LocalDateTime now);
    
    // Find upcoming exams (start time in future)
    @Query("SELECT e FROM Exam e WHERE e.deletedAt IS NULL " +
           "AND e.isPublished = true " +
           "AND e.startTime > :now")
    Page<Exam> findUpcomingExams(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Find completed exams (end time in past)
    @Query("SELECT e FROM Exam e WHERE e.deletedAt IS NULL " +
           "AND e.endTime < :now")
    Page<Exam> findCompletedExams(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Combined filters
    @Query("SELECT e FROM Exam e WHERE e.deletedAt IS NULL " +
           "AND (:subjectClassId IS NULL OR e.subjectClass.id = :subjectClassId) " +
           "AND (:examPurpose IS NULL OR e.examPurpose = :examPurpose) " +
           "AND (:examFormat IS NULL OR e.examFormat = :examFormat) " +
           "AND (:isPublished IS NULL OR e.isPublished = :isPublished)")
    Page<Exam> searchExams(
        @Param("subjectClassId") Long subjectClassId,
        @Param("examPurpose") ExamPurpose examPurpose,
        @Param("examFormat") ExamFormat examFormat,
        @Param("isPublished") Boolean isPublished,
        Pageable pageable
    );
    
    // Search by keyword (in title or description)
    @Query("SELECT e FROM Exam e WHERE e.deletedAt IS NULL " +
           "AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Exam> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Count by subject class
    long countBySubjectClassIdAndDeletedAtIsNull(Long subjectClassId);
    
    // Count by creator
    long countByCreatedByAndDeletedAtIsNull(Long createdBy);
    
    // Count published exams
    long countByIsPublishedAndDeletedAtIsNull(Boolean isPublished);
}

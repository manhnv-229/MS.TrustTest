package com.mstrust.exam.repository;

import com.mstrust.exam.entity.ExamSubmission;
import com.mstrust.exam.entity.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* ---------------------------------------------------
 * Repository cho ExamSubmission entity
 * Cung cấp các query methods cho exam taking flow
 * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
 * --------------------------------------------------- */
@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, Long> {

    /* ---------------------------------------------------
     * Tìm submission theo student và exam
     * @param studentId ID của student
     * @param examId ID của exam
     * @returns List các submission (sorted by attempt_number)
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s " +
           "WHERE s.student.id = :studentId AND s.exam.id = :examId " +
           "ORDER BY s.attemptNumber DESC")
    List<ExamSubmission> findByStudentIdAndExamId(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId
    );

    /* ---------------------------------------------------
     * Tìm submission đang active (IN_PROGRESS) của student cho exam
     * @param studentId ID của student
     * @param examId ID của exam
     * @returns Optional submission đang làm dở
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s " +
           "WHERE s.student.id = :studentId AND s.exam.id = :examId " +
           "AND s.status = 'IN_PROGRESS'")
    Optional<ExamSubmission> findActiveSubmission(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId
    );

    /* ---------------------------------------------------
     * Đếm số lần đã làm bài (tất cả trạng thái)
     * @param studentId ID của student
     * @param examId ID của exam
     * @returns Số lần đã attempt
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(s) FROM ExamSubmission s " +
           "WHERE s.student.id = :studentId AND s.exam.id = :examId")
    int countByStudentIdAndExamId(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId
    );

    /* ---------------------------------------------------
     * Đếm số lần đã submit (SUBMITTED hoặc GRADED)
     * @param studentId ID của student
     * @param examId ID của exam
     * @returns Số lần đã submit
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(s) FROM ExamSubmission s " +
           "WHERE s.student.id = :studentId AND s.exam.id = :examId " +
           "AND s.status IN ('SUBMITTED', 'GRADED')")
    int countSubmittedAttempts(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId
    );

    /* ---------------------------------------------------
     * Lấy tất cả submissions của student cho exam (sorted by attempt)
     * @param studentId ID của student
     * @param examId ID của exam
     * @returns List submissions
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s " +
           "WHERE s.student.id = :studentId AND s.exam.id = :examId " +
           "ORDER BY s.attemptNumber ASC")
    List<ExamSubmission> findAllByStudentAndExam(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId
    );

    /* ---------------------------------------------------
     * Lấy submission mới nhất của student cho exam
     * @param studentId ID của student
     * @param examId ID của exam
     * @returns Optional submission mới nhất
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s " +
           "WHERE s.student.id = :studentId AND s.exam.id = :examId " +
           "ORDER BY s.attemptNumber DESC")
    Optional<ExamSubmission> findLatestSubmission(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId
    );

    /* ---------------------------------------------------
     * Lấy tất cả submissions của một exam (for statistics)
     * @param examId ID của exam
     * @returns List tất cả submissions
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s WHERE s.exam.id = :examId")
    List<ExamSubmission> findByExamId(@Param("examId") Long examId);

    /* ---------------------------------------------------
     * Lấy submissions theo status
     * @param examId ID của exam
     * @param status Trạng thái cần lọc
     * @returns List submissions theo status
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s " +
           "WHERE s.exam.id = :examId AND s.status = :status")
    List<ExamSubmission> findByExamIdAndStatus(
        @Param("examId") Long examId,
        @Param("status") SubmissionStatus status
    );

    /* ---------------------------------------------------
     * Tìm tất cả submissions đã pass
     * @param studentId ID của student
     * @param examId ID của exam
     * @returns List submissions đã pass
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s " +
           "WHERE s.student.id = :studentId AND s.exam.id = :examId " +
           "AND s.passed = true " +
           "ORDER BY s.attemptNumber DESC")
    List<ExamSubmission> findPassedSubmissions(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId
    );

    /* ---------------------------------------------------
     * Kiểm tra student đã pass exam chưa
     * @param studentId ID của student
     * @param examId ID của exam
     * @returns true nếu đã pass ít nhất 1 lần
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM ExamSubmission s " +
           "WHERE s.student.id = :studentId AND s.exam.id = :examId " +
           "AND s.passed = true")
    boolean hasPassedExam(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId
    );

    /* ---------------------------------------------------
     * Lấy điểm cao nhất của student cho exam
     * @param studentId ID của student
     * @param examId ID của exam
     * @returns Optional điểm cao nhất
     * @author: K24DTCN210-NVMANH (19/11/2025 15:16)
     * --------------------------------------------------- */
    @Query("SELECT MAX(s.totalScore) FROM ExamSubmission s " +
           "WHERE s.student.id = :studentId AND s.exam.id = :examId " +
           "AND s.status IN ('SUBMITTED', 'GRADED')")
    Optional<Double> findHighestScore(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId
    );

    /* ---------------------------------------------------
     * Lấy danh sách submissions với pagination (for grading)
     * @param examId ID của exam
     * @param pageable Thông tin phân trang
     * @returns Page submissions
     * @author: K24DTCN210-NVMANH (20/11/2025 11:18)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s WHERE s.exam.id = :examId")
    Page<ExamSubmission> findByExamId(@Param("examId") Long examId, Pageable pageable);

    /* ---------------------------------------------------
     * Lấy danh sách submissions với pagination và filter theo status (for grading)
     * @param examId ID của exam
     * @param status Trạng thái cần lọc
     * @param pageable Thông tin phân trang
     * @returns Page submissions
     * @author: K24DTCN210-NVMANH (20/11/2025 11:18)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s " +
           "WHERE s.exam.id = :examId AND s.status = :status")
    Page<ExamSubmission> findByExamIdAndStatus(
        @Param("examId") Long examId,
        @Param("status") SubmissionStatus status,
        Pageable pageable
    );

    /* ---------------------------------------------------
     * Lấy tất cả submissions với pagination (for teacher to grade all exams)
     * @param pageable Thông tin phân trang
     * @returns Page submissions
     * @author: K24DTCN210-NVMANH (20/11/2025 11:18)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s ORDER BY s.submittedAt DESC")
    Page<ExamSubmission> findAllWithPagination(Pageable pageable);

    /* ---------------------------------------------------
     * Lấy submissions theo status với pagination
     * @param status Trạng thái cần lọc
     * @param pageable Thông tin phân trang
     * @returns Page submissions
     * @author: K24DTCN210-NVMANH (20/11/2025 11:18)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s WHERE s.status = :status ORDER BY s.submittedAt DESC")
    Page<ExamSubmission> findByStatus(@Param("status") SubmissionStatus status, Pageable pageable);

    /* ---------------------------------------------------
     * Lấy tất cả submissions theo status (không pagination - cho WebSocket)
     * @param status Trạng thái cần lọc
     * @returns List submissions
     * @author: K24DTCN210-NVMANH (21/11/2025 01:53)
     * --------------------------------------------------- */
    @Query("SELECT s FROM ExamSubmission s WHERE s.status = :status ORDER BY s.submittedAt DESC")
    List<ExamSubmission> findByStatus(@Param("status") SubmissionStatus status);
}

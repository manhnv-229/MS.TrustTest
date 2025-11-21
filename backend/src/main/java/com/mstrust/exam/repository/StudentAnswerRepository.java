package com.mstrust.exam.repository;

import com.mstrust.exam.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* ---------------------------------------------------
 * Repository cho StudentAnswer entity
 * Cung cấp các query methods cho answer management
 * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
 * --------------------------------------------------- */
@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    /* ---------------------------------------------------
     * Tìm tất cả answers của một submission
     * @param submissionId ID của submission
     * @returns List câu trả lời
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT a FROM StudentAnswer a WHERE a.submission.id = :submissionId")
    List<StudentAnswer> findBySubmissionId(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Tìm answer cho một câu hỏi cụ thể trong submission
     * @param submissionId ID của submission
     * @param questionId ID của question
     * @returns Optional answer
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT a FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId AND a.questionId = :questionId")
    Optional<StudentAnswer> findBySubmissionIdAndQuestionId(
        @Param("submissionId") Long submissionId,
        @Param("questionId") Long questionId
    );

    /* ---------------------------------------------------
     * Đếm số câu đã trả lời trong submission
     * @param submissionId ID của submission
     * @returns Số câu đã trả lời (không rỗng)
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(a) FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId " +
           "AND (a.answerText IS NOT NULL OR a.answerJson IS NOT NULL OR a.uploadedFileUrl IS NOT NULL)")
    int countAnsweredQuestions(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Đếm số câu đã được chấm trong submission
     * @param submissionId ID của submission
     * @returns Số câu đã chấm
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(a) FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId AND a.isCorrect IS NOT NULL")
    int countGradedQuestions(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Đếm số câu trả lời đúng trong submission
     * @param submissionId ID của submission
     * @returns Số câu đúng
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * EditBy: K24DTCN210-NVMANH (20/11/2025 21:58) - Fix null return với COALESCE
     * --------------------------------------------------- */
    @Query("SELECT COALESCE(COUNT(a), 0) FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId AND a.isCorrect = true")
    int countCorrectAnswers(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Lấy tất cả answers chưa được chấm của submission
     * @param submissionId ID của submission
     * @returns List answers chưa chấm
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT a FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId AND a.isCorrect IS NULL")
    List<StudentAnswer> findUngradedAnswers(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Lấy tất cả answers đã được chấm của submission
     * @param submissionId ID của submission
     * @returns List answers đã chấm
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT a FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId AND a.isCorrect IS NOT NULL " +
           "ORDER BY a.questionId ASC")
    List<StudentAnswer> findGradedAnswers(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Xóa tất cả answers của submission (cascade delete alternative)
     * @param submissionId ID của submission
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("DELETE FROM StudentAnswer a WHERE a.submission.id = :submissionId")
    void deleteBySubmissionId(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Kiểm tra student đã trả lời câu hỏi này chưa
     * @param submissionId ID của submission
     * @param questionId ID của question
     * @returns true nếu đã trả lời
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId AND a.questionId = :questionId " +
           "AND (a.answerText IS NOT NULL OR a.answerJson IS NOT NULL OR a.uploadedFileUrl IS NOT NULL)")
    boolean hasAnswered(
        @Param("submissionId") Long submissionId,
        @Param("questionId") Long questionId
    );

    /* ---------------------------------------------------
     * Lấy tất cả answers có file upload
     * @param submissionId ID của submission
     * @returns List answers có file
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT a FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId AND a.uploadedFileUrl IS NOT NULL")
    List<StudentAnswer> findAnswersWithFiles(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Tính tổng điểm của submission (SUM of pointsEarned)
     * @param submissionId ID của submission
     * @returns Tổng điểm
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT COALESCE(SUM(a.pointsEarned), 0) FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId")
    Double calculateTotalScore(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Lấy answers đã được teacher chấm thủ công
     * @param submissionId ID của submission
     * @returns List answers có gradedBy không null
     * @author: K24DTCN210-NVMANH (19/11/2025 15:17)
     * --------------------------------------------------- */
    @Query("SELECT a FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId AND a.gradedBy IS NOT NULL")
    List<StudentAnswer> findManuallyGradedAnswers(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Đếm số câu trả lời chưa được chấm điểm (for grading dashboard)
     * Chỉ đếm những câu đã có câu trả lời (answerJson không null) nhưng chưa chấm (isCorrect null)
     * @param submissionId ID của submission
     * @returns Số câu chưa chấm
     * @author: K24DTCN210-NVMANH (20/11/2025 11:19)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(a) FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId " +
           "AND a.isCorrect IS NULL " +
           "AND a.answerJson IS NOT NULL")
    int countUngradedBySubmissionId(@Param("submissionId") Long submissionId);

    /* ---------------------------------------------------
     * Đếm số câu đã có answerText (cho WebSocket progress tracking)
     * @param submissionId ID của submission
     * @returns Số câu đã có answer text
     * @author: K24DTCN210-NVMANH (21/11/2025 01:50)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(a) FROM StudentAnswer a " +
           "WHERE a.submission.id = :submissionId AND a.answerText IS NOT NULL")
    long countBySubmissionIdAndAnswerTextIsNotNull(@Param("submissionId") Long submissionId);
}

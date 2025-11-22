package com.mstrust.exam.repository;

import com.mstrust.exam.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** ------------------------------------------
 * Mục đích: Repository interface cho ExamQuestion entity (join table)
 * 
 * Chức năng:
 * - Quản lý questions trong exam
 * - Add/remove questions from exam
 * - Reorder questions
 * - Update points for questions
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:24
 */
@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    
    // Find all questions in an exam (ordered by question_order)
    @Query("SELECT eq FROM ExamQuestion eq WHERE eq.exam.id = :examId ORDER BY eq.questionOrder")
    List<ExamQuestion> findByExamIdOrderByQuestionOrder(@Param("examId") Long examId);
    
    // Find specific exam-question relationship (with question loaded)
    @Query("SELECT eq FROM ExamQuestion eq JOIN FETCH eq.question WHERE eq.exam.id = :examId AND eq.question.id = :questionId")
    Optional<ExamQuestion> findByExamIdAndQuestionId(@Param("examId") Long examId, @Param("questionId") Long questionId);
    
    // Check if question exists in exam
    boolean existsByExamIdAndQuestionId(Long examId, Long questionId);
    
    // Count questions in exam
    long countByExamId(Long examId);
    
    // Find by exam
    List<ExamQuestion> findByExamId(Long examId);
    
    // Find by question (which exams use this question)
    List<ExamQuestion> findByQuestionId(Long questionId);
    
    // Count how many exams use this question
    long countByQuestionId(Long questionId);
    
    // Count how many PUBLISHED exams use this question
    @Query("SELECT COUNT(eq) FROM ExamQuestion eq WHERE eq.question.id = :questionId " +
           "AND eq.exam.isPublished = true AND eq.exam.deletedAt IS NULL")
    Long countPublishedExamsByQuestionId(@Param("questionId") Long questionId);
    
    // Delete all questions from an exam
    void deleteByExamId(Long examId);
    
    // Delete specific question from exam
    void deleteByExamIdAndQuestionId(Long examId, Long questionId);
    
    // Find max order in exam (for adding new question)
    @Query("SELECT COALESCE(MAX(eq.questionOrder), 0) FROM ExamQuestion eq WHERE eq.exam.id = :examId")
    Integer findMaxOrderByExamId(@Param("examId") Long examId);
    
    // Calculate total points of an exam
    @Query("SELECT COALESCE(SUM(eq.points), 0) FROM ExamQuestion eq WHERE eq.exam.id = :examId")
    Double calculateTotalPointsByExamId(@Param("examId") Long examId);
}

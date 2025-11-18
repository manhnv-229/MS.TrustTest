package com.mstrust.exam.repository;

import com.mstrust.exam.entity.Difficulty;
import com.mstrust.exam.entity.Question;
import com.mstrust.exam.entity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** ------------------------------------------
 * Mục đích: Repository interface cho Question entity
 * 
 * Chức năng:
 * - CRUD operations cho Question
 * - Tìm kiếm questions theo subject, type, difficulty
 * - Hỗ trợ Question Bank management
 * - Soft delete aware queries
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:23
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    // Basic queries với soft delete
    Optional<Question> findByIdAndDeletedAtIsNull(Long id);
    
    List<Question> findByDeletedAtIsNull();
    
    Page<Question> findByDeletedAtIsNull(Pageable pageable);
    
    // Filter by subject
    Page<Question> findBySubjectIdAndDeletedAtIsNull(Long subjectId, Pageable pageable);
    
    List<Question> findBySubjectIdAndDeletedAtIsNull(Long subjectId);
    
    // Filter by question type
    Page<Question> findByQuestionTypeAndDeletedAtIsNull(QuestionType questionType, Pageable pageable);
    
    List<Question> findByQuestionTypeAndDeletedAtIsNull(QuestionType questionType);
    
    // Filter by difficulty
    Page<Question> findByDifficultyAndDeletedAtIsNull(Difficulty difficulty, Pageable pageable);
    
    List<Question> findByDifficultyAndDeletedAtIsNull(Difficulty difficulty);
    
    // Filter by created by
    Page<Question> findByCreatedByAndDeletedAtIsNull(Long createdBy, Pageable pageable);
    
    List<Question> findByCreatedByAndDeletedAtIsNull(Long createdBy);
    
    // Combined filters
    @Query("SELECT q FROM Question q WHERE q.deletedAt IS NULL " +
           "AND (:subjectId IS NULL OR q.subject.id = :subjectId) " +
           "AND (:questionType IS NULL OR q.questionType = :questionType) " +
           "AND (:difficulty IS NULL OR q.difficulty = :difficulty)")
    Page<Question> searchQuestions(
        @Param("subjectId") Long subjectId,
        @Param("questionType") QuestionType questionType,
        @Param("difficulty") Difficulty difficulty,
        Pageable pageable
    );
    
    // Search by keyword (in question text)
    @Query("SELECT q FROM Question q WHERE q.deletedAt IS NULL " +
           "AND LOWER(q.questionText) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Question> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Count by subject
    long countBySubjectIdAndDeletedAtIsNull(Long subjectId);
    
    // Count by type
    long countByQuestionTypeAndDeletedAtIsNull(QuestionType questionType);
    
    // Count by difficulty
    long countByDifficultyAndDeletedAtIsNull(Difficulty difficulty);
    
    // Count by creator
    long countByCreatedByAndDeletedAtIsNull(Long createdBy);
}

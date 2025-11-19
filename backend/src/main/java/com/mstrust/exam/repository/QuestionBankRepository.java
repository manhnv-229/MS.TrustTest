package com.mstrust.exam.repository;

import com.mstrust.exam.entity.Difficulty;
import com.mstrust.exam.entity.QuestionBank;
import com.mstrust.exam.entity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho QuestionBank - Ngân hàng câu hỏi
 * CreatedBy: K24DTCN210-NVMANH (19/11/2025 01:09)
 */
@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {
    
    /* ---------------------------------------------------
     * Tìm câu hỏi theo ID và chưa bị xóa
     * @param id ID câu hỏi
     * @returns Optional<QuestionBank>
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    Optional<QuestionBank> findByIdAndDeletedAtIsNull(Long id);
    
    /* ---------------------------------------------------
     * Tìm tất cả câu hỏi của một môn học
     * @param subjectId ID môn học
     * @returns List<QuestionBank>
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    List<QuestionBank> findBySubjectIdAndDeletedAtIsNull(Long subjectId);
    
    /* ---------------------------------------------------
     * Tìm câu hỏi theo độ khó
     * @param difficulty Độ khó (EASY/MEDIUM/HARD)
     * @returns List<QuestionBank>
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    List<QuestionBank> findByDifficultyAndDeletedAtIsNull(Difficulty difficulty);
    
    /* ---------------------------------------------------
     * Tìm câu hỏi theo loại
     * @param type Loại câu hỏi
     * @returns List<QuestionBank>
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    List<QuestionBank> findByQuestionTypeAndDeletedAtIsNull(QuestionType type);
    
    /* ---------------------------------------------------
     * Tìm kiếm câu hỏi theo từ khóa
     * @param keyword Từ khóa tìm kiếm
     * @returns List<QuestionBank>
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    @Query("SELECT q FROM QuestionBank q WHERE q.questionText LIKE %:keyword% AND q.deletedAt IS NULL")
    List<QuestionBank> searchByKeyword(@Param("keyword") String keyword);
    
    /* ---------------------------------------------------
     * Filter câu hỏi theo nhiều tiêu chí
     * @param subjectId ID môn học (optional)
     * @param difficulty Độ khó (optional)
     * @param type Loại câu hỏi (optional)
     * @param keyword Từ khóa tìm kiếm (optional)
     * @param pageable Phân trang
     * @returns Page<QuestionBank>
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    @Query("SELECT q FROM QuestionBank q WHERE " +
           "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty) AND " +
           "(:type IS NULL OR q.questionType = :type) AND " +
           "(:keyword IS NULL OR q.questionText LIKE %:keyword%) AND " +
           "q.deletedAt IS NULL")
    Page<QuestionBank> filterQuestions(
        @Param("subjectId") Long subjectId,
        @Param("difficulty") Difficulty difficulty,
        @Param("type") QuestionType type,
        @Param("keyword") String keyword,
        Pageable pageable
    );
    
    /* ---------------------------------------------------
     * Đếm số câu hỏi của một môn học
     * @param subjectId ID môn học
     * @returns long
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(q) FROM QuestionBank q WHERE q.subject.id = :subjectId AND q.deletedAt IS NULL")
    long countBySubject(@Param("subjectId") Long subjectId);
    
    /* ---------------------------------------------------
     * Thống kê câu hỏi theo độ khó trong một môn học
     * @param subjectId ID môn học
     * @returns List<Object[]> - [DifficultyLevel, count]
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    @Query("SELECT q.difficulty, COUNT(q) FROM QuestionBank q " +
           "WHERE q.subject.id = :subjectId AND q.deletedAt IS NULL " +
           "GROUP BY q.difficulty")
    List<Object[]> getStatisticsByDifficulty(@Param("subjectId") Long subjectId);
    
    /* ---------------------------------------------------
     * Thống kê câu hỏi theo loại trong một môn học
     * @param subjectId ID môn học
     * @returns List<Object[]> - [QuestionType, count]
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    @Query("SELECT q.questionType, COUNT(q) FROM QuestionBank q " +
           "WHERE q.subject.id = :subjectId AND q.deletedAt IS NULL " +
           "GROUP BY q.questionType")
    List<Object[]> getStatisticsByType(@Param("subjectId") Long subjectId);
    
    /* ---------------------------------------------------
     * Lấy danh sách câu hỏi của một giáo viên
     * @param teacherId ID giáo viên
     * @param pageable Phân trang
     * @returns Page<QuestionBank>
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    @Query("SELECT q FROM QuestionBank q WHERE q.createdBy.id = :teacherId AND q.deletedAt IS NULL")
    Page<QuestionBank> findByCreator(@Param("teacherId") Long teacherId, Pageable pageable);
    
    /* ---------------------------------------------------
     * Kiểm tra câu hỏi có đang được sử dụng trong bài thi nào không
     * @param questionId ID câu hỏi
     * @returns boolean
     * @author: K24DTCN210-NVMANH (19/11/2025 01:09)
     * --------------------------------------------------- */
    @Query("SELECT CASE WHEN COUNT(eq) > 0 THEN true ELSE false END " +
           "FROM ExamQuestion eq WHERE eq.question.id = :questionId")
    boolean isQuestionInUse(@Param("questionId") Long questionId);
}

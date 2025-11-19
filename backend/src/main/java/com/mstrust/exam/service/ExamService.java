package com.mstrust.exam.service;

import com.mstrust.exam.dto.*;
import com.mstrust.exam.entity.*;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * Service xử lý business logic cho Exam
 * Chức năng:
 * - CRUD operations cho Exam
 * - Validation business rules
 * - Mapping Entity <-> DTO
 * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
 * --------------------------------------------------- */
@Service
@Transactional
@RequiredArgsConstructor
public class ExamService {
    
    private final ExamRepository examRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final UserRepository userRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionBankRepository questionBankRepository;
    
    /* ---------------------------------------------------
     * Tạo exam mới
     * @param request Thông tin exam
     * @param currentUserId ID của user đang tạo (teacher/admin)
     * @returns ExamDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    public ExamDTO createExam(CreateExamRequest request, Long currentUserId) {
        // Validate subject class exists (400 Bad Request cho invalid foreign key)
        SubjectClass subjectClass = subjectClassRepository.findById(request.getSubjectClassId())
            .orElseThrow(() -> new BadRequestException("Invalid subject class ID: " + request.getSubjectClassId()));
        
        // Validate time constraints
        validateTimeConstraints(request.getStartTime(), request.getEndTime(), request.getDurationMinutes());
        
        // Validate scores
        validateScores(request.getPassingScore(), request.getTotalScore());
        
        // Create exam entity
        Exam exam = Exam.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .subjectClass(subjectClass)
            .examPurpose(request.getExamPurpose())
            .examFormat(request.getExamFormat())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .durationMinutes(request.getDurationMinutes())
            .passingScore(request.getPassingScore())
            .totalScore(request.getTotalScore())
            .randomizeQuestions(request.getRandomizeQuestions())
            .randomizeOptions(request.getRandomizeOptions())
            .allowReviewAfterSubmit(request.getAllowReviewAfterSubmit())
            .showCorrectAnswers(request.getShowCorrectAnswers())
            .allowCodeExecution(request.getAllowCodeExecution())
            .programmingLanguage(request.getProgrammingLanguage())
            .isPublished(false)
            .createdBy(currentUserId)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        Exam saved = examRepository.save(exam);
        
        return mapToDTO(saved);
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách exam với filters
     * @param subjectClassId Filter theo subject class (optional)
     * @param examPurpose Filter theo exam purpose (optional)
     * @param examFormat Filter theo exam format (optional)
     * @param isPublished Filter theo published status (optional)
     * @param pageable Phân trang
     * @returns Page<ExamSummaryDTO>
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public Page<ExamSummaryDTO> getExams(Long subjectClassId, ExamPurpose examPurpose, 
                                         ExamFormat examFormat, Boolean isPublished, 
                                         Pageable pageable) {
        Page<Exam> exams = examRepository.searchExams(subjectClassId, examPurpose, examFormat, isPublished, pageable);
        return exams.map(this::mapToSummaryDTO);
    }
    
    /* ---------------------------------------------------
     * Lấy chi tiết exam theo ID
     * @param id ID của exam
     * @returns ExamDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public ExamDTO getExamById(Long id) {
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        
        return mapToDTO(exam);
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách exam theo subject class
     * @param subjectClassId ID của subject class
     * @param pageable Phân trang
     * @returns Page<ExamSummaryDTO>
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public Page<ExamSummaryDTO> getExamsBySubjectClass(Long subjectClassId, Pageable pageable) {
        // Validate subject class exists (400 Bad Request cho invalid foreign key)
        subjectClassRepository.findById(subjectClassId)
            .orElseThrow(() -> new BadRequestException("Invalid subject class ID: " + subjectClassId));
        
        Page<Exam> exams = examRepository.findBySubjectClassIdAndDeletedAtIsNull(subjectClassId, pageable);
        return exams.map(this::mapToSummaryDTO);
    }
    
    /* ---------------------------------------------------
     * Cập nhật exam
     * @param id ID của exam
     * @param request Thông tin cập nhật
     * @param currentUserId ID của user đang cập nhật
     * @returns ExamDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    public ExamDTO updateExam(Long id, UpdateExamRequest request, Long currentUserId) {
        // Find existing exam
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        
        // Check version for optimistic locking
        if (!exam.getVersion().equals(request.getVersion())) {
            throw new BadRequestException("Exam has been modified by another user. Please refresh and try again.");
        }
        
        // Update fields if provided
        if (request.getTitle() != null) {
            exam.setTitle(request.getTitle());
        }
        
        if (request.getDescription() != null) {
            exam.setDescription(request.getDescription());
        }
        
        if (request.getSubjectClassId() != null) {
            SubjectClass subjectClass = subjectClassRepository.findById(request.getSubjectClassId())
                .orElseThrow(() -> new BadRequestException("Invalid subject class ID: " + request.getSubjectClassId()));
            exam.setSubjectClass(subjectClass);
        }
        
        if (request.getExamPurpose() != null) {
            exam.setExamPurpose(request.getExamPurpose());
        }
        
        if (request.getExamFormat() != null) {
            exam.setExamFormat(request.getExamFormat());
        }
        
        if (request.getStartTime() != null) {
            exam.setStartTime(request.getStartTime());
        }
        
        if (request.getEndTime() != null) {
            exam.setEndTime(request.getEndTime());
        }
        
        if (request.getDurationMinutes() != null) {
            exam.setDurationMinutes(request.getDurationMinutes());
        }
        
        // Validate time constraints if any time field changed
        if (request.getStartTime() != null || request.getEndTime() != null || request.getDurationMinutes() != null) {
            validateTimeConstraints(exam.getStartTime(), exam.getEndTime(), exam.getDurationMinutes());
        }
        
        if (request.getPassingScore() != null) {
            exam.setPassingScore(request.getPassingScore());
        }
        
        if (request.getTotalScore() != null) {
            exam.setTotalScore(request.getTotalScore());
        }
        
        // Validate scores if any score field changed
        if (request.getPassingScore() != null || request.getTotalScore() != null) {
            validateScores(exam.getPassingScore(), exam.getTotalScore());
        }
        
        // Update boolean settings
        if (request.getRandomizeQuestions() != null) {
            exam.setRandomizeQuestions(request.getRandomizeQuestions());
        }
        
        if (request.getRandomizeOptions() != null) {
            exam.setRandomizeOptions(request.getRandomizeOptions());
        }
        
        if (request.getAllowReviewAfterSubmit() != null) {
            exam.setAllowReviewAfterSubmit(request.getAllowReviewAfterSubmit());
        }
        
        if (request.getShowCorrectAnswers() != null) {
            exam.setShowCorrectAnswers(request.getShowCorrectAnswers());
        }
        
        if (request.getAllowCodeExecution() != null) {
            exam.setAllowCodeExecution(request.getAllowCodeExecution());
        }
        
        if (request.getProgrammingLanguage() != null) {
            exam.setProgrammingLanguage(request.getProgrammingLanguage());
        }
        
        exam.setUpdatedBy(currentUserId);
        exam.setUpdatedAt(LocalDateTime.now());
        
        Exam updated = examRepository.save(exam);
        
        return mapToDTO(updated);
    }
    
    /* ---------------------------------------------------
     * Soft delete exam
     * @param id ID của exam cần xóa
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    public void deleteExam(Long id) {
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        
        // Check if exam is published
        if (exam.getIsPublished()) {
            throw new BadRequestException("Cannot delete published exam. Please unpublish it first.");
        }
        
        // Soft delete
        examRepository.delete(exam);
    }
    
    /* ---------------------------------------------------
     * Publish exam - cho phép students thấy và làm bài thi
     * Business rules:
     * - Exam phải có ít nhất 1 câu hỏi
     * - startTime phải trong tương lai
     * - Chỉ có thể publish exam chưa publish
     * @param id ID của exam cần publish
     * @param currentUserId ID của user đang thực hiện
     * @returns ExamDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 08:46)
     * --------------------------------------------------- */
    public ExamDTO publishExam(Long id, Long currentUserId) {
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        
        // Check if already published
        if (exam.getIsPublished()) {
            throw new BadRequestException("Exam is already published");
        }
        
        // Validate exam has at least 1 question
        long questionCount = examQuestionRepository.countByExamId(exam.getId());
        if (questionCount == 0) {
            throw new BadRequestException("Cannot publish exam without questions. Please add questions first.");
        }
        
        // Validate start time is in the future
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot publish exam with past start time. Please update start time first.");
        }
        
        // Publish exam
        exam.setIsPublished(true);
        exam.setUpdatedBy(currentUserId);
        exam.setUpdatedAt(LocalDateTime.now());
        
        Exam updated = examRepository.save(exam);
        
        return mapToDTO(updated);
    }
    
    /* ---------------------------------------------------
     * Unpublish exam - ẩn exam khỏi students
     * Business rules:
     * - Chỉ có thể unpublish exam đã publish
     * - Không unpublish được exam đang diễn ra (ONGOING)
     * - Có thể unpublish exam chưa bắt đầu hoặc đã kết thúc
     * @param id ID của exam cần unpublish
     * @param currentUserId ID của user đang thực hiện
     * @returns ExamDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 08:46)
     * --------------------------------------------------- */
    public ExamDTO unpublishExam(Long id, Long currentUserId) {
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        
        // Check if not published
        if (!exam.getIsPublished()) {
            throw new BadRequestException("Exam is not published");
        }
        
        // Check exam status - không được unpublish exam đang diễn ra
        ExamStatus currentStatus = exam.getCurrentStatus();
        if (currentStatus == ExamStatus.ONGOING) {
            throw new BadRequestException("Cannot unpublish ongoing exam. Please wait until exam ends.");
        }
        
        // Unpublish exam
        exam.setIsPublished(false);
        exam.setUpdatedBy(currentUserId);
        exam.setUpdatedAt(LocalDateTime.now());
        
        Exam updated = examRepository.save(exam);
        
        return mapToDTO(updated);
    }
    
    /* ---------------------------------------------------
     * Validate thời gian thi hợp lệ
     * - startTime < endTime
     * - duration <= time window
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    private void validateTimeConstraints(LocalDateTime startTime, LocalDateTime endTime, Integer durationMinutes) {
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new BadRequestException("Start time must be before end time");
        }
        
        // Check if duration fits within time window
        long minutesBetween = java.time.Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes > minutesBetween) {
            throw new BadRequestException("Duration (" + durationMinutes + " minutes) exceeds time window (" + minutesBetween + " minutes)");
        }
    }
    
    /* ---------------------------------------------------
     * Validate điểm số hợp lệ
     * - passingScore <= totalScore
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    private void validateScores(java.math.BigDecimal passingScore, java.math.BigDecimal totalScore) {
        if (passingScore.compareTo(totalScore) > 0) {
            throw new BadRequestException("Passing score cannot be greater than total score");
        }
    }
    
    /* ---------------------------------------------------
     * Map Exam entity sang ExamDTO (full details)
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    private ExamDTO mapToDTO(Exam exam) {
        SubjectClass sc = exam.getSubjectClass();
        
        // Count questions
        long count = examQuestionRepository.countByExamId(exam.getId());
        
        return ExamDTO.builder()
            .id(exam.getId())
            .title(exam.getTitle())
            .description(exam.getDescription())
            .subjectClassId(sc.getId())
            .subjectClassName(sc.getCode())
            .subjectId(sc.getSubject().getId())
            .subjectName(sc.getSubject().getSubjectName())
            .classId(null) // SubjectClass không có ClassEntity
            .className(null)
            .examPurpose(exam.getExamPurpose())
            .examFormat(exam.getExamFormat())
            .startTime(exam.getStartTime())
            .endTime(exam.getEndTime())
            .durationMinutes(exam.getDurationMinutes())
            .passingScore(exam.getPassingScore())
            .totalScore(exam.getTotalScore())
            .randomizeQuestions(exam.getRandomizeQuestions())
            .randomizeOptions(exam.getRandomizeOptions())
            .allowReviewAfterSubmit(exam.getAllowReviewAfterSubmit())
            .showCorrectAnswers(exam.getShowCorrectAnswers())
            .allowCodeExecution(exam.getAllowCodeExecution())
            .programmingLanguage(exam.getProgrammingLanguage())
            .isPublished(exam.getIsPublished())
            .currentStatus(exam.getCurrentStatus())
            .questionCount((int) count)
            .submissionCount(0) // TODO: implement in Phase 6
            .version(exam.getVersion())
            .createdAt(exam.getCreatedAt())
            .updatedAt(exam.getUpdatedAt())
            .createdByName(getUserName(exam.getCreatedBy()))
            .updatedByName(exam.getUpdatedBy() != null ? getUserName(exam.getUpdatedBy()) : null)
            .build();
    }
    
    /* ---------------------------------------------------
     * Map Exam entity sang ExamSummaryDTO (lightweight)
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    private ExamSummaryDTO mapToSummaryDTO(Exam exam) {
        SubjectClass sc = exam.getSubjectClass();
        long count = examQuestionRepository.countByExamId(exam.getId());
        
        return ExamSummaryDTO.builder()
            .id(exam.getId())
            .title(exam.getTitle())
            .subjectClassId(sc.getId())
            .subjectClassName(sc.getCode())
            .subjectName(sc.getSubject().getSubjectName())
            .className(null)
            .examPurpose(exam.getExamPurpose())
            .examFormat(exam.getExamFormat())
            .startTime(exam.getStartTime())
            .endTime(exam.getEndTime())
            .durationMinutes(exam.getDurationMinutes())
            .isPublished(exam.getIsPublished())
            .currentStatus(exam.getCurrentStatus())
            .questionCount((int) count)
            .submissionCount(0) // TODO: implement in Phase 6
            .createdAt(exam.getCreatedAt())
            .createdByName(getUserName(exam.getCreatedBy()))
            .build();
    }
    
    /* ---------------------------------------------------
     * Thêm câu hỏi vào bài thi
     * Business rules:
     * - Exam phải tồn tại và chưa bị xóa
     * - Exam chưa được publish hoặc đang ở trạng thái UPCOMING
     * - Question phải tồn tại trong QuestionBank
     * - Không được thêm duplicate question
     * @param examId ID của exam
     * @param request Thông tin câu hỏi cần thêm
     * @param currentUserId ID của user đang thực hiện
     * @returns ExamQuestionDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 09:18)
     * --------------------------------------------------- */
    public ExamQuestionDTO addQuestionToExam(Long examId, AddQuestionToExamRequest request, Long currentUserId) {
        // Validate exam exists
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
        
        // Check if exam can be modified
        if (exam.getIsPublished() && exam.getCurrentStatus() == ExamStatus.ONGOING) {
            throw new BadRequestException("Cannot modify ongoing exam");
        }
        
        if (exam.getCurrentStatus() == ExamStatus.COMPLETED) {
            throw new BadRequestException("Cannot modify completed exam");
        }
        
        // Validate question exists (400 Bad Request cho invalid foreign key)
        QuestionBank question = questionBankRepository.findByIdAndDeletedAtIsNull(request.getQuestionId())
            .orElseThrow(() -> new BadRequestException("Invalid question ID: " + request.getQuestionId()));
        
        // Check duplicate
        if (examQuestionRepository.existsByExamIdAndQuestionId(examId, request.getQuestionId())) {
            throw new BadRequestException("Question already exists in this exam");
        }
        
        // Create exam-question relationship
        ExamQuestion examQuestion = ExamQuestion.builder()
            .exam(exam)
            .question(question)
            .questionOrder(request.getQuestionOrder())
            .points(request.getPoints())
            .build();
        
        ExamQuestion saved = examQuestionRepository.save(examQuestion);
        
        // Update exam metadata
        exam.setUpdatedBy(currentUserId);
        exam.setUpdatedAt(LocalDateTime.now());
        examRepository.save(exam);
        
        return mapToExamQuestionDTO(saved);
    }
    
    /* ---------------------------------------------------
     * Xóa câu hỏi khỏi bài thi
     * Business rules:
     * - Exam phải tồn tại
     * - Exam không được đang ONGOING hoặc COMPLETED
     * - ExamQuestion phải tồn tại
     * - Sau khi xóa, cập nhật lại questionOrder cho các câu còn lại
     * @param examId ID của exam
     * @param questionId ID của question cần xóa
     * @param currentUserId ID của user đang thực hiện
     * @author: K24DTCN210-NVMANH (19/11/2025 09:18)
     * --------------------------------------------------- */
    public void removeQuestionFromExam(Long examId, Long questionId, Long currentUserId) {
        // Validate exam exists
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
        
        // Check if exam can be modified
        ExamStatus currentStatus = exam.getCurrentStatus();
        if (currentStatus == ExamStatus.ONGOING) {
            throw new BadRequestException("Cannot remove questions from ongoing exam");
        }
        
        if (currentStatus == ExamStatus.COMPLETED) {
            throw new BadRequestException("Cannot remove questions from completed exam");
        }
        
        // Find exam-question relationship
        ExamQuestion examQuestion = examQuestionRepository.findByExamIdAndQuestionId(examId, questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Question not found in this exam"));
        
        // Delete exam-question
        examQuestionRepository.delete(examQuestion);
        
        // Recalculate order for remaining questions
        java.util.List<ExamQuestion> remainingQuestions = examQuestionRepository
            .findByExamIdOrderByQuestionOrder(examId);
        
        for (int i = 0; i < remainingQuestions.size(); i++) {
            remainingQuestions.get(i).setQuestionOrder(i + 1);
        }
        examQuestionRepository.saveAll(remainingQuestions);
        
        // Update exam metadata
        exam.setUpdatedBy(currentUserId);
        exam.setUpdatedAt(LocalDateTime.now());
        examRepository.save(exam);
    }
    
    /* ---------------------------------------------------
     * Sắp xếp lại thứ tự câu hỏi trong bài thi
     * Business rules:
     * - Exam phải tồn tại
     * - Exam chưa published hoặc đang UPCOMING
     * - Tất cả questions trong request phải tồn tại trong exam
     * Strategy: Set temporary negative order first to avoid unique constraint violation
     * @param examId ID của exam
     * @param request Danh sách thứ tự mới
     * @param currentUserId ID của user đang thực hiện
     * @returns List<ExamQuestionDTO>
     * @author: K24DTCN210-NVMANH (19/11/2025 09:18)
     * EditBy: K24DTCN210-NVMANH (19/11/2025 14:03) - Fix unique constraint violation
     * EditBy: K24DTCN210-NVMANH (19/11/2025 14:19) - Add flush to ensure DB updates
     * --------------------------------------------------- */
    public java.util.List<ExamQuestionDTO> reorderQuestions(Long examId, ReorderQuestionsRequest request, Long currentUserId) {
        // Validate exam exists
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
        
        // Check if exam can be modified - chỉ được reorder khi exam chưa bắt đầu (DRAFT hoặc PUBLISHED nhưng chưa đến startTime)
        ExamStatus currentStatus = exam.getCurrentStatus();
        if (currentStatus == ExamStatus.ONGOING || currentStatus == ExamStatus.COMPLETED) {
            throw new BadRequestException("Cannot reorder questions in ongoing or completed exam");
        }
        
        // Step 1: Set temporary negative order to avoid unique constraint violation
        // Example: if swapping order 1 and 2, first set all to negative values
        java.util.List<ExamQuestion> questionsToUpdate = new java.util.ArrayList<>();
        
        for (ReorderQuestionsRequest.QuestionOrder qo : request.getQuestions()) {
            ExamQuestion examQuestion = examQuestionRepository.findByExamIdAndQuestionId(examId, qo.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question " + qo.getQuestionId() + " not found in exam"));
            
            // Set temporary negative order
            examQuestion.setQuestionOrder(-qo.getNewOrder());
            questionsToUpdate.add(examQuestion);
        }
        
        // Save with temporary negative orders and flush to DB
        examQuestionRepository.saveAllAndFlush(questionsToUpdate);
        
        // Step 2: Set actual order (convert back from negative)
        for (ExamQuestion eq : questionsToUpdate) {
            eq.setQuestionOrder(-eq.getQuestionOrder());
        }
        
        // Save with actual orders and flush to DB
        examQuestionRepository.saveAllAndFlush(questionsToUpdate);
        
        // Update exam metadata
        exam.setUpdatedBy(currentUserId);
        exam.setUpdatedAt(LocalDateTime.now());
        examRepository.save(exam);
        
        // Return updated list ordered by new order
        java.util.List<ExamQuestion> updated = examQuestionRepository.findByExamIdOrderByQuestionOrder(examId);
        return updated.stream()
            .map(this::mapToExamQuestionDTO)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Cập nhật điểm số của câu hỏi trong bài thi
     * Business rules:
     * - Exam phải tồn tại
     * - ExamQuestion phải tồn tại
     * - Có thể cập nhật ngay cả khi exam đã published (để điều chỉnh)
     * @param examId ID của exam
     * @param questionId ID của question
     * @param request Điểm số mới
     * @param currentUserId ID của user đang thực hiện
     * @returns ExamQuestionDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 09:18)
     * --------------------------------------------------- */
    public ExamQuestionDTO updateQuestionScore(Long examId, Long questionId, UpdateQuestionScoreRequest request, Long currentUserId) {
        // Validate exam exists
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
        
        // Find exam-question relationship
        ExamQuestion examQuestion = examQuestionRepository.findByExamIdAndQuestionId(examId, questionId)
            .orElseThrow(() -> new ResourceNotFoundException("Question not found in this exam"));
        
        // Update points
        examQuestion.setPoints(request.getPoints());
        ExamQuestion updated = examQuestionRepository.save(examQuestion);
        
        // Update exam metadata
        exam.setUpdatedBy(currentUserId);
        exam.setUpdatedAt(LocalDateTime.now());
        examRepository.save(exam);
        
        return mapToExamQuestionDTO(updated);
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách câu hỏi trong bài thi
     * @param examId ID của exam
     * @returns List<ExamQuestionDTO>
     * @author: K24DTCN210-NVMANH (19/11/2025 09:18)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public java.util.List<ExamQuestionDTO> getExamQuestions(Long examId) {
        // Validate exam exists
        examRepository.findByIdAndDeletedAtIsNull(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + examId));
        
        java.util.List<ExamQuestion> examQuestions = examQuestionRepository.findByExamIdOrderByQuestionOrder(examId);
        
        return examQuestions.stream()
            .map(this::mapToExamQuestionDTO)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Map ExamQuestion entity sang ExamQuestionDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 09:18)
     * --------------------------------------------------- */
    private ExamQuestionDTO mapToExamQuestionDTO(ExamQuestion eq) {
        QuestionBank q = eq.getQuestion();
        Subject subject = q.getSubject();
        
        return ExamQuestionDTO.builder()
            .examQuestionId(eq.getId())
            .questionOrder(eq.getQuestionOrder())
            .points(eq.getPoints())
            .questionId(q.getId())
            .questionText(q.getQuestionText())
            .questionType(q.getQuestionType())
            .difficulty(q.getDifficulty().name())
            .subjectId(subject.getId())
            .subjectName(subject.getSubjectName())
            .createdAt(eq.getCreatedAt())
            .updatedAt(eq.getUpdatedAt())
            .build();
    }
    
    /* ---------------------------------------------------
     * Helper: Lấy tên user theo ID
     * @author: K24DTCN210-NVMANH (19/11/2025 08:40)
     * --------------------------------------------------- */
    private String getUserName(Long userId) {
        return userRepository.findById(userId)
            .map(User::getFullName)
            .orElse("Unknown");
    }
}

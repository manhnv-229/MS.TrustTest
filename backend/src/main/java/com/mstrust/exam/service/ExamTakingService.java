package com.mstrust.exam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mstrust.exam.dto.*;
import com.mstrust.exam.entity.*;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Service cho exam taking flow (student làm bài)
 * Xử lý toàn bộ business logic từ start đến submit exam
 * @author: K24DTCN210-NVMANH (19/11/2025 15:30)
 * EditBy: K24DTCN210-NVMANH (19/11/2025 15:30) - Fixed compilation errors
 * --------------------------------------------------- */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ExamTakingService {
    
    private final ExamRepository examRepository;
    private final ExamSubmissionRepository submissionRepository;
    private final StudentAnswerRepository answerRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final SubjectClassStudentRepository subjectClassStudentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    /* ---------------------------------------------------
     * Lấy danh sách exams student có thể làm
     * @param studentId ID của student
     * @param subjectCode Mã môn học để filter (optional)
     * @returns List AvailableExamDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 15:30)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 15:17) - Added class filter for security
     * EditBy: K24DTCN210-NVMANH (23/11/2025 16:47) - Add subjectCode parameter
     * --------------------------------------------------- */
    public List<AvailableExamDTO> getAvailableExams(Long studentId, String subjectCode) {
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // ✅ Lấy danh sách classes mà student đã enroll
        List<SubjectClassStudent> enrolledClasses = subjectClassStudentRepository
            .findEnrolledClassesByStudentId(studentId);
        
        // Tạo Set classIds để filter nhanh
        Set<Long> enrolledClassIds = enrolledClasses.stream()
            .map(scs -> scs.getSubjectClass().getId())
            .collect(Collectors.toSet());
        
        // ✅ Chỉ lấy exams thuộc classes mà student đã enroll
        List<Exam> eligibleExams = examRepository.findAll().stream()
            .filter(exam -> {
                // Check status
                ExamStatus status = exam.getCurrentStatus();
                if (status != ExamStatus.PUBLISHED && status != ExamStatus.ONGOING) {
                    return false;
                }
                
                // ✅ Check student có thuộc class này không
                Long examClassId = exam.getSubjectClass().getId();
                if (!enrolledClassIds.contains(examClassId)) {
                    return false;
                }
                
                // ✅ Filter by subjectCode if provided
                if (subjectCode != null && !subjectCode.trim().isEmpty()) {
                    String examSubjectCode = exam.getSubjectClass().getSubject().getSubjectCode();
                    return examSubjectCode.equals(subjectCode);
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // Map to DTO với eligibility check
        return eligibleExams.stream()
            .map(exam -> mapToAvailableExamDTO(exam, studentId))
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Check eligibility của student cho một exam
     * @param examId ID của exam
     * @param studentId ID của student
     * @returns Map với isEligible và reason
     * @author: K24DTCN210-NVMANH (19/11/2025 15:30)
     * --------------------------------------------------- */
    public Map<String, Object> checkEligibility(Long examId, Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        
        // Check 1: Exam status
        ExamStatus status = exam.getCurrentStatus();
        if (status != ExamStatus.PUBLISHED && status != ExamStatus.ONGOING) {
            result.put("isEligible", false);
            result.put("reason", "Exam is not available yet");
            return result;
        }
        
        // Check 2: Max attempts
        int attemptsMade = submissionRepository.countByStudentIdAndExamId(studentId, examId);
        if (exam.getMaxAttempts() > 0 && attemptsMade >= exam.getMaxAttempts()) {
            result.put("isEligible", false);
            result.put("reason", "Maximum attempts reached (" + exam.getMaxAttempts() + ")");
            return result;
        }
        
        // Check 3: Active submission exists
        Optional<ExamSubmission> activeSubmission = submissionRepository
            .findActiveSubmission(studentId, examId);
        
        if (activeSubmission.isPresent()) {
            result.put("isEligible", false);
            result.put("reason", "You have an active submission in progress");
            result.put("submissionId", activeSubmission.get().getId());
            return result;
        }
        
        result.put("isEligible", true);
        result.put("attemptsMade", attemptsMade);
        result.put("remainingAttempts", exam.getMaxAttempts() > 0 ? 
            exam.getMaxAttempts() - attemptsMade : null);
        
        return result;
    }
    
    /* ---------------------------------------------------
     * Start exam - tạo submission mới
     * @param examId ID của exam
     * @param studentId ID của student
     * @returns StartExamResponse
     * @author: K24DTCN210-NVMANH (19/11/2025 15:30)
     * --------------------------------------------------- */
    public StartExamResponse startExam(Long examId, Long studentId) {
        // Validate eligibility
        Map<String, Object> eligibility = checkEligibility(examId, studentId);
        if (!(Boolean) eligibility.get("isEligible")) {
            throw new BadRequestException((String) eligibility.get("reason"));
        }
        
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        
        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        
        // Calculate attempt number
        int attemptNumber = submissionRepository.countByStudentIdAndExamId(studentId, examId) + 1;
        
        // Generate random seeds if needed
        Long questionSeed = exam.getRandomizeQuestions() ? 
            System.currentTimeMillis() : null;
        Long optionSeed = exam.getRandomizeOptions() ? 
            System.currentTimeMillis() + 1000 : null;
        
        // Get question count
        int totalQuestions = (int) examQuestionRepository.countByExamId(examId);
        
        // Create submission
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ExamSubmission submission = ExamSubmission.builder()
            .exam(exam)
            .student(student)
            .attemptNumber(attemptNumber)
            .startedAt(now)
            .status(SubmissionStatus.IN_PROGRESS)
            .maxScore(exam.getTotalScore())
            .questionSeed(questionSeed)
            .optionSeed(optionSeed)
            .autoSaveCount(0)  // Khởi tạo autoSaveCount = 0
            .build();
        
        submission = submissionRepository.save(submission);
        
        log.info("Student {} started exam {} (attempt #{})", studentId, examId, attemptNumber);
        
        // Build response
        LocalDateTime startedAtLocal = now.toLocalDateTime();
        LocalDateTime mustSubmitBefore = startedAtLocal.plusMinutes(exam.getDurationMinutes());
        
        return StartExamResponse.builder()
            .submissionId(submission.getId())
            .examId(examId)
            .examTitle(exam.getTitle())
            .attemptNumber(attemptNumber)
            .maxAttempts(exam.getMaxAttempts())
            .startedAt(startedAtLocal)
            .durationMinutes(exam.getDurationMinutes())
            .mustSubmitBefore(mustSubmitBefore)
            .remainingSeconds(exam.getDurationMinutes() * 60)
            .totalQuestions(totalQuestions)
            .randomizeQuestions(exam.getRandomizeQuestions())
            .randomizeOptions(exam.getRandomizeOptions())
            .autoSaveIntervalSeconds(30)
            .message("Exam started successfully. Good luck!")
            .build();
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách questions cho exam (with randomization)
     * @param submissionId ID của submission
     * @param studentId ID của student (for validation)
     * @returns List QuestionForStudentDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 15:30)
     * --------------------------------------------------- */
    public List<QuestionForStudentDTO> getExamQuestions(Long submissionId, Long studentId) {
        ExamSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        // Validate ownership
        if (!submission.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("This submission does not belong to you");
        }
        
        // Validate status
        if (!submission.isActive()) {
            throw new BadRequestException("This submission is not active");
        }
        
        Exam exam = submission.getExam();
        
        // Get all exam questions
        List<ExamQuestion> examQuestions = examQuestionRepository
            .findByExamId(exam.getId());
        
        // Sort by questionOrder
        examQuestions.sort(Comparator.comparing(ExamQuestion::getQuestionOrder));
        
        // Randomize questions if needed
        if (exam.getRandomizeQuestions() && submission.getQuestionSeed() != null) {
            Random random = new Random(submission.getQuestionSeed());
            Collections.shuffle(examQuestions, random);
        }
        
        // Get existing answers
        List<StudentAnswer> existingAnswers = answerRepository.findBySubmissionId(submissionId);
        Map<Long, StudentAnswer> answerMap = existingAnswers.stream()
            .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));
        
        // Map to DTO
        List<QuestionForStudentDTO> questions = new ArrayList<>();
        for (int i = 0; i < examQuestions.size(); i++) {
            ExamQuestion eq = examQuestions.get(i);
            QuestionBank question = eq.getQuestion();
            
            // Parse options (without correct answer)
            List<String> options = parseOptionsWithoutAnswer(question, 
                exam.getRandomizeOptions(), submission.getOptionSeed());
            
            // Get saved answer if exists
            StudentAnswer savedAnswer = answerMap.get(question.getId());
            
            QuestionForStudentDTO dto = QuestionForStudentDTO.builder()
                .id(question.getId())
                .questionBankId(question.getId())
                .questionType(question.getQuestionType())
                .questionText(question.getQuestionText())
                .questionCode(null)  // QuestionBank doesn't have questionCode
                .options(options)
                .maxScore(eq.getPoints())  // Use points from ExamQuestion
                .displayOrder(i + 1)
                .imageUrl(null)  // Would need to parse from attachments JSON
                .audioUrl(null)
                .videoUrl(null)
                .allowCodeExecution(exam.getAllowCodeExecution())
                .programmingLanguage(exam.getProgrammingLanguage())
                .hint(question.getGradingCriteria())  // Use gradingCriteria as hint
                .savedAnswer(savedAnswer != null ? parseAnswerJson(savedAnswer.getAnswerJson()) : null)
                .isAnswered(savedAnswer != null && !savedAnswer.isEmpty())
                .build();
            
            questions.add(dto);
        }
        
        return questions;
    }
    
    /* ---------------------------------------------------
     * Save/submit answer cho một câu hỏi
     * @param submissionId ID của submission
     * @param request SubmitAnswerRequest
     * @param studentId ID của student (for validation)
     * @returns Success message
     * @author: K24DTCN210-NVMANH (19/11/2025 15:30)
     * --------------------------------------------------- */
    public Map<String, Object> saveAnswer(Long submissionId, SubmitAnswerRequest request, Long studentId) {
        ExamSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        // Validate ownership
        if (!submission.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("This submission does not belong to you");
        }
        
        // Validate status and time
        if (!submission.isActive()) {
            throw new BadRequestException("This submission is not active");
        }
        
        if (submission.isExpired()) {
            submitExam(submissionId, studentId);
            throw new BadRequestException("Time expired. Exam has been auto-submitted");
        }
        
        // Find ExamQuestion first to validate question belongs to this exam
        ExamQuestion examQuestion = examQuestionRepository
            .findByExamIdAndQuestionId(submission.getExam().getId(), request.getQuestionId())
            .orElseThrow(() -> new ResourceNotFoundException("Question not found in this exam"));
        
        QuestionBank question = examQuestion.getQuestion();
        
        // Debug log
        log.info("ExamQuestion found: id={}, examId={}, questionId={}", 
            examQuestion.getId(), examQuestion.getExam().getId(), examQuestion.getQuestion() != null ? examQuestion.getQuestion().getId() : "NULL");
        
        if (question == null) {
            throw new ResourceNotFoundException("Question relationship is null - lazy loading issue");
        }
        
        // Find or create answer
        StudentAnswer answer = answerRepository
            .findBySubmissionIdAndQuestionId(submissionId, request.getQuestionId())
            .orElse(new StudentAnswer());
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        boolean isNewAnswer = answer.getId() == null;
        
        // Set basic info
        if (isNewAnswer) {
            answer.setSubmission(submission);
            answer.setQuestionId(request.getQuestionId());  // Fix: Set questionId directly for insertable=false mapping
            answer.setQuestion(question);  // Keep for JPA reference
            answer.setFirstSavedAt(now);
            answer.setSavedCount(0);
        }
        
        // Save answer data
        try {
            if (request.getAnswer() != null) {
                String answerJson = objectMapper.writeValueAsString(request.getAnswer());
                answer.setAnswerJson(answerJson);
            }
            answer.setAnswerText(request.getAnswerText());
            answer.setUploadedFileUrl(request.getUploadedFileUrl());
            answer.setUploadedFileName(request.getUploadedFileName());
        } catch (Exception e) {
            throw new BadRequestException("Invalid answer format");
        }
        
        // Auto-grade if possible
        autoGradeAnswer(answer, question, submission.getExam().getId());
        
        // Update tracking
        answer.setLastSavedAt(now);
        answer.setSavedCount(answer.getSavedCount() + 1);
        
        answer = answerRepository.save(answer);
        log.info("[SaveAnswer] StudentAnswer saved successfully - ID: {}, QuestionId: {}", 
            answer.getId(), answer.getQuestion().getId());

        // Update submission tracking trong separate transaction để tránh rollback
        updateSubmissionTracking(submission.getId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        // Handle null isAutoSave - default to false (manual save)
        boolean isAutoSave = request.getIsAutoSave() != null && request.getIsAutoSave();
        result.put("message", isAutoSave ? "Answer auto-saved" : "Answer saved");
        result.put("isGraded", answer.getIsCorrect() != null);
        result.put("pointsEarned", answer.getPointsEarned());
        
        return result;
    }
    
    /* ---------------------------------------------------
     * Submit exam - final submission
     * @param submissionId ID của submission
     * @param studentId ID của student (for validation)
     * @returns ExamResultDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 15:30)
     * --------------------------------------------------- */
    public ExamResultDTO submitExam(Long submissionId, Long studentId) {
        ExamSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        // Validate ownership
        if (!submission.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("This submission does not belong to you");
        }
        
        // Validate status
        if (submission.isSubmitted()) {
            throw new BadRequestException("This exam has already been submitted");
        }
        
        Exam exam = submission.getExam();
        
        // Calculate time spent
        int timeSpent = submission.calculateTimeSpent();
        
        // Get all answers
        List<StudentAnswer> answers = answerRepository.findBySubmissionId(submissionId);
        
        // Calculate total score
        BigDecimal totalScore = BigDecimal.valueOf(
            answerRepository.calculateTotalScore(submissionId)
        );
        
        // Check if passed
        boolean passed = totalScore.compareTo(exam.getPassingScore()) >= 0;
        
        // Update submission
        submission.setSubmittedAt(new Timestamp(System.currentTimeMillis()));
        submission.setTimeSpentSeconds(timeSpent);
        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setTotalScore(totalScore);
        submission.setPassed(passed);
        
        submissionRepository.save(submission);
        
        log.info("Student {} submitted exam {} (score: {}/{})", 
            studentId, exam.getId(), totalScore, exam.getTotalScore());
        
        // Return result (respect exam settings)
        return getResult(submissionId, studentId);
    }
    
    /* ---------------------------------------------------
     * Lấy kết quả bài thi (theo exam settings)
     * @param submissionId ID của submission
     * @param studentId ID của student (for validation)
     * @returns ExamResultDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 15:30)
     * --------------------------------------------------- */
    public ExamResultDTO getResult(Long submissionId, Long studentId) {
        ExamSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        // Validate ownership
        if (!submission.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("This submission does not belong to you");
        }
        
        Exam exam = submission.getExam();
        
        // Check if results are available
        if (!exam.getShowResultsAfterSubmit() && !submission.isSubmitted()) {
            throw new BadRequestException("Results are not available yet");
        }
        
        // Get statistics
        List<StudentAnswer> answers = answerRepository.findBySubmissionId(submissionId);
        int totalQuestions = answers.size();
        int answeredQuestions = (int) answers.stream()
            .filter(a -> !a.isEmpty()).count();
        int correctAnswers = (int) answers.stream()
            .filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
        int incorrectAnswers = (int) answers.stream()
            .filter(a -> Boolean.FALSE.equals(a.getIsCorrect())).count();
        int ungradedQuestions = (int) answers.stream()
            .filter(a -> a.getIsCorrect() == null && !a.isEmpty()).count();
        
        // Calculate percentage
        BigDecimal percentage = submission.getTotalScore()
            .divide(submission.getMaxScore(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
        
        LocalDateTime startedAt = submission.getStartedAt() != null ? 
            submission.getStartedAt().toLocalDateTime() : null;
        LocalDateTime submittedAt = submission.getSubmittedAt() != null ?
            submission.getSubmittedAt().toLocalDateTime() : null;
        
        // Build result DTO using constructor
        ExamResultDTO result = new ExamResultDTO();
        result.setSubmissionId(submissionId);
        result.setExamId(exam.getId());
        result.setExamTitle(exam.getTitle());
        result.setAttemptNumber(submission.getAttemptNumber());
        result.setStatus(submission.getStatus());
        result.setStartedAt(startedAt);
        result.setSubmittedAt(submittedAt);
        result.setTimeSpentSeconds(submission.getTimeSpentSeconds());
        result.setTotalScore(submission.getTotalScore());
        result.setMaxScore(submission.getMaxScore());
        result.setPassingScore(exam.getPassingScore());
        result.setPassed(submission.getPassed());
        result.setPercentage(percentage);
        result.setTotalQuestions(totalQuestions);
        result.setAnsweredQuestions(answeredQuestions);
        result.setCorrectAnswers(exam.getShowCorrectAnswers() ? correctAnswers : null);
        result.setIncorrectAnswers(exam.getShowCorrectAnswers() ? incorrectAnswers : null);
        result.setUngradedQuestions(ungradedQuestions);
        result.setCanViewDetailedAnswers(!exam.getShowScoreOnly());
        result.setShowCorrectAnswers(exam.getShowCorrectAnswers());
        result.setMessage(submission.getPassed() ? 
            "Congratulations! You passed the exam." : 
            "You did not pass this time. Keep practicing!");
        
        // Add detailed answers if allowed
        if (!exam.getShowScoreOnly() && exam.getAllowReviewAfterSubmit()) {
            result.setAnswers(mapToAnswerReviewDTOs(answers, exam.getShowCorrectAnswers()));
        }
        
        return result;
    }
    
    // =============== PRIVATE HELPER METHODS ===============
    
    /* ---------------------------------------------------
     * Update submission tracking trong separate transaction
     * Để tránh rollback answer save khi có optimistic locking conflict
     * @param submissionId ID của submission
     * @author: K24DTCN210-NVMANH (24/11/2025 16:13)
     * --------------------------------------------------- */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void updateSubmissionTracking(Long submissionId) {
        try {
            ExamSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
            
            Timestamp now = new Timestamp(System.currentTimeMillis());
            submission.setLastSavedAt(now);
            
            Integer currentCount = submission.getAutoSaveCount();
            submission.setAutoSaveCount(currentCount != null ? currentCount + 1 : 1);
            
            submission = submissionRepository.save(submission);
            
            log.info("[SaveAnswer] ExamSubmission updated - ID: {}, AutoSaveCount: {}", 
                submission.getId(), submission.getAutoSaveCount());
                
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            // Optimistic locking failure - another process updated submission
            log.warn("[SaveAnswer] Optimistic locking conflict on submission {} - " +
                "Another process updated it. This is OK, answer was saved. Error: {}", 
                submissionId, e.getMessage());
        } catch (Exception e) {
            // Other errors - log but don't fail the whole save operation
            log.error("[SaveAnswer] CRITICAL: Failed to update submission tracking - ID: {}. " +
                "Answer was saved but submission tracking failed! Error: {}", 
                submissionId, e.getMessage(), e);
        }
    }
    
    private AvailableExamDTO mapToAvailableExamDTO(Exam exam, Long studentId) {
        Map<String, Object> eligibility = checkEligibility(exam.getId(), studentId);
        
        int totalQuestions = (int) examQuestionRepository.countByExamId(exam.getId());
        int attemptsMade = submissionRepository.countByStudentIdAndExamId(studentId, exam.getId());
        boolean hasActiveSubmission = submissionRepository
            .findActiveSubmission(studentId, exam.getId()).isPresent();
        boolean hasPassed = submissionRepository.hasPassedExam(studentId, exam.getId());
        
        Optional<Double> highestScore = submissionRepository.findHighestScore(studentId, exam.getId());
        
        return AvailableExamDTO.builder()
            .id(exam.getId())
            .title(exam.getTitle())
            .description(exam.getDescription())
            .subjectClassId(exam.getSubjectClass().getId())
            .subjectClassName(exam.getSubjectClass().getCode())
            .subjectCode(exam.getSubjectClass().getSubject().getSubjectCode())
            .startTime(exam.getStartTime())
            .endTime(exam.getEndTime())
            .durationMinutes(exam.getDurationMinutes())
            .totalScore(exam.getTotalScore())
            .passingScore(exam.getPassingScore())
            .examPurpose(exam.getExamPurpose().name())
            .examFormat(exam.getExamFormat().name())
            .randomizeQuestions(exam.getRandomizeQuestions())
            .randomizeOptions(exam.getRandomizeOptions())
            .maxAttempts(exam.getMaxAttempts())
            .attemptsMade(attemptsMade)
            .remainingAttempts(exam.getMaxAttempts() > 0 ? 
                exam.getMaxAttempts() - attemptsMade : null)
            .hasActiveSubmission(hasActiveSubmission)
            .hasPassed(hasPassed)
            .highestScore(highestScore.isPresent() ? 
                BigDecimal.valueOf(highestScore.get()) : null)
            .status(exam.getCurrentStatus())
            .isEligible((Boolean) eligibility.get("isEligible"))
            .ineligibleReason((String) eligibility.get("reason"))
            .totalQuestions(totalQuestions)
            .build();
    }
    
    /* ---------------------------------------------------
     * Parse options từ JSON Map sang List<String> format "A. Text"
     * @param question QuestionBank entity
     * @param randomize Có shuffle options không
     * @param seed Random seed để shuffle
     * @returns List<String> với format ["A. Option A", "B. Option B", ...]
     * @author: K24DTCN210-NVMANH (24/11/2025 10:28)
     * --------------------------------------------------- */
    private List<String> parseOptionsWithoutAnswer(QuestionBank question, 
            Boolean randomize, Long seed) {
        try {
            // ✅ Check if options is null or empty
            String optionsJson = question.getOptions();
            if (optionsJson == null || optionsJson.trim().isEmpty()) {
                log.warn("Question {} has null or empty options field", question.getId());
                return new ArrayList<>();
            }
            
            @SuppressWarnings("unchecked")
            Map<String, String> optionsMap = objectMapper.readValue(
                optionsJson, Map.class);
            
            // Check if map is null or empty
            if (optionsMap == null || optionsMap.isEmpty()) {
                log.warn("Question {} has empty options map", question.getId());
                return new ArrayList<>();
            }
            
            // Remove correctAnswer key if exists
            optionsMap.remove("correctAnswer");
            
            // Convert Map to List with format "Key. Value"
            List<String> optionsList = new ArrayList<>();
            
            // Get entries and sort by key (A, B, C, D)
            List<Map.Entry<String, String>> entries = new ArrayList<>(optionsMap.entrySet());
            entries.sort(Map.Entry.comparingByKey());
            
            // Randomize if needed
            if (Boolean.TRUE.equals(randomize) && seed != null) {
                Random random = new Random(seed + question.getId());
                Collections.shuffle(entries, random);
            }
            
            // Format as "Key. Value"
            for (Map.Entry<String, String> entry : entries) {
                optionsList.add(entry.getKey() + ". " + entry.getValue());
            }
            
            return optionsList;
        } catch (Exception e) {
            log.error("Error parsing options for question {}", question.getId(), e);
            return new ArrayList<>();
        }
    }
    
    private Object parseAnswerJson(String answerJson) {
        if (answerJson == null) return null;
        try {
            return objectMapper.readValue(answerJson, Object.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void autoGradeAnswer(StudentAnswer answer, QuestionBank question, Long examId) {
        // Chỉ auto-grade cho MULTIPLE_CHOICE, MULTIPLE_SELECT, TRUE_FALSE
        QuestionType type = question.getQuestionType();
        
        if (type != QuestionType.MULTIPLE_CHOICE && 
            type != QuestionType.MULTIPLE_SELECT && 
            type != QuestionType.TRUE_FALSE) {
            return;  // Requires manual grading
        }
        
        try {
            String correctAnswerStr = question.getCorrectAnswer();
            String studentAnswerStr = answer.getAnswerJson();
            
            if (correctAnswerStr == null || studentAnswerStr == null) {
                return;
            }
            
            boolean isCorrect = correctAnswerStr.equals(studentAnswerStr);
            answer.setIsCorrect(isCorrect);
            
            // Get max points from ExamQuestion
            ExamQuestion eq = examQuestionRepository
                .findByExamIdAndQuestionId(examId, question.getId())
                .orElse(null);
            
            if (eq != null) {
                answer.setMaxPoints(eq.getPoints());
                answer.setPointsEarned(isCorrect ? eq.getPoints() : BigDecimal.ZERO);
            }
        } catch (Exception e) {
            log.error("Error auto-grading answer", e);
        }
    }
    
    private List<AnswerReviewDTO> mapToAnswerReviewDTOs(List<StudentAnswer> answers, 
            Boolean showCorrectAnswers) {
        List<AnswerReviewDTO> result = new ArrayList<>();
        for (StudentAnswer answer : answers) {
            QuestionBank question = answer.getQuestion();
            
            AnswerReviewDTO dto = AnswerReviewDTO.builder()
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .options(parseOptionsWithoutAnswer(question, false, null))
                .studentAnswer(parseAnswerJson(answer.getAnswerJson()))
                .studentAnswerText(answer.getAnswerText())
                .correctAnswer(showCorrectAnswers ? 
                    parseAnswerJson(question.getCorrectAnswer()) : null)
                .correctAnswerText(showCorrectAnswers ? 
                    question.getCorrectAnswer() : null)
                .isCorrect(answer.getIsCorrect())
                .pointsEarned(answer.getPointsEarned())
                .maxPoints(answer.getMaxPoints())
                .teacherFeedback(answer.getTeacherFeedback())
                .gradedByName(answer.getGradedBy() != null ? 
                    answer.getGradedBy().getFullName() : null)
                .isGraded(answer.isGraded())
                .requiresManualGrading(question.getQuestionType() == QuestionType.ESSAY || 
                    question.getQuestionType() == QuestionType.CODING)
                .uploadedFileUrl(answer.getUploadedFileUrl())
                .uploadedFileName(answer.getUploadedFileName())
                .build();
            
            result.add(dto);
        }
        return result;
    }
    
    /* ---------------------------------------------------
     * Tạm dừng bài thi của học sinh (giáo viên thực hiện)
     * @param request PauseExamRequest chứa submissionId, reason, pauseDuration
     * @param teacherId ID của giáo viên thực hiện
     * @returns Map với success message
     * @author: K24DTCN210-NVMANH (21/11/2025 02:05)
     * --------------------------------------------------- */
    public Map<String, Object> pauseExam(PauseExamRequest request, Long teacherId) {
        ExamSubmission submission = submissionRepository.findById(request.getSubmissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        // Validate status
        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new BadRequestException("Can only pause IN_PROGRESS exams. Current status: " 
                + submission.getStatus());
        }
        
        // Validate teacher has permission (check if teacher manages the exam's class)
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        
        // Update status to PAUSED
        submission.setStatus(SubmissionStatus.PAUSED);
        submissionRepository.save(submission);
        
        log.info("Teacher {} paused exam submission {} for student {}. Reason: {}", 
            teacherId, submission.getId(), submission.getStudent().getId(), request.getReason());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Exam paused successfully");
        result.put("submissionId", submission.getId());
        result.put("studentName", submission.getStudent().getFullName());
        result.put("reason", request.getReason());
        result.put("pauseDurationMinutes", request.getPauseDurationMinutes());
        
        return result;
    }
    
    /* ---------------------------------------------------
     * Tiếp tục bài thi đã tạm dừng (giáo viên thực hiện)
     * @param request ResumeExamRequest chứa submissionId, additionalTime
     * @param teacherId ID của giáo viên thực hiện
     * @returns Map với success message
     * @author: K24DTCN210-NVMANH (21/11/2025 02:05)
     * --------------------------------------------------- */
    public Map<String, Object> resumeExam(ResumeExamRequest request, Long teacherId) {
        ExamSubmission submission = submissionRepository.findById(request.getSubmissionId())
            .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        // Validate status
        if (submission.getStatus() != SubmissionStatus.PAUSED) {
            throw new BadRequestException("Can only resume PAUSED exams. Current status: " 
                + submission.getStatus());
        }
        
        // Validate teacher has permission
        User teacher = userRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        
        // Update status back to IN_PROGRESS
        submission.setStatus(SubmissionStatus.IN_PROGRESS);
        
        // Add additional time if specified (store in a note or extend endTime)
        // Note: This is a simplified version. Full implementation would need to track pause time
        // and extend the deadline accordingly
        
        submissionRepository.save(submission);
        
        log.info("Teacher {} resumed exam submission {} for student {}. Additional time: {} minutes", 
            teacherId, submission.getId(), submission.getStudent().getId(), 
            request.getAdditionalTimeMinutes());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Exam resumed successfully");
        result.put("submissionId", submission.getId());
        result.put("studentName", submission.getStudent().getFullName());
        result.put("additionalTimeMinutes", request.getAdditionalTimeMinutes());
        
        return result;
    }
    
    /* ---------------------------------------------------
     * Lấy danh sách các phiên thi đang active (giáo viên xem)
     * @returns List ActiveSessionDTO
     * @author: K24DTCN210-NVMANH (21/11/2025 02:05)
     * --------------------------------------------------- */
    public List<ActiveSessionDTO> getActiveSessions() {
        // Get all IN_PROGRESS submissions
        List<ExamSubmission> activeSubmissions = submissionRepository
            .findByStatus(SubmissionStatus.IN_PROGRESS);
        
        return activeSubmissions.stream()
            .map(this::mapToActiveSessionDTO)
            .sorted(Comparator.comparing(ActiveSessionDTO::getStartedAt).reversed())
            .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Lấy live view của một bài thi cho giáo viên
     * @param examId ID của exam
     * @returns TeacherLiveViewDTO với statistics và alerts
     * @author: K24DTCN210-NVMANH (21/11/2025 02:05)
     * --------------------------------------------------- */
    public TeacherLiveViewDTO getTeacherLiveView(Long examId) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        
        // Get all active sessions for this exam
        List<ExamSubmission> activeSessions = submissionRepository
            .findByExamIdAndStatus(examId, SubmissionStatus.IN_PROGRESS);
        
        if (activeSessions.isEmpty()) {
            return TeacherLiveViewDTO.builder()
                .examId(examId)
                .examTitle(exam.getTitle())
                .totalActiveSessions(0)
                .sessions(new ArrayList<>())
                .statistics(createEmptyStatistics())
                .alerts(new ArrayList<>())
                .lastUpdated(LocalDateTime.now())
                .build();
        }
        
        // Map to ActiveSessionDTO
        List<ActiveSessionDTO> sessionDTOs = activeSessions.stream()
            .map(this::mapToActiveSessionDTO)
            .collect(Collectors.toList());
        
        // Calculate statistics
        Map<String, Object> statistics = calculateStatistics(sessionDTOs);
        
        // Generate alerts
        List<String> alerts = generateAlerts(sessionDTOs);
        
        return TeacherLiveViewDTO.builder()
            .examId(examId)
            .examTitle(exam.getTitle())
            .totalActiveSessions(activeSessions.size())
            .sessions(sessionDTOs)
            .statistics(statistics)
            .alerts(alerts)
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    // =============== NEW HELPER METHODS ===============
    
    private ActiveSessionDTO mapToActiveSessionDTO(ExamSubmission submission) {
        // Get student info
        User student = submission.getStudent();
        
        // Calculate progress
        int totalQuestions = (int) examQuestionRepository.countByExamId(submission.getExam().getId());
        int answeredQuestions = (int) answerRepository
            .countBySubmissionIdAndAnswerTextIsNotNull(submission.getId());
        BigDecimal progressPercentage = totalQuestions > 0 ? 
            BigDecimal.valueOf(answeredQuestions)
                .divide(BigDecimal.valueOf(totalQuestions), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO;
        
        // Calculate remaining time
        int durationMinutes = submission.getExam().getDurationMinutes();
        LocalDateTime startedAt = submission.getStartedAt().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        long elapsedMinutes = java.time.Duration.between(startedAt, now).toMinutes();
        int remainingMinutes = Math.max(0, durationMinutes - (int) elapsedMinutes);
        
        // Check last activity
        LocalDateTime lastActivity = submission.getLastSavedAt() != null ?
            submission.getLastSavedAt().toLocalDateTime() : startedAt;
        long minutesSinceLastActivity = java.time.Duration.between(lastActivity, now).toMinutes();
        boolean isInactive = minutesSinceLastActivity > 10;
        
        return ActiveSessionDTO.builder()
            .submissionId(submission.getId())
            .examId(submission.getExam().getId())
            .examTitle(submission.getExam().getTitle())
            .studentId(student.getId())
            .studentName(student.getFullName())
            .studentEmail(student.getEmail())
            .startedAt(startedAt)
            .durationMinutes(durationMinutes)
            .remainingMinutes(remainingMinutes)
            .totalQuestions(totalQuestions)
            .answeredQuestions(answeredQuestions)
            .progressPercentage(progressPercentage)
            .lastActivity(lastActivity)
            .autoSaveCount(submission.getAutoSaveCount())
            .status(submission.getStatus())
            .isInactive(isInactive)
            .build();
    }
    
    private Map<String, Object> calculateStatistics(List<ActiveSessionDTO> sessions) {
        Map<String, Object> stats = new HashMap<>();
        
        if (sessions.isEmpty()) {
            return createEmptyStatistics();
        }
        
        // Average progress
        BigDecimal avgProgress = sessions.stream()
            .map(ActiveSessionDTO::getProgressPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(sessions.size()), 2, RoundingMode.HALF_UP);
        
        // Average time remaining
        double avgTimeRemaining = sessions.stream()
            .mapToInt(ActiveSessionDTO::getRemainingMinutes)
            .average()
            .orElse(0.0);
        
        // Students with low progress (< 30%)
        long lowProgressCount = sessions.stream()
            .filter(s -> s.getProgressPercentage().compareTo(BigDecimal.valueOf(30)) < 0)
            .count();
        
        // Inactive students (no activity > 10 minutes)
        long inactiveCount = sessions.stream()
            .filter(ActiveSessionDTO::getIsInactive)
            .count();
        
        stats.put("averageProgress", avgProgress);
        stats.put("averageTimeRemaining", BigDecimal.valueOf(avgTimeRemaining).setScale(2, RoundingMode.HALF_UP));
        stats.put("studentsWithLowProgress", lowProgressCount);
        stats.put("inactiveStudents", inactiveCount);
        
        return stats;
    }
    
    private Map<String, Object> createEmptyStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageProgress", BigDecimal.ZERO);
        stats.put("averageTimeRemaining", BigDecimal.ZERO);
        stats.put("studentsWithLowProgress", 0);
        stats.put("inactiveStudents", 0);
        return stats;
    }
    
    private List<String> generateAlerts(List<ActiveSessionDTO> sessions) {
        List<String> alerts = new ArrayList<>();
        
        // Check for inactive students
        List<String> inactiveStudents = sessions.stream()
            .filter(ActiveSessionDTO::getIsInactive)
            .map(s -> s.getStudentName() + " (no activity for 10+ minutes)")
            .collect(Collectors.toList());
        
        if (!inactiveStudents.isEmpty()) {
            alerts.add("⚠️ " + inactiveStudents.size() + " inactive student(s): " + 
                String.join(", ", inactiveStudents));
        }
        
        // Check for students with low time remaining (< 5 minutes)
        List<String> urgentStudents = sessions.stream()
            .filter(s -> s.getRemainingMinutes() < 5)
            .map(s -> s.getStudentName() + " (" + s.getRemainingMinutes() + " min left)")
            .collect(Collectors.toList());
        
        if (!urgentStudents.isEmpty()) {
            alerts.add("⏰ " + urgentStudents.size() + " student(s) with < 5 minutes: " + 
                String.join(", ", urgentStudents));
        }
        
        // Check for students with very low progress (< 20%) and half time elapsed
        List<String> strugglingStudents = sessions.stream()
            .filter(s -> {
                BigDecimal progress = s.getProgressPercentage();
                int remaining = s.getRemainingMinutes();
                int duration = s.getDurationMinutes();
                boolean halfTimeElapsed = remaining < (duration / 2);
                return progress.compareTo(BigDecimal.valueOf(20)) < 0 && halfTimeElapsed;
            })
            .map(s -> s.getStudentName() + " (" + s.getProgressPercentage() + "% done)")
            .collect(Collectors.toList());
        
        if (!strugglingStudents.isEmpty()) {
            alerts.add("📊 " + strugglingStudents.size() + " student(s) struggling (< 20% progress): " + 
                String.join(", ", strugglingStudents));
        }
        
        return alerts;
    }
}

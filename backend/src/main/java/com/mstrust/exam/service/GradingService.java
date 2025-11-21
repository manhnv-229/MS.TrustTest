package com.mstrust.exam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mstrust.exam.dto.*;
import com.mstrust.exam.entity.*;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Service xử lý logic chấm điểm và quản lý grading cho teacher
 * @author: K24DTCN210-NVMANH (20/11/2025 11:20)
 * EditBy: K24DTCN210-NVMANH (20/11/2025 15:48) - Handle data inconsistency
 * --------------------------------------------------- */
@Service
@RequiredArgsConstructor
@Transactional
public class GradingService {

        private final ExamSubmissionRepository submissionRepository;
        private final StudentAnswerRepository answerRepository;
        private final ExamRepository examRepository;
        private final UserRepository userRepository;
        private final ExamQuestionRepository examQuestionRepository;
        private final ObjectMapper objectMapper;

        /*
         * ---------------------------------------------------
         * Lấy danh sách submissions với phân trang và filter
         * 
         * @param examId ID của exam cần lọc (optional)
         * 
         * @param status Trạng thái cần lọc (optional)
         * 
         * @param page Số trang (0-based)
         * 
         * @param size Kích thước trang
         * 
         * @param sortBy Trường cần sort
         * 
         * @param sortOrder Thứ tự sort (asc/desc)
         * 
         * @param teacherId ID của teacher
         * 
         * @returns Page danh sách submissions
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 11:20)
         * ---------------------------------------------------
         */
        public Page<SubmissionListItemDTO> getSubmissions(
                        Long examId, SubmissionStatus status,
                        Integer page, Integer size,
                        String sortBy, String sortOrder,
                        Long teacherId) {

                // Validate teacher permission nếu có examId
                if (examId != null) {
                        checkTeacherPermission(examId, teacherId);
                }

                // Tạo pageable
                Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

                // Query submissions
                Page<ExamSubmission> submissions;
                if (examId != null && status != null) {
                        submissions = submissionRepository.findByExamIdAndStatus(examId, status, pageable);
                } else if (examId != null) {
                        submissions = submissionRepository.findByExamId(examId, pageable);
                } else if (status != null) {
                        submissions = submissionRepository.findByStatus(status, pageable);
                } else {
                        submissions = submissionRepository.findAllWithPagination(pageable);
                }

                // Convert to DTO
                return submissions.map(this::convertToListItemDTO);
        }

        /*
         * ---------------------------------------------------
         * Lấy chi tiết submission để chấm điểm
         * 
         * @param submissionId ID của submission
         * 
         * @param teacherId ID của teacher
         * 
         * @returns Chi tiết submission với tất cả answers
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 11:20)
         * EditBy: K24DTCN210-NVMANH (20/11/2025 15:48) - Skip answers có data
         * inconsistency
         * ---------------------------------------------------
         */
        public SubmissionGradingDetailDTO getSubmissionDetail(Long submissionId, Long teacherId) {
                // Tìm submission
                ExamSubmission submission = submissionRepository.findById(submissionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

                // Check permission
                checkTeacherPermission(submission.getExam().getId(), teacherId);

                // Lấy tất cả answers
                List<StudentAnswer> answers = answerRepository.findBySubmissionId(submissionId);

                // Đếm số câu chưa chấm
                int ungradedCount = answerRepository.countUngradedBySubmissionId(submissionId);

                // Build DTO - Skip answers không tìm thấy ExamQuestion (data inconsistency)
                List<AnswerForGradingDTO> validAnswers = answers.stream()
                                .map(ans -> convertToAnswerForGradingDTOSafe(ans, submission))
                                .filter(dto -> dto != null)
                                .collect(Collectors.toList());

                return SubmissionGradingDetailDTO.builder()
                                .id(submission.getId())
                                .exam(convertToExamSummary(submission.getExam()))
                                .student(convertToStudentInfo(submission.getStudent()))
                                .startedAt(submission.getStartedAt().toLocalDateTime())
                                .submittedAt(submission.getSubmittedAt() != null
                                                ? submission.getSubmittedAt().toLocalDateTime()
                                                : null)
                                .timeSpentSeconds(submission.getTimeSpentSeconds())
                                .status(submission.getStatus())
                                .answers(validAnswers)
                                .currentScore(submission.getTotalScore())
                                .maxScore(submission.getExam().getTotalScore())
                                .passed(submission.getPassed())
                                .ungradedAnswersCount(ungradedCount)
                                .build();
        }

        /*
         * ---------------------------------------------------
         * Chấm điểm cho một câu trả lời
         * 
         * @param request Thông tin chấm điểm
         * 
         * @param teacherId ID của teacher
         * 
         * @returns Kết quả chấm điểm
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 11:20)
         * EditBy: K24DTCN210-NVMANH (20/11/2025 15:27) - Sửa logic tìm ExamQuestion
         * EditBy: K24DTCN210-NVMANH (20/11/2025 16:43) - Handle data inconsistency
         * gracefully
         * ---------------------------------------------------
         */
        public Map<String, Object> gradeAnswer(GradeAnswerRequest request, Long teacherId) {
                // Tìm answer
                StudentAnswer answer = answerRepository.findById(request.getAnswerId())
                                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

                ExamSubmission submission = answer.getSubmission();

                // Check permission
                checkTeacherPermission(submission.getExam().getId(), teacherId);

                // Validate submission status
                if (submission.getStatus() != SubmissionStatus.SUBMITTED
                                && submission.getStatus() != SubmissionStatus.GRADED) {
                        throw new BadRequestException("Submission must be SUBMITTED to grade");
                }

                // Validate points - Get ExamQuestion bằng examId + questionId
                // Handle data inconsistency: answer.questionId không có trong exam_questions
                java.util.Optional<ExamQuestion> examQuestionOpt = examQuestionRepository
                                .findByExamIdAndQuestionId(submission.getExam().getId(), answer.getQuestionId());

                if (examQuestionOpt.isEmpty()) {
                        // Log warning về data inconsistency
                        System.err.println("ERROR: Cannot grade answer - Data inconsistency detected!");
                        System.err.println("  Answer ID: " + answer.getId());
                        System.err.println("  Question ID: " + answer.getQuestionId());
                        System.err.println("  Exam ID: " + submission.getExam().getId());
                        System.err.println("  Issue: Question is not properly added to this exam");
                        System.err.println("  Action needed: Admin must add question " + answer.getQuestionId()
                                        + " to exam " + submission.getExam().getId() + " via exam management");

                        throw new BadRequestException(
                                        "Không thể chấm điểm: Câu hỏi này không thuộc về đề thi. " +
                                                        "Cần Admin/Teacher kiểm tra và thêm câu hỏi ID="
                                                        + answer.getQuestionId() +
                                                        " vào đề thi ID=" + submission.getExam().getId()
                                                        + " trong phần quản lý đề thi.");
                }

                ExamQuestion examQuestion = examQuestionOpt.get();

                BigDecimal maxPoints = examQuestion.getPoints();
                if (request.getPointsEarned().compareTo(BigDecimal.ZERO) < 0) {
                        throw new BadRequestException("Points earned cannot be negative");
                }
                if (request.getPointsEarned().compareTo(maxPoints) > 0) {
                        throw new BadRequestException("Points earned cannot exceed max points: " + maxPoints);
                }

                // Update answer
                answer.setPointsEarned(request.getPointsEarned());
                answer.setIsCorrect(request.getIsCorrect());
                answer.setTeacherFeedback(request.getFeedback());
                answer.setGradedAt(new Timestamp(System.currentTimeMillis()));

                User teacher = userRepository.findById(teacherId)
                                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
                answer.setGradedBy(teacher);

                answerRepository.save(answer);

                // Recalculate total score
                BigDecimal totalScore = BigDecimal.valueOf(
                                answerRepository.calculateTotalScore(submission.getId()));
                submission.setTotalScore(totalScore);

                // Check if passed
                submission.setPassed(
                                totalScore.compareTo(submission.getExam().getPassingScore()) >= 0);

                submissionRepository.save(submission);

                // Return result
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Answer graded successfully");
                result.put("answerId", answer.getId());
                result.put("pointsEarned", answer.getPointsEarned());
                result.put("isCorrect", answer.getIsCorrect());
                result.put("newTotalScore", submission.getTotalScore());
                result.put("passed", submission.getPassed());
                result.put("ungradedAnswers", answerRepository.countUngradedBySubmissionId(submission.getId()));

                return result;
        }

        /*
         * ---------------------------------------------------
         * Hoàn tất quá trình chấm điểm (finalize)
         * 
         * @param submissionId ID của submission
         * 
         * @param teacherId ID của teacher
         * 
         * @returns Kết quả cuối cùng của bài thi
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 11:20)
         * EditBy: K24DTCN210-NVMANH (20/11/2025 15:48) - Skip answers có data
         * inconsistency
         * ---------------------------------------------------
         */
        public ExamResultDTO finalizeGrading(Long submissionId, Long teacherId) {
                // Tìm submission
                System.out.println("=== 1. SEARCH SUBMISSION ===");
                ExamSubmission submission = submissionRepository.findById(submissionId)
                                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

                // Check permission
                System.out.println("=== 2. CHECK permission ===");
                checkTeacherPermission(submission.getExam().getId(), teacherId);

                // Validate status
                System.out.println("=== 3. DEBUG finalizeGrading - Building ExamResultDTO ===");
                if (submission.getStatus() != SubmissionStatus.SUBMITTED
                                && submission.getStatus() != SubmissionStatus.GRADED) {
                        throw new BadRequestException("Submission must be SUBMITTED to finalize");
                }
                System.out.println("=== 4. Check all manual questions are graded");
                // Check all manual questions are graded
                int ungradedCount = answerRepository.countUngradedBySubmissionId(submissionId);
                if (ungradedCount > 0) {
                        throw new BadRequestException(
                                        "Cannot finalize: " + ungradedCount + " answers are not graded yet");
                }

                // Recalculate final score BUT don't set to submission yet
                System.out.println("=== 5. Recalculate final score ===");
                Double scoreDouble = answerRepository.calculateTotalScore(submissionId);

                // Handle null result from calculateTotalScore
                BigDecimal calculatedScore = (scoreDouble != null)
                                ? BigDecimal.valueOf(scoreDouble)
                                : BigDecimal.ZERO;

                // DON'T modify submission yet to keep it clean for query
                // Will update after query to avoid Hibernate flush

                // Query answers FIRST before modifying submission to avoid Hibernate flush issues
                System.out.println("=== 10. findBySubmissionId (BEFORE modifying submission) ===");
                System.out.println("=== submissionId: " + submissionId);
                List<StudentAnswer> answers;
                try {
                        answers = answerRepository.findBySubmissionId(submissionId);
                } catch (Exception e) {
                        System.err.println("=== Exception findBySubmissionId type: "
                                        + e.getClass().getName());
                        System.err.println("=== Exception findBySubmissionId message: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException(                     
                                        "Exception findBySubmissionId",
                                        e);
                }

                // DON'T save yet - will save after ALL DTO conversion is done
                // to avoid Hibernate flush during DTO conversion queries

                System.out.println("=== 11. convertToAnswerReviewDTOSafe ===");
                List<AnswerReviewDTO> validAnswers;
                try {
                        validAnswers = answers.stream()
                                        .map(ans -> {
                                                try {
                                                        System.out.println("=== Processing answer ID: " + ans.getId());
                                                        AnswerReviewDTO dto = convertToAnswerReviewDTOSafe(ans,
                                                                        submission);
                                                        System.out.println("=== Successfully converted answer ID: "
                                                                        + ans.getId());
                                                        return dto;
                                                } catch (Exception e) {
                                                        System.err.println("=== ERROR converting answer ID: "
                                                                        + ans.getId());
                                                        System.err.println("=== Exception type: "
                                                                        + e.getClass().getName());
                                                        System.err.println("=== Exception message: " + e.getMessage());
                                                        e.printStackTrace();
                                                        throw new RuntimeException(
                                                                        "Failed to convert answer ID=" + ans.getId(),
                                                                        e);
                                                }
                                        })
                                        .filter(dto -> dto != null)
                                        .collect(Collectors.toList());
                } catch (Exception e) {
                        System.err.println("=== FATAL ERROR in stream processing ===");
                        System.err.println("=== Exception type: " + e.getClass().getName());
                        System.err.println("=== Exception message: " + e.getMessage());
                        System.err.println("=== Full stack trace:");
                        e.printStackTrace();
                        throw new RuntimeException("Failed to process answers for submission " + submissionId, e);
                }

                // === DEFENSIVE NULL HANDLING - Log all values before building DTO ===
                System.out.println("=== DEBUG finalizeGrading - Building ExamResultDTO ===");
                System.out.println("submission.getId(): " + submission.getId());
                System.out.println("submission.getExam().getId(): " + submission.getExam().getId());
                System.out.println("submission.getExam().getTitle(): " + submission.getExam().getTitle());
                System.out.println("submission.getAttemptNumber(): " + submission.getAttemptNumber());
                System.out.println("submission.getStatus(): " + submission.getStatus());
                System.out.println("submission.getStartedAt(): " + submission.getStartedAt());
                System.out.println("submission.getSubmittedAt(): " + submission.getSubmittedAt());
                System.out.println("submission.getTimeSpentSeconds(): " + submission.getTimeSpentSeconds());
                System.out.println("submission.getTotalScore(): " + submission.getTotalScore());
                System.out.println("submission.getExam().getTotalScore(): " + submission.getExam().getTotalScore());
                System.out.println("submission.getExam().getPassingScore(): " + submission.getExam().getPassingScore());
                System.out.println("submission.getPassed(): " + submission.getPassed());
                System.out.println(
                                "submission.getExam().getShowCorrectAnswers(): "
                                                + submission.getExam().getShowCorrectAnswers());
                System.out.println("validAnswers.size(): " + validAnswers.size());

                // Handle null totalScore for DTO
                BigDecimal finalScore = submission.getTotalScore() != null
                                ? submission.getTotalScore()
                                : BigDecimal.ZERO;

                // Calculate percentage safely
                BigDecimal percentage = BigDecimal.ZERO;
                if (submission.getExam().getTotalScore() != null
                                && submission.getExam().getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
                        percentage = finalScore
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(submission.getExam().getTotalScore(), 2, BigDecimal.ROUND_HALF_UP);
                }

                // Calculate statistics - with safe defaults
                int correctCount = answerRepository.countCorrectAnswers(submissionId);
                int totalCount = validAnswers != null ? validAnswers.size() : 0;
                int incorrectCount = totalCount - correctCount;

                // === BUILD DTO WITH COMPREHENSIVE NULL SAFETY ===
                // Convert all primitives explicitly to prevent any null issues
                Integer attemptNumber = submission.getAttemptNumber() != null ? submission.getAttemptNumber() : 1;
                Integer timeSpentSeconds = submission.getTimeSpentSeconds() != null ? submission.getTimeSpentSeconds()
                                : 0;
                Boolean passed = submission.getPassed() != null ? submission.getPassed() : Boolean.FALSE;
                Boolean showCorrectAnswers = submission.getExam().getShowCorrectAnswers() != null
                                ? submission.getExam().getShowCorrectAnswers()
                                : Boolean.FALSE;

                LocalDateTime startedAtLocal = submission.getStartedAt() != null
                                ? submission.getStartedAt().toLocalDateTime()
                                : null;
                LocalDateTime submittedAtLocal = submission.getSubmittedAt() != null
                                ? submission.getSubmittedAt().toLocalDateTime()
                                : null;

                // Create Integer objects directly from primitives (NOT using Integer.valueOf
                // with potentially null values)
                Integer totalQuestionsObj = totalCount;
                Integer answeredQuestionsObj = totalCount;
                Integer correctAnswersObj = correctCount;
                Integer incorrectAnswersObj = incorrectCount;
                Integer ungradedQuestionsObj = 0;

                // NOW update and save submission AFTER all DTO conversion is complete
                submission.setTotalScore(calculatedScore);
                submission.setPassed(
                                calculatedScore.compareTo(submission.getExam().getPassingScore()) >= 0);
                submission.setStatus(SubmissionStatus.GRADED);
                submissionRepository.save(submission);

                // Create DTO using setters to avoid Lombok builder null issues
                ExamResultDTO result = new ExamResultDTO();
                result.setSubmissionId(submission.getId());
                result.setExamId(submission.getExam().getId());
                result.setExamTitle(submission.getExam().getTitle());
                result.setAttemptNumber(attemptNumber);
                result.setStatus(submission.getStatus());
                result.setStartedAt(startedAtLocal);
                result.setSubmittedAt(submittedAtLocal);
                result.setTimeSpentSeconds(timeSpentSeconds);
                result.setTotalScore(finalScore);
                result.setMaxScore(submission.getExam().getTotalScore());
                result.setPassingScore(submission.getExam().getPassingScore());
                result.setPassed(passed);
                result.setPercentage(percentage);
                result.setTotalQuestions(totalQuestionsObj);
                result.setAnsweredQuestions(answeredQuestionsObj);
                result.setCorrectAnswers(correctAnswersObj);
                result.setIncorrectAnswers(incorrectAnswersObj);
                result.setUngradedQuestions(ungradedQuestionsObj);
                result.setAnswers(validAnswers);
                result.setCanViewDetailedAnswers(Boolean.TRUE);
                result.setShowCorrectAnswers(showCorrectAnswers);
                result.setMessage(generateResultMessage(passed));

                System.out.println("=== ExamResultDTO created successfully with setters! ===");
                return result;
        }

        /*
         * ---------------------------------------------------
         * Kiểm tra quyền của teacher để chấm điểm
         * 
         * @param examId ID của exam
         * 
         * @param teacherId ID của teacher
         * 
         * @throws BadRequestException nếu không có quyền
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 11:20)
         * ---------------------------------------------------
         */
        private void checkTeacherPermission(Long examId, Long teacherId) {
                Exam exam = examRepository.findById(examId)
                                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

                User teacher = userRepository.findById(teacherId)
                                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

                // Check if teacher created this exam OR is DEPT_MANAGER/ADMIN
                boolean hasPermission = exam.getCreatedBy().equals(teacherId) ||
                                teacher.getRoles().stream()
                                                .anyMatch(r -> r.getRoleName().equals("DEPT_MANAGER") ||
                                                                r.getRoleName().equals("ADMIN"));

                if (!hasPermission) {
                        throw new BadRequestException("You don't have permission to grade this exam");
                }
        }

        /*
         * ---------------------------------------------------
         * Convert ExamSubmission sang SubmissionListItemDTO
         * 
         * @param submission ExamSubmission entity
         * 
         * @returns SubmissionListItemDTO
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 11:20)
         * ---------------------------------------------------
         */
        private SubmissionListItemDTO convertToListItemDTO(ExamSubmission submission) {
                int ungradedCount = answerRepository.countUngradedBySubmissionId(submission.getId());

                BigDecimal percentage = BigDecimal.ZERO;
                if (submission.getTotalScore() != null && submission.getExam().getTotalScore() != null
                                && submission.getExam().getTotalScore().compareTo(BigDecimal.ZERO) > 0) {
                        percentage = submission.getTotalScore()
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(submission.getExam().getTotalScore(), 2, BigDecimal.ROUND_HALF_UP);
                }

                // Default totalScore to 0 if null (for new ungraded submissions)
                BigDecimal totalScore = submission.getTotalScore() != null
                                ? submission.getTotalScore()
                                : BigDecimal.ZERO;

                return SubmissionListItemDTO.builder()
                                .id(submission.getId())
                                .examId(submission.getExam().getId())
                                .examTitle(submission.getExam().getTitle())
                                .studentId(submission.getStudent().getId())
                                .studentName(submission.getStudent().getFullName())
                                .studentCode(submission.getStudent().getStudentCode())
                                .submittedAt(submission.getSubmittedAt() != null
                                                ? submission.getSubmittedAt().toLocalDateTime()
                                                : null)
                                .totalScore(totalScore)
                                .maxScore(submission.getExam().getTotalScore())
                                .percentage(percentage)
                                .passed(submission.getPassed())
                                .status(submission.getStatus())
                                .ungradedAnswers(ungradedCount)
                                .timeSpentSeconds(submission.getTimeSpentSeconds())
                                .build();
        }

        /*
         * ---------------------------------------------------
         * Convert StudentAnswer sang AnswerForGradingDTO (Safe version)
         * 
         * @param answer StudentAnswer entity
         * 
         * @param submission ExamSubmission entity
         * 
         * @returns AnswerForGradingDTO hoặc null nếu data inconsistency
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 15:48)
         * ---------------------------------------------------
         */
        private AnswerForGradingDTO convertToAnswerForGradingDTOSafe(StudentAnswer answer, ExamSubmission submission) {
                // Lấy ExamQuestion - Return null nếu không tìm thấy (data inconsistency)
                ExamQuestion examQuestion = examQuestionRepository
                                .findByExamIdAndQuestionId(submission.getExam().getId(), answer.getQuestionId())
                                .orElse(null);

                if (examQuestion == null) {
                        System.err.println("WARNING: Data inconsistency - Answer ID=" + answer.getId() +
                                        " references question_id=" + answer.getQuestionId() +
                                        " which is not in exam_id=" + submission.getExam().getId());
                        return null;
                }

                QuestionBank questionBank = examQuestion.getQuestion();

                // === USE SETTERS INSTEAD OF LOMBOK BUILDER ===
                Boolean isCorrectValue = answer.getIsCorrect() != null ? answer.getIsCorrect() : Boolean.FALSE;
                Boolean isGradedValue = answer.getIsCorrect() != null ? Boolean.TRUE : Boolean.FALSE;

                AnswerForGradingDTO dto = new AnswerForGradingDTO();
                dto.setId(answer.getId());
                dto.setQuestionId(examQuestion.getId());
                dto.setQuestionText(questionBank.getQuestionText());
                dto.setQuestionType(questionBank.getQuestionType());
                dto.setStudentAnswer(parseJson(answer.getAnswerJson()));
                dto.setStudentAnswerText(answer.getAnswerText());
                dto.setUploadedFileUrl(answer.getUploadedFileUrl());
                dto.setCorrectAnswer(parseJson(questionBank.getCorrectAnswer()));
                dto.setMaxPoints(examQuestion.getPoints());
                dto.setPointsEarned(answer.getPointsEarned());
                dto.setIsCorrect(isCorrectValue);
                dto.setTeacherFeedback(answer.getTeacherFeedback());
                dto.setIsGraded(isGradedValue);
                dto.setGradedAt(answer.getGradedAt() != null ? answer.getGradedAt().toLocalDateTime() : null);
                dto.setGradedByName(answer.getGradedBy() != null ? answer.getGradedBy().getFullName() : null);

                return dto;
        }

        /*
         * ---------------------------------------------------
         * Convert StudentAnswer sang AnswerReviewDTO (Safe version)
         * 
         * @param answer StudentAnswer entity
         * 
         * @param submission ExamSubmission entity
         * 
         * @returns AnswerReviewDTO hoặc null nếu data inconsistency
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 15:48)
         * EditBy: K24DTCN210-NVMANH (21/11/2025 00:03) - Use setters instead of Lombok
         * builder to avoid null issues
         * ---------------------------------------------------
         */
        private AnswerReviewDTO convertToAnswerReviewDTOSafe(StudentAnswer answer, ExamSubmission submission) {
                System.out.println(
                                "=== 11.1. Get ExamQuestion for answer ID=" + answer.getId() + " questionId="
                                                + answer.getQuestionId());

                // Lấy ExamQuestion - Return null nếu không tìm thấy (data inconsistency)
                ExamQuestion examQuestion = examQuestionRepository
                                .findByExamIdAndQuestionId(submission.getExam().getId(), answer.getQuestionId())
                                .orElse(null);

                if (examQuestion == null) {
                        System.err.println("WARNING: Data inconsistency - Answer ID=" + answer.getId() +
                                        " references question_id=" + answer.getQuestionId() +
                                        " which is not in exam_id=" + submission.getExam().getId());
                        return null;
                }

                System.out.println("=== 11.2. Get QuestionBank");
                QuestionBank questionBank = examQuestion.getQuestion();

                System.out.println("=== 11.3. Calculate values");
                // Calculate boolean values explicitly
                boolean requiresManualGrading = questionBank.getQuestionType() == QuestionType.ESSAY
                                || questionBank.getQuestionType() == QuestionType.CODING;

                Boolean isCorrectValue = answer.getIsCorrect() != null ? answer.getIsCorrect() : Boolean.FALSE;
                Boolean isGradedValue = answer.getIsCorrect() != null ? Boolean.TRUE : Boolean.FALSE;

                System.out.println("=== 11.4. Create DTO with setters (NO LOMBOK BUILDER)");
                // === USE SETTERS INSTEAD OF LOMBOK BUILDER ===
                AnswerReviewDTO dto = new AnswerReviewDTO();
                dto.setQuestionId(examQuestion.getId());
                dto.setQuestionText(questionBank.getQuestionText());
                dto.setQuestionType(questionBank.getQuestionType());
                dto.setStudentAnswer(parseJson(answer.getAnswerJson()));
                dto.setCorrectAnswer(parseJson(questionBank.getCorrectAnswer()));
                dto.setIsCorrect(isCorrectValue);
                dto.setPointsEarned(answer.getPointsEarned());
                dto.setMaxPoints(examQuestion.getPoints());
                dto.setTeacherFeedback(answer.getTeacherFeedback());
                dto.setIsGraded(isGradedValue);
                dto.setRequiresManualGrading(requiresManualGrading);

                System.out.println("=== 11.5. AnswerReviewDTO created successfully!");
                return dto;
        }

        /*
         * ---------------------------------------------------
         * Convert Exam sang ExamSummaryDTO (simplified for grading)
         * 
         * @param exam Exam entity
         * 
         * @returns ExamSummaryDTO
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 11:23)
         * ---------------------------------------------------
         */
        private ExamSummaryDTO convertToExamSummary(Exam exam) {
                int questionCount = (int) examQuestionRepository.countByExamId(exam.getId());

                return ExamSummaryDTO.builder()
                                .id(exam.getId())
                                .title(exam.getTitle())
                                .durationMinutes(exam.getDurationMinutes())
                                .questionCount(questionCount)
                                .build();
        }

        /*
         * ---------------------------------------------------
         * Convert User sang StudentInfoDTO
         * 
         * @param user User entity
         * 
         * @returns StudentInfoDTO
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 11:20)
         * ---------------------------------------------------
         */
        private StudentInfoDTO convertToStudentInfo(User user) {
                return StudentInfoDTO.builder()
                                .id(user.getId())
                                .studentCode(user.getStudentCode())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .build();
        }

        /*
         * ---------------------------------------------------
         * Parse JSON string sang Object
         * 
         * @param json JSON string
         * 
         * @returns Object hoặc null nếu parse lỗi
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 11:20)
         * ---------------------------------------------------
         */
        private Object parseJson(String json) {
                if (json == null || json.trim().isEmpty()) {
                        return null;
                }
                try {
                        return objectMapper.readValue(json, Object.class);
                } catch (Exception e) {
                        return json;
                }
        }

        /*
         * ---------------------------------------------------
         * Tạo message kết quả dựa trên passed status
         * 
         * @param passed Boolean - Có đạt điểm đỗ không
         * 
         * @returns String message
         * 
         * @author: K24DTCN210-NVMANH (20/11/2025 22:32)
         * ---------------------------------------------------
         */
        private String generateResultMessage(Boolean passed) {
                if (passed == null) {
                        return "Kết quả đang được xử lý";
                }
                return passed ? "Chúc mừng! Bạn đã đạt điểm đỗ." : "Rất tiếc, bạn chưa đạt điểm đỗ.";
        }
}

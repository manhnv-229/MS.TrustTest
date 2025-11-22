package com.mstrust.exam.service;

import com.mstrust.exam.dto.exam.ExamDTO;
import com.mstrust.exam.dto.grading.*;
import com.mstrust.exam.dto.user.UserDTO;
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
import java.util.*;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Service xử lý nghiệp vụ chấm điểm bài thi
 * Bao gồm: lấy danh sách bài chấm, chấm từng câu, hoàn tất chấm điểm
 * @author: K24DTCN210-NVMANH (21/11/2025 14:12)
 * EditBy: K24DTCN210-NVMANH (21/11/2025 14:12) - Rewrite to match actual entities
 * --------------------------------------------------- */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GradingService {
    
    private final ExamSubmissionRepository submissionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final ExamRepository examRepository;
    private final QuestionBankRepository questionBankRepository;
    private final UserRepository userRepository;
    
    /* ---------------------------------------------------
     * Lấy danh sách bài nộp cần chấm cho giáo viên
     * @param teacherId ID của giáo viên
     * @param status Trạng thái bài nộp (SUBMITTED, GRADED)
     * @param examId ID đề thi (tùy chọn)
     * @returns Danh sách bài nộp cần chấm
     * @author: K24DTCN210-NVMANH (21/11/2025 14:12)
     * --------------------------------------------------- */
    public List<GradingSubmissionListDTO> getSubmissionsForGrading(
            Long teacherId, SubmissionStatus status, Long examId) {
        
        log.info("Getting submissions for grading - teacherId: {}, status: {}, examId: {}", 
                teacherId, status, examId);
        
        List<ExamSubmission> submissions;
        
        if (examId != null) {
            Exam exam = examRepository.findById(examId)
                    .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
            
            if (!exam.getSubjectClass().getTeacher().getId().equals(teacherId)) {
                throw new BadRequestException("You can only grade submissions from your own classes");
            }
            
            if (status != null) {
                submissions = submissionRepository.findByExamIdAndStatus(examId, status);
            } else {
                submissions = submissionRepository.findByExamId(examId);
            }
        } else {
            if (status != null) {
                submissions = submissionRepository.findByStatusAndTeacherId(status, teacherId);
            } else {
                submissions = submissionRepository.findByTeacherId(teacherId);
            }
        }
        
        submissions = submissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.SUBMITTED 
                        || s.getStatus() == SubmissionStatus.GRADED)
                .collect(Collectors.toList());
        
        return submissions.stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }
    
    /* ---------------------------------------------------
     * Lấy chi tiết bài nộp để chấm điểm
     * @param submissionId ID của bài nộp
     * @param teacherId ID của giáo viên
     * @returns Chi tiết bài nộp với tất cả câu trả lời
     * @author: K24DTCN210-NVMANH (21/11/2025 14:12)
     * --------------------------------------------------- */
    public GradingDetailDTO getSubmissionDetail(Long submissionId, Long teacherId) {
        log.info("Getting submission detail for grading - submissionId: {}, teacherId: {}", 
                submissionId, teacherId);
        
        ExamSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        validateTeacherPermission(submission, teacherId);
        
        List<StudentAnswer> answers = studentAnswerRepository.findBySubmissionId(submissionId);
        
        return convertToDetailDTO(submission, answers);
    }
    
    /* ---------------------------------------------------
     * Chấm điểm một câu trả lời
     * @param answerId ID của câu trả lời
     * @param request Request chứa điểm và feedback
     * @param teacherId ID của giáo viên
     * @returns StudentAnswer đã được cập nhật
     * @author: K24DTCN210-NVMANH (21/11/2025 14:12)
     * --------------------------------------------------- */
    public StudentAnswer gradeAnswer(Long answerId, GradeAnswerRequest request, Long teacherId) {
        log.info("Grading answer - answerId: {}, score: {}, teacherId: {}", 
                answerId, request.getScore(), teacherId);
        
        StudentAnswer answer = studentAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));
        
        validateTeacherPermission(answer.getSubmission(), teacherId);
        
        if (answer.getSubmission().getStatus() == SubmissionStatus.GRADED) {
            throw new BadRequestException("Cannot grade already finalized submission");
        }
        
        BigDecimal maxPoints = answer.getMaxPoints();
        BigDecimal newScore = BigDecimal.valueOf(request.getScore());
        
        if (newScore.compareTo(maxPoints) > 0) {
            throw new BadRequestException(
                    String.format("Score %.2f exceeds maximum score %.2f", 
                            request.getScore(), maxPoints.doubleValue()));
        }
        
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        
        answer.setPointsEarned(newScore.setScale(2, RoundingMode.HALF_UP));
        answer.setTeacherFeedback(request.getFeedback());
        answer.setIsCorrect(newScore.compareTo(maxPoints) == 0);
        answer.setGradedBy(teacher);
        answer.setGradedAt(new Timestamp(System.currentTimeMillis()));
        
        studentAnswerRepository.save(answer);
        
        log.info("Answer graded successfully - answerId: {}, score: {}/{}", 
                answerId, newScore, maxPoints);
        
        return answer;
    }
    
    /* ---------------------------------------------------
     * Hoàn tất việc chấm điểm - tính tổng điểm và chuyển status sang GRADED
     * @param submissionId ID của bài nộp
     * @param request Request chứa nhận xét chung
     * @param teacherId ID của giáo viên
     * @returns ExamSubmission đã hoàn tất chấm điểm
     * @author: K24DTCN210-NVMANH (21/11/2025 14:12)
     * --------------------------------------------------- */
    public ExamSubmission finalizeGrading(Long submissionId, FinalizeGradingRequest request, Long teacherId) {
        log.info("Finalizing grading - submissionId: {}, teacherId: {}", submissionId, teacherId);
        
        ExamSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        validateTeacherPermission(submission, teacherId);
        
        if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new BadRequestException("Submission already graded");
        }
        
        if (submission.getStatus() != SubmissionStatus.SUBMITTED) {
            throw new BadRequestException("Can only finalize submitted submissions");
        }
        
        validateAllQuestionsGraded(submissionId);
        
        calculateTotalScore(submission);
        
        submission.setStatus(SubmissionStatus.GRADED);
        
        Exam exam = submission.getExam();
        BigDecimal passingScore = exam.getPassingScore() != null ? exam.getPassingScore() : BigDecimal.ZERO;
        submission.setPassed(submission.getTotalScore().compareTo(passingScore) >= 0);
        
        submissionRepository.save(submission);
        
        log.info("Grading finalized - submissionId: {}, totalScore: {}/{}", 
                submissionId, submission.getTotalScore(), submission.getMaxScore());
        
        return submission;
    }
    
    /* ---------------------------------------------------
     * Lấy thống kê chấm điểm cho một đề thi
     * @param examId ID của đề thi
     * @param teacherId ID của giáo viên
     * @returns Map chứa các thống kê
     * @author: K24DTCN210-NVMANH (21/11/2025 14:12)
     * --------------------------------------------------- */
    public Map<String, Object> getGradingStats(Long examId, Long teacherId) {
        log.info("Getting grading stats - examId: {}, teacherId: {}", examId, teacherId);
        
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));
        
        if (!exam.getSubjectClass().getTeacher().getId().equals(teacherId)) {
            throw new BadRequestException("You can only view stats for your own exams");
        }
        
        List<ExamSubmission> allSubmissions = submissionRepository.findByExamId(examId);
        List<ExamSubmission> gradedSubmissions = allSubmissions.stream()
                .filter(s -> s.getStatus() == SubmissionStatus.GRADED)
                .collect(Collectors.toList());
        
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalSubmissions", allSubmissions.size());
        stats.put("graded", gradedSubmissions.size());
        stats.put("pending", allSubmissions.size() - gradedSubmissions.size());
        
        if (!gradedSubmissions.isEmpty()) {
            List<BigDecimal> scores = gradedSubmissions.stream()
                    .map(ExamSubmission::getTotalScore)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (!scores.isEmpty()) {
                double avgScore = scores.stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .average().orElse(0.0);
                double maxScore = scores.stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .max().orElse(0.0);
                double minScore = scores.stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .min().orElse(0.0);
                
                stats.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
                stats.put("highestScore", maxScore);
                stats.put("lowestScore", minScore);
                
                BigDecimal passingScore = exam.getPassingScore() != null ? 
                        exam.getPassingScore() : BigDecimal.ZERO;
                long passedCount = scores.stream()
                        .filter(s -> s.compareTo(passingScore) >= 0)
                        .count();
                double passRate = (passedCount * 100.0) / scores.size();
                
                stats.put("passRate", Math.round(passRate * 100.0) / 100.0);
                stats.put("passedCount", passedCount);
                stats.put("failedCount", scores.size() - passedCount);
            }
        } else {
            stats.put("averageScore", 0.0);
            stats.put("highestScore", 0.0);
            stats.put("lowestScore", 0.0);
            stats.put("passRate", 0.0);
            stats.put("passedCount", 0);
            stats.put("failedCount", 0);
        }
        
        return stats;
    }
    
    /* ---------------------------------------------------
     * Lấy kết quả bài thi cho học sinh xem
     * @param submissionId ID của bài nộp
     * @param studentId ID của học sinh
     * @returns Kết quả bài thi với điểm số và chi tiết câu trả lời
     * @author: K24DTCN210-NVMANH (21/11/2025 14:12)
     * --------------------------------------------------- */
    public StudentResultDTO getStudentResult(Long submissionId, Long studentId) {
        log.info("Getting student result - submissionId: {}, studentId: {}", submissionId, studentId);
        
        ExamSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        
        if (!submission.getStudent().getId().equals(studentId)) {
            throw new BadRequestException("You can only view your own results");
        }
        
        if (submission.getStatus() != SubmissionStatus.GRADED) {
            throw new BadRequestException("Results are not available yet. Your submission is being graded.");
        }
        
        Exam exam = submission.getExam();
        List<StudentAnswer> answers = studentAnswerRepository.findBySubmissionId(submissionId);
        
        boolean canViewAnswers = exam.getShowCorrectAnswers() != null && exam.getShowCorrectAnswers();
        
        List<AnswerResultDTO> answerResults = null;
        if (canViewAnswers) {
            answerResults = answers.stream()
                    .map(this::convertToAnswerResultDTO)
                    .collect(Collectors.toList());
        }
        
        Double percentage = null;
        Boolean passed = null;
        if (submission.getMaxScore() != null && submission.getMaxScore().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pct = submission.getTotalScore()
                    .divide(submission.getMaxScore(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100.0));
            percentage = pct.setScale(2, RoundingMode.HALF_UP).doubleValue();
            
            BigDecimal passingScore = exam.getPassingScore() != null ? 
                    exam.getPassingScore() : BigDecimal.ZERO;
            passed = submission.getTotalScore().compareTo(passingScore) >= 0;
        }
        
        return StudentResultDTO.builder()
                .submissionId(submission.getId())
                .examTitle(exam.getTitle())
                .startTime(submission.getStartedAt() != null ? submission.getStartedAt().toLocalDateTime() : null)
                .submitTime(submission.getSubmittedAt() != null ? submission.getSubmittedAt().toLocalDateTime() : null)
                .status(submission.getStatus())
                .totalScore(submission.getTotalScore().doubleValue())
                .maxScore(submission.getMaxScore().doubleValue())
                .percentage(percentage)
                .passed(passed)
                .passingScore(exam.getPassingScore() != null ? exam.getPassingScore().doubleValue() : null)
                .generalFeedback(null)
                .answers(answerResults)
                .canViewAnswers(canViewAnswers)
                .build();
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    private void validateTeacherPermission(ExamSubmission submission, Long teacherId) {
        Exam exam = submission.getExam();
        SubjectClass subjectClass = exam.getSubjectClass();
        
        if (!subjectClass.getTeacher().getId().equals(teacherId)) {
            throw new BadRequestException("You can only grade submissions from your own classes");
        }
    }
    
    private void validateAllQuestionsGraded(Long submissionId) {
        List<StudentAnswer> answers = studentAnswerRepository.findBySubmissionId(submissionId);
        
        List<StudentAnswer> ungradedManualAnswers = answers.stream()
                .filter(a -> {
                    QuestionType type = a.getQuestion().getQuestionType();
                    return requiresManualGrading(type) && a.getPointsEarned() == null;
                })
                .collect(Collectors.toList());
        
        if (!ungradedManualAnswers.isEmpty()) {
            throw new BadRequestException(
                    String.format("Cannot finalize: %d manual questions still need grading", 
                            ungradedManualAnswers.size()));
        }
    }
    
    private boolean requiresManualGrading(QuestionType type) {
        return type == QuestionType.ESSAY 
                || type == QuestionType.SHORT_ANSWER 
                || type == QuestionType.CODING;
    }
    
    private void calculateTotalScore(ExamSubmission submission) {
        List<StudentAnswer> answers = studentAnswerRepository.findBySubmissionId(submission.getId());
        
        BigDecimal totalScore = answers.stream()
                .map(a -> a.getPointsEarned() != null ? a.getPointsEarned() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        submission.setTotalScore(totalScore.setScale(2, RoundingMode.HALF_UP));
    }
    
    private GradingSubmissionListDTO convertToListDTO(ExamSubmission submission) {
        List<StudentAnswer> answers = studentAnswerRepository.findBySubmissionId(submission.getId());
        
        long pendingManual = answers.stream()
                .filter(a -> {
                    QuestionType type = a.getQuestion().getQuestionType();
                    return requiresManualGrading(type) && a.getPointsEarned() == null;
                })
                .count();
        
        BigDecimal autoGradedScore = answers.stream()
                .filter(a -> {
                    QuestionType type = a.getQuestion().getQuestionType();
                    return !requiresManualGrading(type);
                })
                .map(a -> a.getPointsEarned() != null ? a.getPointsEarned() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long gradedCount = answers.stream()
                .filter(a -> a.getPointsEarned() != null)
                .count();
        double progress = answers.isEmpty() ? 0 : (gradedCount * 100.0) / answers.size();
        
        User student = submission.getStudent();
        
        return GradingSubmissionListDTO.builder()
                .id(submission.getId())
                .examId(submission.getExam().getId())
                .examTitle(submission.getExam().getTitle())
                .studentId(student.getId())
                .studentName(student.getFullName())
                .studentCode(student.getStudentCode())
                .submitTime(submission.getSubmittedAt() != null ? submission.getSubmittedAt().toLocalDateTime() : null)
                .status(submission.getStatus())
                .pendingManualQuestions((int) pendingManual)
                .autoGradedScore(autoGradedScore.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .maxScore(submission.getMaxScore() != null ? submission.getMaxScore().doubleValue() : null)
                .gradingProgress(Math.round(progress * 100.0) / 100.0)
                .build();
    }
    
    private GradingDetailDTO convertToDetailDTO(ExamSubmission submission, List<StudentAnswer> answers) {
        Exam exam = submission.getExam();
        User student = submission.getStudent();
        
        List<AnswerForGradingDTO> answerDTOs = answers.stream()
                .map(this::convertToAnswerForGradingDTO)
                .collect(Collectors.toList());
        
        BigDecimal currentScore = answers.stream()
                .map(a -> a.getPointsEarned() != null ? a.getPointsEarned() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long gradedCount = answers.stream()
                .filter(a -> a.getPointsEarned() != null)
                .count();
        
        return GradingDetailDTO.builder()
                .submissionId(submission.getId())
                .exam(convertToExamDTO(exam))
                .student(convertToUserDTO(student))
                .startTime(submission.getStartedAt() != null ? submission.getStartedAt().toLocalDateTime() : null)
                .submitTime(submission.getSubmittedAt() != null ? submission.getSubmittedAt().toLocalDateTime() : null)
                .answers(answerDTOs)
                .currentScore(currentScore.setScale(2, RoundingMode.HALF_UP).doubleValue())
                .maxScore(submission.getMaxScore() != null ? submission.getMaxScore().doubleValue() : null)
                .gradedQuestions((int) gradedCount)
                .totalQuestions(answers.size())
                .generalFeedback(null)
                .build();
    }
    
    private AnswerForGradingDTO convertToAnswerForGradingDTO(StudentAnswer answer) {
        QuestionBank question = answer.getQuestion();
        QuestionType type = question.getQuestionType();
        boolean isAutoGraded = !requiresManualGrading(type);
        
        String studentAnswerText = answer.getAnswerText() != null ? 
                answer.getAnswerText() : answer.getAnswerJson();
        
        return AnswerForGradingDTO.builder()
                .answerId(answer.getId())
                .questionId(question.getId())
                .questionText(question.getQuestionText())
                .questionType(type)
                .studentAnswer(studentAnswerText)
                .correctAnswer(question.getCorrectAnswer())
                .currentScore(answer.getPointsEarned() != null ? answer.getPointsEarned().doubleValue() : null)
                .maxScore(answer.getMaxPoints() != null ? answer.getMaxPoints().doubleValue() : null)
                .isAutoGraded(isAutoGraded)
                .feedback(answer.getTeacherFeedback())
                .isCorrect(answer.getIsCorrect())
                .build();
    }
    
    private AnswerResultDTO convertToAnswerResultDTO(StudentAnswer answer) {
        QuestionBank question = answer.getQuestion();
        
        String studentAnswerText = answer.getAnswerText() != null ? 
                answer.getAnswerText() : answer.getAnswerJson();
        
        return AnswerResultDTO.builder()
                .questionNumber(null)
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType())
                .studentAnswer(studentAnswerText)
                .correctAnswer(question.getCorrectAnswer())
                .score(answer.getPointsEarned() != null ? answer.getPointsEarned().doubleValue() : null)
                .maxScore(answer.getMaxPoints() != null ? answer.getMaxPoints().doubleValue() : null)
                .isCorrect(answer.getIsCorrect())
                .feedback(answer.getTeacherFeedback())
                .build();
    }
    
    private ExamDTO convertToExamDTO(Exam exam) {
        return ExamDTO.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .duration(exam.getDurationMinutes())
                .passingScore(exam.getPassingScore() != null ? exam.getPassingScore().doubleValue() : null)
                .build();
    }
    
    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .studentCode(user.getStudentCode())
                .build();
    }
}

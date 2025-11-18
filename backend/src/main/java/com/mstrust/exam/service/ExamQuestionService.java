package com.mstrust.exam.service;

import com.mstrust.exam.dto.AddQuestionToExamRequest;
import com.mstrust.exam.dto.ExamQuestionDTO;
import com.mstrust.exam.dto.UpdateExamQuestionRequest;
import com.mstrust.exam.dto.ExamWithQuestionsDTO;
import com.mstrust.exam.entity.*;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.ExamQuestionRepository;
import com.mstrust.exam.repository.ExamRepository;
import com.mstrust.exam.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** ------------------------------------------
 * Mục đích: Service xử lý business logic cho Exam-Question relationships
 * Cung cấp các chức năng quản lý câu hỏi trong bài thi
 *
 * Features:
 * - Add/remove questions from exam
 * - Update question order and points
 * - Reorder questions in exam
 * - Get exam with full question list
 * - Validation cho exam status và permissions
 * - Bulk operations
 *
 * @author NVMANH with Cline
 * @created 18/11/2025 23:19
 */
@Service
@RequiredArgsConstructor
public class ExamQuestionService {

    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;

    /** ------------------------------------------
     * Mục đích: Thêm một câu hỏi vào bài thi
     * @param examId ID của bài thi
     * @param request Thông tin câu hỏi cần thêm
     * @return ExamQuestionDTO của relationship vừa tạo
     * @throws ResourceNotFoundException nếu không tìm thấy exam hoặc question
     * @throws BadRequestException nếu exam đã publish hoặc validation fail
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    @Transactional
    public ExamQuestionDTO addQuestionToExam(Long examId, AddQuestionToExamRequest request) {
        // Tìm exam
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + examId));

        // Kiểm tra exam chưa publish
        if (exam.getIsPublished()) {
            throw new BadRequestException("Không thể thêm câu hỏi vào bài thi đã publish.");
        }

        // Tìm question
        Question question = questionRepository.findByIdAndDeletedAtIsNull(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + request.getQuestionId()));

        // Kiểm tra question chưa có trong exam
        if (examQuestionRepository.existsByExamIdAndQuestionId(examId, request.getQuestionId())) {
            throw new BadRequestException("Câu hỏi đã tồn tại trong bài thi này.");
        }

        // Xác định question order
        Integer questionOrder = request.getQuestionOrder();
        if (questionOrder == null) {
            // Auto assign next order
            questionOrder = examQuestionRepository.findMaxOrderByExamId(examId) + 1;
        } else {
            // Validate order không trùng
            validateQuestionOrder(examId, questionOrder);
        }

        // Tạo ExamQuestion entity
        ExamQuestion examQuestion = ExamQuestion.builder()
                .exam(exam)
                .question(question)
                .questionOrder(questionOrder)
                .points(BigDecimal.valueOf(request.getPoints()))
                .createdAt(LocalDateTime.now())
                .build();

        // Lưu vào database
        ExamQuestion savedExamQuestion = examQuestionRepository.save(examQuestion);

        return convertToDTO(savedExamQuestion);
    }

    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin câu hỏi trong bài thi
     * @param examId ID của bài thi
     * @param questionId ID của câu hỏi
     * @param request Thông tin cập nhật
     * @return ExamQuestionDTO sau khi cập nhật
     * @throws ResourceNotFoundException nếu không tìm thấy relationship
     * @throws BadRequestException nếu exam đã publish
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    @Transactional
    public ExamQuestionDTO updateExamQuestion(Long examId, Long questionId, UpdateExamQuestionRequest request) {
        // Tìm ExamQuestion
        ExamQuestion examQuestion = examQuestionRepository.findByExamIdAndQuestionId(examId, questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy câu hỏi trong bài thi với examId: " + examId + ", questionId: " + questionId));

        // Kiểm tra exam chưa publish
        if (examQuestion.getExam().getIsPublished()) {
            throw new BadRequestException("Không thể cập nhật câu hỏi trong bài thi đã publish.");
        }

        // Validate question order nếu có thay đổi
        if (request.getQuestionOrder() != null && !request.getQuestionOrder().equals(examQuestion.getQuestionOrder())) {
            validateQuestionOrder(examId, request.getQuestionOrder(), examQuestion.getId());
        }

        // Cập nhật fields
        if (request.getQuestionOrder() != null) {
            examQuestion.setQuestionOrder(request.getQuestionOrder());
        }
        if (request.getPoints() != null) {
            examQuestion.setPoints(BigDecimal.valueOf(request.getPoints()));
        }
        examQuestion.setUpdatedAt(LocalDateTime.now());

        // Lưu thay đổi
        ExamQuestion updatedExamQuestion = examQuestionRepository.save(examQuestion);

        return convertToDTO(updatedExamQuestion);
    }

    /** ------------------------------------------
     * Mục đích: Xóa một câu hỏi khỏi bài thi
     * @param examId ID của bài thi
     * @param questionId ID của câu hỏi cần xóa
     * @throws ResourceNotFoundException nếu không tìm thấy relationship
     * @throws BadRequestException nếu exam đã publish
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    @Transactional
    public void removeQuestionFromExam(Long examId, Long questionId) {
        // Tìm ExamQuestion
        ExamQuestion examQuestion = examQuestionRepository.findByExamIdAndQuestionId(examId, questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy câu hỏi trong bài thi với examId: " + examId + ", questionId: " + questionId));

        // Kiểm tra exam chưa publish
        if (examQuestion.getExam().getIsPublished()) {
            throw new BadRequestException("Không thể xóa câu hỏi khỏi bài thi đã publish.");
        }

        // Xóa relationship
        examQuestionRepository.delete(examQuestion);

        // Reorder các câu hỏi còn lại
        reorderQuestionsAfterRemoval(examId, examQuestion.getQuestionOrder());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách câu hỏi trong bài thi
     * @param examId ID của bài thi
     * @return Danh sách ExamQuestionDTO theo thứ tự
     * @throws ResourceNotFoundException nếu không tìm thấy exam
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    @Transactional(readOnly = true)
    public List<ExamQuestionDTO> getExamQuestions(Long examId) {
        // Kiểm tra exam tồn tại
        examRepository.findByIdAndDeletedAtIsNull(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + examId));

        return examQuestionRepository.findByExamIdOrderByQuestionOrder(examId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin bài thi kèm danh sách câu hỏi
     * @param examId ID của bài thi
     * @return ExamWithQuestionsDTO
     * @throws ResourceNotFoundException nếu không tìm thấy exam
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    @Transactional(readOnly = true)
    public ExamWithQuestionsDTO getExamWithQuestions(Long examId) {
        // Tìm exam
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + examId));

        // Lấy danh sách questions
        List<ExamQuestionDTO> questions = getExamQuestions(examId);

        // Tính tổng điểm
        Double totalPoints = examQuestionRepository.calculateTotalPointsByExamId(examId);

        return ExamWithQuestionsDTO.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .subjectClassId(exam.getSubjectClass() != null ? exam.getSubjectClass().getId() : null)
                .subjectClassName(exam.getSubjectClass() != null ? exam.getSubjectClass().getClassName() : null)
                .examPurpose(exam.getExamPurpose())
                .examFormat(exam.getExamFormat())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .durationMinutes(exam.getDurationMinutes())
                .totalScore(exam.getTotalScore() != null ? exam.getTotalScore().doubleValue() : null)
                .passingScore(exam.getPassingScore() != null ? exam.getPassingScore().doubleValue() : null)
                .monitoringLevel(exam.getMonitoringLevel())
                .isPublished(exam.getIsPublished())
                .currentStatus(exam.getCurrentStatus().name())
                .questions(questions)
                .calculatedTotalPoints(totalPoints)
                .createdBy(exam.getCreatedBy())
                .createdAt(exam.getCreatedAt())
                .updatedBy(exam.getUpdatedBy())
                .updatedAt(exam.getUpdatedAt())
                .version(exam.getVersion())
                .build();
    }

    /** ------------------------------------------
     * Mục đích: Reorder câu hỏi trong bài thi
     * @param examId ID của bài thi
     * @param questionOrders Map của questionId -> newOrder
     * @throws ResourceNotFoundException nếu không tìm thấy exam
     * @throws BadRequestException nếu exam đã publish hoặc order invalid
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    @Transactional
    public void reorderExamQuestions(Long examId, List<UpdateExamQuestionRequest> questionOrders) {
        // Tìm exam
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + examId));

        // Kiểm tra exam chưa publish
        if (exam.getIsPublished()) {
            throw new BadRequestException("Không thể reorder câu hỏi trong bài thi đã publish.");
        }

        // Validate orders
        validateQuestionOrders(examId, questionOrders);

        // Update từng question
        for (UpdateExamQuestionRequest orderRequest : questionOrders) {
            ExamQuestion examQuestion = examQuestionRepository
                    .findByExamIdAndQuestionId(examId, orderRequest.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy câu hỏi " + orderRequest.getQuestionId() + " trong bài thi"));

            examQuestion.setQuestionOrder(orderRequest.getQuestionOrder());
            examQuestion.setUpdatedAt(LocalDateTime.now());
            examQuestionRepository.save(examQuestion);
        }
    }

    /** ------------------------------------------
     * Mục đích: Xóa tất cả câu hỏi khỏi bài thi
     * @param examId ID của bài thi
     * @throws ResourceNotFoundException nếu không tìm thấy exam
     * @throws BadRequestException nếu exam đã publish
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    @Transactional
    public void clearExamQuestions(Long examId) {
        // Tìm exam
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + examId));

        // Kiểm tra exam chưa publish
        if (exam.getIsPublished()) {
            throw new BadRequestException("Không thể xóa tất cả câu hỏi khỏi bài thi đã publish.");
        }

        // Xóa tất cả questions
        examQuestionRepository.deleteByExamId(examId);
    }

    /** ------------------------------------------
     * Mục đích: Validate question order không trùng
     * @param examId ID của exam
     * @param questionOrder Order cần validate
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    private void validateQuestionOrder(Long examId, Integer questionOrder) {
        validateQuestionOrder(examId, questionOrder, null);
    }

    /** ------------------------------------------
     * Mục đích: Validate question order không trùng (exclude specific ID)
     * @param examId ID của exam
     * @param questionOrder Order cần validate
     * @param excludeId ID của ExamQuestion cần exclude (để update)
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    private void validateQuestionOrder(Long examId, Integer questionOrder, Long excludeId) {
        List<ExamQuestion> existingQuestions = examQuestionRepository.findByExamId(examId);

        for (ExamQuestion eq : existingQuestions) {
            if (!eq.getId().equals(excludeId) && eq.getQuestionOrder().equals(questionOrder)) {
                throw new BadRequestException("Thứ tự câu hỏi " + questionOrder + " đã được sử dụng.");
            }
        }
    }

    /** ------------------------------------------
     * Mục đích: Validate danh sách question orders
     * @param examId ID của exam
     * @param questionOrders Danh sách orders cần validate
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    private void validateQuestionOrders(Long examId, List<UpdateExamQuestionRequest> questionOrders) {
        // Check số lượng questions khớp
        long currentQuestionCount = examQuestionRepository.countByExamId(examId);
        if (questionOrders.size() != currentQuestionCount) {
            throw new BadRequestException("Số lượng câu hỏi trong request không khớp với số câu hỏi hiện tại.");
        }

        // Check tất cả questions tồn tại trong exam
        for (UpdateExamQuestionRequest order : questionOrders) {
            if (!examQuestionRepository.existsByExamIdAndQuestionId(examId, order.getQuestionId())) {
                throw new BadRequestException("Câu hỏi " + order.getQuestionId() + " không tồn tại trong bài thi.");
            }
        }

        // Check orders không trùng nhau và liên tục từ 1
        List<Integer> orders = questionOrders.stream()
                .map(UpdateExamQuestionRequest::getQuestionOrder)
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < orders.size(); i++) {
            if (!orders.get(i).equals(i + 1)) {
                throw new BadRequestException("Thứ tự câu hỏi phải liên tục từ 1 đến " + currentQuestionCount);
            }
        }
    }

    /** ------------------------------------------
     * Mục đích: Reorder questions sau khi xóa một question
     * @param examId ID của exam
     * @param removedOrder Thứ tự của question đã xóa
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    private void reorderQuestionsAfterRemoval(Long examId, Integer removedOrder) {
        List<ExamQuestion> remainingQuestions = examQuestionRepository
                .findByExamIdOrderByQuestionOrder(examId)
                .stream()
                .filter(eq -> eq.getQuestionOrder() > removedOrder)
                .collect(Collectors.toList());

        for (ExamQuestion eq : remainingQuestions) {
            eq.setQuestionOrder(eq.getQuestionOrder() - 1);
            eq.setUpdatedAt(LocalDateTime.now());
            examQuestionRepository.save(eq);
        }
    }

    /** ------------------------------------------
     * Mục đích: Convert ExamQuestion entity sang ExamQuestionDTO
     * @param examQuestion ExamQuestion entity
     * @return ExamQuestionDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:19
     */
    private ExamQuestionDTO convertToDTO(ExamQuestion examQuestion) {
        Question question = examQuestion.getQuestion();

        return ExamQuestionDTO.builder()
                .examId(examQuestion.getExam().getId())
                .questionId(examQuestion.getQuestion().getId())
                .questionOrder(examQuestion.getQuestionOrder())
                .points(examQuestion.getPoints() != null ? examQuestion.getPoints().doubleValue() : null)
                .questionType(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .questionText(question.getQuestionText())
                .tags(question.getTags())
                .options(question.getOptions())
                .correctAnswer(question.getCorrectAnswer())
                .maxWords(question.getMaxWords())
                .minWords(question.getMinWords())
                .programmingLanguage(question.getProgrammingLanguage())
                .starterCode(question.getStarterCode())
                .timeLimitSeconds(question.getTimeLimitSeconds())
                .memoryLimitMb(question.getMemoryLimitMb())
                .blankPositions(question.getBlankPositions())
                .leftItems(question.getLeftItems())
                .rightItems(question.getRightItems())
                .attachments(question.getAttachments())
                .subjectId(question.getSubject() != null ? question.getSubject().getId() : null)
                .subjectName(question.getSubject() != null ? question.getSubject().getSubjectName() : null)
                .createdAt(examQuestion.getCreatedAt())
                .build();
    }
}

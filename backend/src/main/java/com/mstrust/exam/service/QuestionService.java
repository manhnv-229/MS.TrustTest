package com.mstrust.exam.service;

import com.mstrust.exam.dto.CreateQuestionRequest;
import com.mstrust.exam.dto.QuestionDTO;
import com.mstrust.exam.dto.UpdateQuestionRequest;
import com.mstrust.exam.entity.*;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.ExamQuestionRepository;
import com.mstrust.exam.repository.QuestionRepository;
import com.mstrust.exam.repository.SubjectRepository;
import com.mstrust.exam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** ------------------------------------------
 * Mục đích: Service xử lý business logic cho Question Bank
 * Cung cấp các chức năng CRUD và quản lý câu hỏi
 * 
 * Features:
 * - CRUD operations
 * - Search & filter (by subject, type, difficulty, creator)
 * - Validation theo question type
 * - Track usage in exams
 * - Soft delete với validation
 * 
 * @author NVMANH with Cline
 * @created 18/11/2025 18:38
 */
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ExamQuestionRepository examQuestionRepository;

    /** ------------------------------------------
     * Mục đích: Tạo mới một câu hỏi trong Question Bank
     * @param request Thông tin câu hỏi cần tạo
     * @return QuestionDTO của câu hỏi vừa tạo
     * @throws ResourceNotFoundException nếu không tìm thấy subject (khi có subjectId)
     * @throws BadRequestException nếu validation fail
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional
    public QuestionDTO createQuestion(CreateQuestionRequest request) {
        // Validate theo question type
        validateQuestionRequest(request.getQuestionType(), request);

        // Tìm subject nếu có subjectId
        Subject subject = null;
        if (request.getSubjectId() != null) {
            subject = subjectRepository.findByIdAndDeletedAtIsNull(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy môn học với ID: " + request.getSubjectId()));
        }

        // Lấy user hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));

        // Tạo entity từ request
        Question question = Question.builder()
                .subject(subject)
                .questionType(request.getQuestionType())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : Difficulty.MEDIUM)
                .tags(request.getTags())
                .questionText(request.getQuestionText())
                .options(request.getOptions())
                .correctAnswer(request.getCorrectAnswer())
                .maxWords(request.getMaxWords())
                .minWords(request.getMinWords())
                .gradingCriteria(request.getGradingCriteria())
                .programmingLanguage(request.getProgrammingLanguage())
                .starterCode(request.getStarterCode())
                .testCases(request.getTestCases())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .memoryLimitMb(request.getMemoryLimitMb())
                .blankPositions(request.getBlankPositions())
                .leftItems(request.getLeftItems())
                .rightItems(request.getRightItems())
                .correctMatches(request.getCorrectMatches())
                .attachments(request.getAttachments())
                .version(1)
                .createdBy(currentUser.getId())
                .createdAt(LocalDateTime.now())
                .build();

        // Lưu vào database
        Question savedQuestion = questionRepository.save(question);

        return convertToDTO(savedQuestion);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả các câu hỏi (chưa bị xóa)
     * @return Danh sách QuestionDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional(readOnly = true)
    public List<QuestionDTO> getAllQuestions() {
        return questionRepository.findByDeletedAtIsNull()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các câu hỏi với phân trang
     * @param pageable Thông tin phân trang
     * @return Page chứa QuestionDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional(readOnly = true)
    public Page<QuestionDTO> getQuestionsPage(Pageable pageable) {
        return questionRepository.findByDeletedAtIsNull(pageable)
                .map(this::convertToDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một câu hỏi theo ID
     * @param id ID của câu hỏi
     * @return QuestionDTO
     * @throws ResourceNotFoundException nếu không tìm thấy câu hỏi
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional(readOnly = true)
    public QuestionDTO getQuestionById(Long id) {
        Question question = questionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + id));

        return convertToDTO(question);
    }

    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin một câu hỏi
     * @param id ID của câu hỏi cần cập nhật
     * @param request Thông tin cập nhật
     * @return QuestionDTO sau khi cập nhật
     * @throws ResourceNotFoundException nếu không tìm thấy câu hỏi
     * @throws BadRequestException nếu validation fail hoặc question đang được dùng trong published exam
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional
    public QuestionDTO updateQuestion(Long id, UpdateQuestionRequest request) {
        // Tìm câu hỏi cần cập nhật
        Question question = questionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + id));

        // Kiểm tra xem câu hỏi có đang được dùng trong published exam không
        Long publishedExamCount = examQuestionRepository.countPublishedExamsByQuestionId(id);
        if (publishedExamCount > 0) {
            throw new BadRequestException(
                    "Không thể cập nhật câu hỏi vì đang được sử dụng trong " + publishedExamCount + 
                    " bài thi đã publish. Vui lòng tạo version mới hoặc đợi exam kết thúc.");
        }

        // Validate theo question type
        validateQuestionRequest(request.getQuestionType(), request);

        // Nếu có thay đổi subject
        if (request.getSubjectId() != null && !request.getSubjectId().equals(
                question.getSubject() != null ? question.getSubject().getId() : null)) {
            Subject subject = subjectRepository.findByIdAndDeletedAtIsNull(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy môn học với ID: " + request.getSubjectId()));
            question.setSubject(subject);
        }

        // Lấy user hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));

        // Cập nhật các field
        question.setQuestionType(request.getQuestionType());
        question.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : Difficulty.MEDIUM);
        question.setTags(request.getTags());
        question.setQuestionText(request.getQuestionText());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setMaxWords(request.getMaxWords());
        question.setMinWords(request.getMinWords());
        question.setGradingCriteria(request.getGradingCriteria());
        question.setProgrammingLanguage(request.getProgrammingLanguage());
        question.setStarterCode(request.getStarterCode());
        question.setTestCases(request.getTestCases());
        question.setTimeLimitSeconds(request.getTimeLimitSeconds());
        question.setMemoryLimitMb(request.getMemoryLimitMb());
        question.setBlankPositions(request.getBlankPositions());
        question.setLeftItems(request.getLeftItems());
        question.setRightItems(request.getRightItems());
        question.setCorrectMatches(request.getCorrectMatches());
        question.setAttachments(request.getAttachments());
        question.setUpdatedBy(currentUser.getId());
        question.setUpdatedAt(LocalDateTime.now());

        // Increment version
        question.setVersion(question.getVersion() + 1);

        // Lưu thay đổi
        Question updatedQuestion = questionRepository.save(question);

        return convertToDTO(updatedQuestion);
    }

    /** ------------------------------------------
     * Mục đích: Xóa mềm một câu hỏi
     * @param id ID của câu hỏi cần xóa
     * @throws ResourceNotFoundException nếu không tìm thấy câu hỏi
     * @throws BadRequestException nếu câu hỏi đang được dùng trong published exam
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional
    public void deleteQuestion(Long id) {
        Question question = questionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + id));

        // Kiểm tra xem câu hỏi có đang được dùng trong published exam không
        Long publishedExamCount = examQuestionRepository.countPublishedExamsByQuestionId(id);
        if (publishedExamCount > 0) {
            throw new BadRequestException(
                    "Không thể xóa câu hỏi vì đang được sử dụng trong " + publishedExamCount + 
                    " bài thi đã publish.");
        }

        // Soft delete
        question.setDeletedAt(LocalDateTime.now());
        questionRepository.save(question);
    }

    /** ------------------------------------------
     * Mục đích: Tìm kiếm câu hỏi theo từ khóa
     * @param keyword Từ khóa tìm kiếm (trong question_text)
     * @param pageable Thông tin phân trang
     * @return Page chứa QuestionDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional(readOnly = true)
    public Page<QuestionDTO> searchQuestions(String keyword, Pageable pageable) {
        return questionRepository.searchByKeyword(keyword, pageable)
                .map(this::convertToDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách câu hỏi theo môn học
     * @param subjectId ID của môn học
     * @return Danh sách QuestionDTO thuộc môn học
     * @throws ResourceNotFoundException nếu không tìm thấy môn học
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsBySubject(Long subjectId) {
        // Kiểm tra subject tồn tại
        subjectRepository.findByIdAndDeletedAtIsNull(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + subjectId));

        return questionRepository.findBySubjectIdAndDeletedAtIsNull(subjectId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách câu hỏi theo loại
     * @param type Loại câu hỏi
     * @return Danh sách QuestionDTO theo loại
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsByType(QuestionType type) {
        return questionRepository.findByQuestionTypeAndDeletedAtIsNull(type)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách câu hỏi theo độ khó
     * @param difficulty Độ khó
     * @return Danh sách QuestionDTO theo độ khó
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsByDifficulty(Difficulty difficulty) {
        return questionRepository.findByDifficultyAndDeletedAtIsNull(difficulty)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách câu hỏi theo người tạo
     * @param creatorId ID người tạo
     * @return Danh sách QuestionDTO do người này tạo
     * @throws ResourceNotFoundException nếu không tìm thấy user
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsByCreator(Long creatorId) {
        // Kiểm tra user tồn tại
        userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + creatorId));

        return questionRepository.findByCreatedByAndDeletedAtIsNull(creatorId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Validate question request theo loại câu hỏi
     * @param type Loại câu hỏi
     * @param request Request object (CreateQuestionRequest hoặc UpdateQuestionRequest)
     * @throws BadRequestException nếu validation fail
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    private void validateQuestionRequest(QuestionType type, Object request) {
        String options = null;
        String correctAnswer = null;
        Integer maxWords = null;
        Integer minWords = null;
        String programmingLanguage = null;
        String testCases = null;
        String blankPositions = null;
        String leftItems = null;
        String rightItems = null;
        String correctMatches = null;

        // Extract fields from request
        if (request instanceof CreateQuestionRequest) {
            CreateQuestionRequest req = (CreateQuestionRequest) request;
            options = req.getOptions();
            correctAnswer = req.getCorrectAnswer();
            maxWords = req.getMaxWords();
            minWords = req.getMinWords();
            programmingLanguage = req.getProgrammingLanguage();
            testCases = req.getTestCases();
            blankPositions = req.getBlankPositions();
            leftItems = req.getLeftItems();
            rightItems = req.getRightItems();
            correctMatches = req.getCorrectMatches();
        } else if (request instanceof UpdateQuestionRequest) {
            UpdateQuestionRequest req = (UpdateQuestionRequest) request;
            options = req.getOptions();
            correctAnswer = req.getCorrectAnswer();
            maxWords = req.getMaxWords();
            minWords = req.getMinWords();
            programmingLanguage = req.getProgrammingLanguage();
            testCases = req.getTestCases();
            blankPositions = req.getBlankPositions();
            leftItems = req.getLeftItems();
            rightItems = req.getRightItems();
            correctMatches = req.getCorrectMatches();
        }

        // Validate theo type
        switch (type) {
            case MULTIPLE_CHOICE:
            case MULTIPLE_SELECT:
                if (options == null || options.trim().isEmpty()) {
                    throw new BadRequestException("Options là bắt buộc cho câu hỏi loại " + type);
                }
                if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
                    throw new BadRequestException("Correct answer là bắt buộc cho câu hỏi loại " + type);
                }
                break;

            case ESSAY:
            case SHORT_ANSWER:
                if (maxWords != null && minWords != null && maxWords < minWords) {
                    throw new BadRequestException("Max words phải lớn hơn hoặc bằng min words");
                }
                break;

            case CODING:
                if (programmingLanguage == null || programmingLanguage.trim().isEmpty()) {
                    throw new BadRequestException("Programming language là bắt buộc cho câu hỏi CODING");
                }
                if (testCases == null || testCases.trim().isEmpty()) {
                    throw new BadRequestException("Test cases là bắt buộc cho câu hỏi CODING");
                }
                break;

            case FILL_IN_BLANK:
                if (blankPositions == null || blankPositions.trim().isEmpty()) {
                    throw new BadRequestException("Blank positions là bắt buộc cho câu hỏi FILL_IN_BLANK");
                }
                break;

            case MATCHING:
                if (leftItems == null || leftItems.trim().isEmpty()) {
                    throw new BadRequestException("Left items là bắt buộc cho câu hỏi MATCHING");
                }
                if (rightItems == null || rightItems.trim().isEmpty()) {
                    throw new BadRequestException("Right items là bắt buộc cho câu hỏi MATCHING");
                }
                if (correctMatches == null || correctMatches.trim().isEmpty()) {
                    throw new BadRequestException("Correct matches là bắt buộc cho câu hỏi MATCHING");
                }
                break;

            case TRUE_FALSE:
                if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
                    throw new BadRequestException("Correct answer là bắt buộc cho câu hỏi TRUE_FALSE");
                }
                if (!correctAnswer.equalsIgnoreCase("true") && !correctAnswer.equalsIgnoreCase("false")) {
                    throw new BadRequestException("Correct answer cho TRUE_FALSE phải là 'true' hoặc 'false'");
                }
                break;
        }
    }

    /** ------------------------------------------
     * Mục đích: Convert Question entity sang QuestionDTO
     * @param question Question entity
     * @return QuestionDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 18:38
     */
    private QuestionDTO convertToDTO(Question question) {
        // Get usage statistics
        Long usageCount = examQuestionRepository.countByQuestionId(question.getId());

        // Get creator and updater names
        String createdByName = null;
        String updatedByName = null;

        if (question.getCreatedBy() != null) {
            createdByName = userRepository.findById(question.getCreatedBy())
                    .map(User::getFullName)
                    .orElse(null);
        }

        if (question.getUpdatedBy() != null) {
            updatedByName = userRepository.findById(question.getUpdatedBy())
                    .map(User::getFullName)
                    .orElse(null);
        }

        return QuestionDTO.builder()
                .id(question.getId())
                .subjectId(question.getSubject() != null ? question.getSubject().getId() : null)
                .subjectName(question.getSubject() != null ? question.getSubject().getSubjectName() : null)
                .questionType(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .tags(question.getTags())
                .questionText(question.getQuestionText())
                .options(question.getOptions())
                .correctAnswer(question.getCorrectAnswer())
                .maxWords(question.getMaxWords())
                .minWords(question.getMinWords())
                .gradingCriteria(question.getGradingCriteria())
                .programmingLanguage(question.getProgrammingLanguage())
                .starterCode(question.getStarterCode())
                .testCases(question.getTestCases())
                .timeLimitSeconds(question.getTimeLimitSeconds())
                .memoryLimitMb(question.getMemoryLimitMb())
                .blankPositions(question.getBlankPositions())
                .leftItems(question.getLeftItems())
                .rightItems(question.getRightItems())
                .correctMatches(question.getCorrectMatches())
                .attachments(question.getAttachments())
                .usageCount(usageCount != null ? usageCount : 0L)
                .version(question.getVersion())
                .createdBy(question.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(question.getCreatedAt())
                .updatedBy(question.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(question.getUpdatedAt())
                .build();
    }
}

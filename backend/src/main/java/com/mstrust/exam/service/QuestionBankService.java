package com.mstrust.exam.service;

import com.mstrust.exam.dto.CreateQuestionBankRequest;
import com.mstrust.exam.dto.QuestionBankDTO;
import com.mstrust.exam.dto.UpdateQuestionBankRequest;
import com.mstrust.exam.entity.*;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.QuestionBankRepository;
import com.mstrust.exam.repository.SubjectRepository;
import com.mstrust.exam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý business logic cho Question Bank (Phase 4)
 * CreatedBy: K24DTCN210-NVMANH (19/11/2025 02:07)
 */
@Service
@RequiredArgsConstructor
public class QuestionBankService {

    private final QuestionBankRepository questionBankRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    /* ---------------------------------------------------
     * Tạo mới câu hỏi vào Question Bank
     * @param request Thông tin câu hỏi
     * @return QuestionBankDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 02:07)
     * --------------------------------------------------- */
    @Transactional
    public QuestionBankDTO createQuestion(CreateQuestionBankRequest request) {
        System.out.println("=== DEBUG: createQuestion START ===");
        System.out.println("Request: " + request);
        
        // Validate
        System.out.println("DEBUG: Validating question type: " + request.getQuestionType());
        validateQuestionByType(request.getQuestionType(), request);
        System.out.println("DEBUG: Validation passed");

        // Get subject
        Subject subject = null;
        if (request.getSubjectId() != null) {
            System.out.println("DEBUG: Finding subject with ID: " + request.getSubjectId());
            subject = subjectRepository.findByIdAndDeletedAtIsNull(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy môn học với ID: " + request.getSubjectId()));
            System.out.println("DEBUG: Subject found: " + subject.getSubjectName());
        }

        // Get current user
        System.out.println("DEBUG: Getting current user...");
        User currentUser = getCurrentUser();
        System.out.println("DEBUG: Current user found: " + currentUser.getFullName() + " (ID: " + currentUser.getId() + ")");

        // Create entity
        QuestionBank questionBank = QuestionBank.builder()
                .subject(subject)
                .questionType(request.getQuestionType())
                .difficulty(request.getDifficulty())
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
                .createdBy(currentUser)
                .build();

        QuestionBank saved = questionBankRepository.save(questionBank);
        return convertToDTO(saved);
    }

    /* ---------------------------------------------------
     * Lấy danh sách câu hỏi với filter và phân trang
     * @param subjectId ID môn học (optional)
     * @param difficulty Độ khó (optional)
     * @param type Loại câu hỏi (optional)
     * @param keyword Từ khóa (optional)
     * @param pageable Phân trang
     * @return Page<QuestionBankDTO>
     * @author: K24DTCN210-NVMANH (19/11/2025 02:07)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public Page<QuestionBankDTO> filterQuestions(
            Long subjectId,
            Difficulty difficulty,
            QuestionType type,
            String keyword,
            Pageable pageable) {
        
        return questionBankRepository.filterQuestions(subjectId, difficulty, type, keyword, pageable)
                .map(this::convertToDTO);
    }

    /* ---------------------------------------------------
     * Lấy chi tiết một câu hỏi
     * @param id ID câu hỏi
     * @return QuestionBankDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 02:07)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public QuestionBankDTO getQuestionById(Long id) {
        QuestionBank question = questionBankRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + id));
        return convertToDTO(question);
    }

    /* ---------------------------------------------------
     * Cập nhật câu hỏi
     * @param id ID câu hỏi
     * @param request Thông tin cập nhật
     * @return QuestionBankDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 02:07)
     * --------------------------------------------------- */
    @Transactional
    public QuestionBankDTO updateQuestion(Long id, UpdateQuestionBankRequest request) {
        QuestionBank question = questionBankRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + id));

        // Check if used in exam
        if (questionBankRepository.isQuestionInUse(id)) {
            throw new BadRequestException("Không thể cập nhật câu hỏi đang được sử dụng trong bài thi");
        }

        User currentUser = getCurrentUser();

        // Update fields
        if (request.getDifficulty() != null) {
            question.setDifficulty(request.getDifficulty());
        }
        if (request.getTags() != null) {
            question.setTags(request.getTags());
        }
        if (request.getQuestionText() != null) {
            question.setQuestionText(request.getQuestionText());
        }
        if (request.getOptions() != null) {
            question.setOptions(request.getOptions());
        }
        if (request.getCorrectAnswer() != null) {
            question.setCorrectAnswer(request.getCorrectAnswer());
        }
        if (request.getMaxWords() != null) {
            question.setMaxWords(request.getMaxWords());
        }
        if (request.getMinWords() != null) {
            question.setMinWords(request.getMinWords());
        }
        if (request.getGradingCriteria() != null) {
            question.setGradingCriteria(request.getGradingCriteria());
        }
        if (request.getProgrammingLanguage() != null) {
            question.setProgrammingLanguage(request.getProgrammingLanguage());
        }
        if (request.getStarterCode() != null) {
            question.setStarterCode(request.getStarterCode());
        }
        if (request.getTestCases() != null) {
            question.setTestCases(request.getTestCases());
        }
        if (request.getTimeLimitSeconds() != null) {
            question.setTimeLimitSeconds(request.getTimeLimitSeconds());
        }
        if (request.getMemoryLimitMb() != null) {
            question.setMemoryLimitMb(request.getMemoryLimitMb());
        }
        if (request.getBlankPositions() != null) {
            question.setBlankPositions(request.getBlankPositions());
        }
        if (request.getLeftItems() != null) {
            question.setLeftItems(request.getLeftItems());
        }
        if (request.getRightItems() != null) {
            question.setRightItems(request.getRightItems());
        }
        if (request.getCorrectMatches() != null) {
            question.setCorrectMatches(request.getCorrectMatches());
        }
        if (request.getAttachments() != null) {
            question.setAttachments(request.getAttachments());
        }

        question.setUpdatedBy(currentUser);
        question.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        QuestionBank updated = questionBankRepository.save(question);
        return convertToDTO(updated);
    }

    /* ---------------------------------------------------
     * Xóa câu hỏi (soft delete)
     * @param id ID câu hỏi
     * @author: K24DTCN210-NVMANH (19/11/2025 02:07)
     * --------------------------------------------------- */
    @Transactional
    public void deleteQuestion(Long id) {
        QuestionBank question = questionBankRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy câu hỏi với ID: " + id));

        if (questionBankRepository.isQuestionInUse(id)) {
            throw new BadRequestException("Không thể xóa câu hỏi đang được sử dụng trong bài thi");
        }

        question.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        questionBankRepository.save(question);
    }

    /* ---------------------------------------------------
     * Lấy thống kê câu hỏi theo môn học
     * @param subjectId ID môn học
     * @return Thống kê
     * @author: K24DTCN210-NVMANH (19/11/2025 02:07)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public Object getStatistics(Long subjectId) {
        long totalQuestions = questionBankRepository.countBySubject(subjectId);
        List<Object[]> byDifficulty = questionBankRepository.getStatisticsByDifficulty(subjectId);
        List<Object[]> byType = questionBankRepository.getStatisticsByType(subjectId);

        return new Object() {
            public final long total = totalQuestions;
            public final List<Object[]> byDifficulty_stats = byDifficulty;
            public final List<Object[]> byType_stats = byType;
        };
    }

    /* ---------------------------------------------------
     * Validate câu hỏi theo loại
     * @param type Loại câu hỏi
     * @param request Request object
     * @author: K24DTCN210-NVMANH (19/11/2025 02:07)
     * --------------------------------------------------- */
    private void validateQuestionByType(QuestionType type, CreateQuestionBankRequest request) {
        switch (type) {
            case MULTIPLE_CHOICE:
            case MULTIPLE_SELECT:
                if (request.getOptions() == null || request.getOptions().trim().isEmpty()) {
                    throw new BadRequestException("Options là bắt buộc cho " + type);
                }
                if (request.getCorrectAnswer() == null || request.getCorrectAnswer().trim().isEmpty()) {
                    throw new BadRequestException("Correct answer là bắt buộc cho " + type);
                }
                break;
            case CODING:
                if (request.getProgrammingLanguage() == null || request.getProgrammingLanguage().trim().isEmpty()) {
                    throw new BadRequestException("Programming language là bắt buộc cho CODING");
                }
                if (request.getTestCases() == null || request.getTestCases().trim().isEmpty()) {
                    throw new BadRequestException("Test cases là bắt buộc cho CODING");
                }
                break;
            case FILL_IN_BLANK:
                if (request.getBlankPositions() == null || request.getBlankPositions().trim().isEmpty()) {
                    throw new BadRequestException("Blank positions là bắt buộc cho FILL_IN_BLANK");
                }
                break;
            case MATCHING:
                if (request.getLeftItems() == null || request.getLeftItems().trim().isEmpty()) {
                    throw new BadRequestException("Left items là bắt buộc cho MATCHING");
                }
                if (request.getRightItems() == null || request.getRightItems().trim().isEmpty()) {
                    throw new BadRequestException("Right items là bắt buộc cho MATCHING");
                }
                if (request.getCorrectMatches() == null || request.getCorrectMatches().trim().isEmpty()) {
                    throw new BadRequestException("Correct matches là bắt buộc cho MATCHING");
                }
                break;
            case TRUE_FALSE:
                if (request.getCorrectAnswer() == null || request.getCorrectAnswer().trim().isEmpty()) {
                    throw new BadRequestException("Correct answer là bắt buộc cho TRUE_FALSE");
                }
                break;
            case ESSAY:
            case SHORT_ANSWER:
                // No specific validation
                break;
        }
    }

    /* ---------------------------------------------------
     * Convert Entity sang DTO
     * @param question QuestionBank entity
     * @return QuestionBankDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 02:07)
     * --------------------------------------------------- */
    private QuestionBankDTO convertToDTO(QuestionBank question) {
        long usageCount = questionBankRepository.isQuestionInUse(question.getId()) ? 1L : 0L;

        return QuestionBankDTO.builder()
                .id(question.getId())
                .subjectId(question.getSubject() != null ? question.getSubject().getId() : null)
                .subjectName(question.getSubject() != null ? question.getSubject().getSubjectName() : null)
                .questionType(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .tags(question.getTags())
                .version(question.getVersion())
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
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .createdById(question.getCreatedBy() != null ? question.getCreatedBy().getId() : null)
                .createdByName(question.getCreatedBy() != null ? question.getCreatedBy().getFullName() : null)
                .updatedById(question.getUpdatedBy() != null ? question.getUpdatedBy().getId() : null)
                .updatedByName(question.getUpdatedBy() != null ? question.getUpdatedBy().getFullName() : null)
                .usageCount(usageCount)
                .build();
    }

    /* ---------------------------------------------------
     * Lấy user hiện tại từ JWT token
     * @return User
     * @author: K24DTCN210-NVMANH (19/11/2025 02:07)
     * EditBy: K24DTCN210-NVMANH (25/11/2025 23:24) - Fix: Dùng findByEmail vì JWT lưu email
     * --------------------------------------------------- */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("DEBUG getCurrentUser: Looking for email = '" + email + "'");
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("DEBUG getCurrentUser: User NOT FOUND for email: " + email);
                    return new ResourceNotFoundException("Không tìm thấy user: " + email);
                });
        
        System.out.println("DEBUG getCurrentUser: User FOUND - ID: " + user.getId() + ", Name: " + user.getFullName());
        return user;
    }
}

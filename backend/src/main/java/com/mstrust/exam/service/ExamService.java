package com.mstrust.exam.service;

import com.mstrust.exam.dto.CreateExamRequest;
import com.mstrust.exam.dto.ExamDTO;
import com.mstrust.exam.dto.UpdateExamRequest;
import com.mstrust.exam.entity.*;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.ExamQuestionRepository;
import com.mstrust.exam.repository.ExamRepository;
import com.mstrust.exam.repository.SubjectClassRepository;
import com.mstrust.exam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** ------------------------------------------
 * Mục đích: Service xử lý business logic cho Exam management
 * Cung cấp các chức năng CRUD và quản lý bài thi
 *
 * Features:
 * - CRUD operations với validation
 * - Publish/Unpublish exams
 * - Time-based status calculation
 * - Search & filter theo subject class, purpose, format
 * - Business rules validation (time, scoring, conflicts)
 * - Soft delete với validation
 *
 * @author NVMANH with Cline
 * @created 18/11/2025 23:18
 */
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final UserRepository userRepository;
    private final ExamQuestionRepository examQuestionRepository;

    /** ------------------------------------------
     * Mục đích: Tạo mới một bài thi
     * @param request Thông tin bài thi cần tạo
     * @return ExamDTO của bài thi vừa tạo
     * @throws ResourceNotFoundException nếu không tìm thấy subject class
     * @throws BadRequestException nếu validation fail
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional
    public ExamDTO createExam(CreateExamRequest request) {
        // Validate request
        validateCreateExamRequest(request);

        // Tìm subject class
        SubjectClass subjectClass = subjectClassRepository.findByIdAndDeletedAtIsNull(request.getSubjectClassId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lớp học phần với ID: " + request.getSubjectClassId()));

        // Lấy user hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));

        // Tạo entity từ request
        Exam exam = Exam.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .subjectClass(subjectClass)
                .examPurpose(request.getPurpose())
                .examFormat(request.getFormat())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(request.getDurationMinutes())
                .passingScore(BigDecimal.valueOf(request.getPassingScore()))
                .totalScore(BigDecimal.valueOf(request.getTotalScore()))
                .monitoringLevel(request.getMonitoringLevel())
                .isPublished(false) // Mặc định chưa publish
                .version(1)
                .createdBy(currentUser.getId())
                .createdAt(LocalDateTime.now())
                .build();

        // Lưu vào database
        Exam savedExam = examRepository.save(exam);

        return convertToDTO(savedExam);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả các bài thi (chưa bị xóa)
     * @return Danh sách ExamDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional(readOnly = true)
    public List<ExamDTO> getAllExams() {
        return examRepository.findByDeletedAtIsNull()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các bài thi với phân trang
     * @param pageable Thông tin phân trang
     * @return Page chứa ExamDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional(readOnly = true)
    public Page<ExamDTO> getExamsPage(Pageable pageable) {
        return examRepository.findByDeletedAtIsNull(pageable)
                .map(this::convertToDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một bài thi theo ID
     * @param id ID của bài thi
     * @return ExamDTO
     * @throws ResourceNotFoundException nếu không tìm thấy bài thi
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional(readOnly = true)
    public ExamDTO getExamById(Long id) {
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + id));

        return convertToDTO(exam);
    }

    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin một bài thi
     * @param id ID của bài thi cần cập nhật
     * @param request Thông tin cập nhật
     * @return ExamDTO sau khi cập nhật
     * @throws ResourceNotFoundException nếu không tìm thấy bài thi
     * @throws BadRequestException nếu validation fail hoặc bài thi đã publish
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional
    public ExamDTO updateExam(Long id, UpdateExamRequest request) {
        // Tìm bài thi cần cập nhật
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + id));

        // Kiểm tra xem bài thi có đang publish không
        if (exam.getIsPublished()) {
            throw new BadRequestException(
                    "Không thể cập nhật bài thi đã publish. Vui lòng unpublish trước khi cập nhật.");
        }

        // Validate request
        validateUpdateExamRequest(request, exam);

        // Nếu có thay đổi subject class
        if (request.getSubjectClassId() != null && !request.getSubjectClassId().equals(exam.getSubjectClass().getId())) {
            SubjectClass subjectClass = subjectClassRepository.findByIdAndDeletedAtIsNull(request.getSubjectClassId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy lớp học phần với ID: " + request.getSubjectClassId()));
            exam.setSubjectClass(subjectClass);
        }

        // Lấy user hiện tại
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + username));

        // Cập nhật các field
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setExamPurpose(request.getPurpose());
        exam.setExamFormat(request.getFormat());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setPassingScore(BigDecimal.valueOf(request.getPassingScore()));
        exam.setTotalScore(BigDecimal.valueOf(request.getTotalScore()));
        exam.setMonitoringLevel(request.getMonitoringLevel());
        exam.setUpdatedBy(currentUser.getId());
        exam.setUpdatedAt(LocalDateTime.now());

        // Increment version
        exam.setVersion(exam.getVersion() + 1);

        // Lưu thay đổi
        Exam updatedExam = examRepository.save(exam);

        return convertToDTO(updatedExam);
    }

    /** ------------------------------------------
     * Mục đích: Publish một bài thi (cho phép học sinh tham gia)
     * @param id ID của bài thi
     * @throws ResourceNotFoundException nếu không tìm thấy bài thi
     * @throws BadRequestException nếu bài thi không hợp lệ để publish
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional
    public void publishExam(Long id) {
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + id));

        // Validate trước khi publish
        validateExamForPublishing(exam);

        exam.setIsPublished(true);
        examRepository.save(exam);
    }

    /** ------------------------------------------
     * Mục đích: Unpublish một bài thi (hủy publish)
     * @param id ID của bài thi
     * @throws ResourceNotFoundException nếu không tìm thấy bài thi
     * @throws BadRequestException nếu bài thi đang diễn ra
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional
    public void unpublishExam(Long id) {
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + id));

        // Kiểm tra xem bài thi có đang diễn ra không
        ExamStatus currentStatus = exam.getCurrentStatus();
        if (currentStatus == ExamStatus.ONGOING) {
            throw new BadRequestException("Không thể unpublish bài thi đang diễn ra.");
        }

        exam.setIsPublished(false);
        examRepository.save(exam);
    }

    /** ------------------------------------------
     * Mục đích: Xóa mềm một bài thi
     * @param id ID của bài thi cần xóa
     * @throws ResourceNotFoundException nếu không tìm thấy bài thi
     * @throws BadRequestException nếu bài thi đã publish
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài thi với ID: " + id));

        // Kiểm tra xem bài thi có đang publish không
        if (exam.getIsPublished()) {
            throw new BadRequestException("Không thể xóa bài thi đã publish. Vui lòng unpublish trước.");
        }

        // Soft delete
        exam.setDeletedAt(LocalDateTime.now());
        examRepository.save(exam);
    }

    /** ------------------------------------------
     * Mục đích: Tìm kiếm bài thi theo từ khóa
     * @param keyword Từ khóa tìm kiếm (trong title hoặc description)
     * @param pageable Thông tin phân trang
     * @return Page chứa ExamDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional(readOnly = true)
    public Page<ExamDTO> searchExams(String keyword, Pageable pageable) {
        return examRepository.searchByKeyword(keyword, pageable)
                .map(this::convertToDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách bài thi theo subject class
     * @param subjectClassId ID của subject class
     * @return Danh sách ExamDTO thuộc subject class
     * @throws ResourceNotFoundException nếu không tìm thấy subject class
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional(readOnly = true)
    public List<ExamDTO> getExamsBySubjectClass(Long subjectClassId) {
        // Kiểm tra subject class tồn tại
        subjectClassRepository.findByIdAndDeletedAtIsNull(subjectClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học phần với ID: " + subjectClassId));

        return examRepository.findBySubjectClassIdAndDeletedAtIsNull(subjectClassId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách bài thi theo người tạo
     * @param creatorId ID người tạo
     * @return Danh sách ExamDTO do người này tạo
     * @throws ResourceNotFoundException nếu không tìm thấy user
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional(readOnly = true)
    public List<ExamDTO> getExamsByCreator(Long creatorId) {
        // Kiểm tra user tồn tại
        userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + creatorId));

        return examRepository.findByCreatedByIdAndDeletedAtIsNull(creatorId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách bài thi đang diễn ra
     * @return Danh sách ExamDTO đang diễn ra
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional(readOnly = true)
    public List<ExamDTO> getOngoingExams() {
        return examRepository.findOngoingExams(LocalDateTime.now())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách bài thi sắp diễn ra
     * @param pageable Thông tin phân trang
     * @return Page chứa ExamDTO sắp diễn ra
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    @Transactional(readOnly = true)
    public Page<ExamDTO> getUpcomingExams(Pageable pageable) {
        return examRepository.findUpcomingExams(LocalDateTime.now(), pageable)
                .map(this::convertToDTO);
    }

    /** ------------------------------------------
     * Mục đích: Validate CreateExamRequest
     * @param request Request cần validate
     * @throws BadRequestException nếu validation fail
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    private void validateCreateExamRequest(CreateExamRequest request) {
        // Validate time
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }

        // Validate scoring
        if (request.getPassingScore() > request.getTotalScore()) {
            throw new BadRequestException("Điểm pass không được lớn hơn tổng điểm.");
        }

        // Validate duration matches time range
        long calculatedDuration = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (Math.abs(calculatedDuration - request.getDurationMinutes()) > 5) { // Cho phép sai số 5 phút
            throw new BadRequestException("Thời lượng thi không khớp với khoảng thời gian start-end.");
        }

        // Check for time conflicts in same subject class
        List<Exam> conflictingExams = examRepository.findBySubjectClassIdAndDeletedAtIsNull(request.getSubjectClassId())
                .stream()
                .filter(exam -> hasTimeConflict(exam, request.getStartTime(), request.getEndTime()))
                .collect(Collectors.toList());

        if (!conflictingExams.isEmpty()) {
            throw new BadRequestException(
                    "Thời gian thi bị trùng với bài thi khác trong cùng lớp học phần: " +
                    conflictingExams.get(0).getTitle());
        }
    }

    /** ------------------------------------------
     * Mục đích: Validate UpdateExamRequest
     * @param request Request cần validate
     * @param existingExam Bài thi hiện tại
     * @throws BadRequestException nếu validation fail
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    private void validateUpdateExamRequest(UpdateExamRequest request, Exam existingExam) {
        // Validate time
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }

        // Validate scoring
        if (request.getPassingScore() > request.getTotalScore()) {
            throw new BadRequestException("Điểm pass không được lớn hơn tổng điểm.");
        }

        // Validate duration matches time range
        long calculatedDuration = java.time.Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (Math.abs(calculatedDuration - request.getDurationMinutes()) > 5) {
            throw new BadRequestException("Thời lượng thi không khớp với khoảng thời gian start-end.");
        }

        // Check for time conflicts (excluding current exam)
        Long subjectClassId = request.getSubjectClassId() != null ?
                request.getSubjectClassId() : existingExam.getSubjectClass().getId();

        List<Exam> conflictingExams = examRepository.findBySubjectClassIdAndDeletedAtIsNull(subjectClassId)
                .stream()
                .filter(exam -> !exam.getId().equals(existingExam.getId()))
                .filter(exam -> hasTimeConflict(exam, request.getStartTime(), request.getEndTime()))
                .collect(Collectors.toList());

        if (!conflictingExams.isEmpty()) {
            throw new BadRequestException(
                    "Thời gian thi bị trùng với bài thi khác trong cùng lớp học phần: " +
                    conflictingExams.get(0).getTitle());
        }
    }

    /** ------------------------------------------
     * Mục đích: Validate exam trước khi publish
     * @param exam Bài thi cần validate
     * @throws BadRequestException nếu không hợp lệ để publish
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    private void validateExamForPublishing(Exam exam) {
        // Check if exam has questions
        long questionCount = examQuestionRepository.countByExamId(exam.getId());
        if (questionCount == 0) {
            throw new BadRequestException("Không thể publish bài thi chưa có câu hỏi.");
        }

        // Check if start time is in future
        if (exam.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Không thể publish bài thi với thời gian bắt đầu trong quá khứ.");
        }
    }

    /** ------------------------------------------
     * Mục đích: Kiểm tra xung đột thời gian giữa 2 bài thi
     * @param exam Bài thi hiện có
     * @param startTime Thời gian bắt đầu mới
     * @param endTime Thời gian kết thúc mới
     * @return true nếu có xung đột
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    private boolean hasTimeConflict(Exam exam, LocalDateTime startTime, LocalDateTime endTime) {
        return !(endTime.isBefore(exam.getStartTime()) || startTime.isAfter(exam.getEndTime()));
    }

    /** ------------------------------------------
     * Mục đích: Convert Exam entity sang ExamDTO
     * @param exam Exam entity
     * @return ExamDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:18
     */
    private ExamDTO convertToDTO(Exam exam) {
        // Get statistics
        long totalQuestions = examQuestionRepository.countByExamId(exam.getId());
        long totalStudents = exam.getSubjectClass() != null ?
                exam.getSubjectClass().getSubjectClassStudents().size() : 0;

        // Get creator and updater names
        String createdByName = null;
        String updatedByName = null;

        if (exam.getCreatedBy() != null) {
            createdByName = userRepository.findById(exam.getCreatedBy())
                    .map(User::getFullName)
                    .orElse(null);
        }

        if (exam.getUpdatedBy() != null) {
            updatedByName = userRepository.findById(exam.getUpdatedBy())
                    .map(User::getFullName)
                    .orElse(null);
        }

        return ExamDTO.builder()
                .id(exam.getId())
                .subjectClassId(exam.getSubjectClass() != null ? exam.getSubjectClass().getId() : null)
                .subjectClassName(exam.getSubjectClass() != null ? exam.getSubjectClass().getClassName() : null)
                .subjectName(exam.getSubjectClass() != null && exam.getSubjectClass().getSubject() != null ?
                        exam.getSubjectClass().getSubject().getSubjectName() : null)
                .title(exam.getTitle())
                .description(exam.getDescription())
                .purpose(exam.getExamPurpose())
                .format(exam.getExamFormat())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .durationMinutes(exam.getDurationMinutes())
                .totalScore(exam.getTotalScore() != null ? exam.getTotalScore().doubleValue() : null)
                .passingScore(exam.getPassingScore() != null ? exam.getPassingScore().doubleValue() : null)
                .monitoringLevel(exam.getMonitoringLevel())
                .publicationStatus(exam.getIsPublished() ? ExamStatus.PUBLISHED : ExamStatus.DRAFT)
                .currentStatus(exam.getCurrentStatus().name())
                .totalQuestions((int) totalQuestions)
                .totalStudents((int) totalStudents)
                .createdBy(exam.getCreatedBy())
                .createdByName(createdByName)
                .createdAt(exam.getCreatedAt())
                .updatedBy(exam.getUpdatedBy())
                .updatedByName(updatedByName)
                .updatedAt(exam.getUpdatedAt())
                .version(exam.getVersion())
                .build();
    }
}

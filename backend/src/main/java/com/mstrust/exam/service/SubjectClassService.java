package com.mstrust.exam.service;

import com.mstrust.exam.dto.*;
import com.mstrust.exam.entity.*;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.DuplicateResourceException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** ------------------------------------------
 * Mục đích: Service xử lý business logic cho SubjectClass
 * @author NVMANH with Cline
 * @created 15/11/2025 14:32
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectClassService {
    
    private final SubjectClassRepository subjectClassRepository;
    private final SubjectClassStudentRepository subjectClassStudentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    
    /** ------------------------------------------
     * Mục đích: Tạo mới lớp học phần
     * @param request - Thông tin lớp học phần
     * @return SubjectClassDTO
     * @throws DuplicateResourceException - Nếu code đã tồn tại
     * @throws ResourceNotFoundException - Nếu subject hoặc teacher không tồn tại
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional
    public SubjectClassDTO createSubjectClass(CreateSubjectClassRequest request) {
        log.info("Creating subject class with code: {}", request.getCode());
        
        // Validate code không trùng
        if (subjectClassRepository.existsByCodeExcludingId(request.getCode(), null)) {
            throw new DuplicateResourceException("Mã lớp học phần '" + request.getCode() + "' đã tồn tại");
        }
        
        // Validate subject exists
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy môn học với ID: " + request.getSubjectId()));
        
        if (subject.getDeletedAt() != null) {
            throw new BadRequestException("Môn học đã bị xóa, không thể tạo lớp học phần");
        }
        
        // Validate teacher exists và có role TEACHER
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy giáo viên với ID: " + request.getTeacherId()));
        
        if (teacher.getDeletedAt() != null) {
            throw new BadRequestException("Giáo viên đã bị xóa, không thể phân công");
        }
        
        // Build entity
        SubjectClass subjectClass = SubjectClass.builder()
                .code(request.getCode())
                .subject(subject)
                .semester(request.getSemester())
                .teacher(teacher)
                .schedule(request.getSchedule())
                .maxStudents(request.getMaxStudents() != null ? request.getMaxStudents() : 50)
                .build();
        
        SubjectClass saved = subjectClassRepository.save(subjectClass);
        log.info("Created subject class with ID: {}", saved.getId());
        
        return SubjectClassDTO.fromEntity(saved);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy tất cả lớp học phần (không phân trang)
     * @return List<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public List<SubjectClassDTO> getAllSubjectClasses() {
        log.info("Getting all subject classes");
        return subjectClassRepository.findAllByDeletedAtIsNull().stream()
                .map(SubjectClassDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy tất cả lớp học phần với phân trang
     * @param pageable - Thông tin phân trang
     * @return Page<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public Page<SubjectClassDTO> getAllSubjectClassesWithPagination(Pageable pageable) {
        log.info("Getting subject classes with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return subjectClassRepository.findAllByDeletedAtIsNull(pageable)
                .map(SubjectClassDTO::fromEntity);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy thông tin lớp học phần theo ID
     * @param id - ID của lớp học phần
     * @return SubjectClassDTO
     * @throws ResourceNotFoundException - Nếu không tìm thấy
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public SubjectClassDTO getSubjectClassById(Long id) {
        log.info("Getting subject class by ID: {}", id);
        SubjectClass subjectClass = subjectClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lớp học phần với ID: " + id));
        
        if (subjectClass.isDeleted()) {
            throw new ResourceNotFoundException("Lớp học phần đã bị xóa");
        }
        
        return SubjectClassDTO.fromEntity(subjectClass);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy thông tin lớp học phần theo code
     * @param code - Mã lớp học phần
     * @return SubjectClassDTO
     * @throws ResourceNotFoundException - Nếu không tìm thấy
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public SubjectClassDTO getSubjectClassByCode(String code) {
        log.info("Getting subject class by code: {}", code);
        SubjectClass subjectClass = subjectClassRepository.findByCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lớp học phần với mã: " + code));
        
        return SubjectClassDTO.fromEntity(subjectClass);
    }
    
    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin lớp học phần
     * @param id - ID của lớp học phần
     * @param request - Thông tin cập nhật
     * @return SubjectClassDTO
     * @throws ResourceNotFoundException - Nếu không tìm thấy
     * @throws DuplicateResourceException - Nếu code mới đã tồn tại
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional
    public SubjectClassDTO updateSubjectClass(Long id, UpdateSubjectClassRequest request) {
        log.info("Updating subject class with ID: {}", id);
        
        SubjectClass subjectClass = subjectClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lớp học phần với ID: " + id));
        
        if (subjectClass.isDeleted()) {
            throw new BadRequestException("Không thể cập nhật lớp học phần đã bị xóa");
        }
        
        // Update code nếu có
        if (request.getCode() != null && !request.getCode().equals(subjectClass.getCode())) {
            if (subjectClassRepository.existsByCodeExcludingId(request.getCode(), id)) {
                throw new DuplicateResourceException("Mã lớp học phần '" + request.getCode() + "' đã tồn tại");
            }
            subjectClass.setCode(request.getCode());
        }
        
        // Update subject nếu có
        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy môn học với ID: " + request.getSubjectId()));
            
            if (subject.getDeletedAt() != null) {
                throw new BadRequestException("Môn học đã bị xóa");
            }
            subjectClass.setSubject(subject);
        }
        
        // Update semester nếu có
        if (request.getSemester() != null) {
            subjectClass.setSemester(request.getSemester());
        }
        
        // Update teacher nếu có
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy giáo viên với ID: " + request.getTeacherId()));
            
            if (teacher.getDeletedAt() != null) {
                throw new BadRequestException("Giáo viên đã bị xóa");
            }
            subjectClass.setTeacher(teacher);
        }
        
        // Update schedule
        if (request.getSchedule() != null) {
            subjectClass.setSchedule(request.getSchedule());
        }
        
        // Update maxStudents nếu có và validate
        if (request.getMaxStudents() != null) {
            int currentEnrolled = subjectClass.getEnrolledCount();
            if (request.getMaxStudents() < currentEnrolled) {
                throw new BadRequestException(
                        "Số lượng sinh viên tối đa (" + request.getMaxStudents() + 
                        ") không được nhỏ hơn số sinh viên đã đăng ký (" + currentEnrolled + ")");
            }
            subjectClass.setMaxStudents(request.getMaxStudents());
        }
        
        SubjectClass updated = subjectClassRepository.save(subjectClass);
        log.info("Updated subject class with ID: {}", id);
        
        return SubjectClassDTO.fromEntity(updated);
    }
    
    /** ------------------------------------------
     * Mục đích: Xóa mềm lớp học phần
     * @param id - ID của lớp học phần
     * @throws ResourceNotFoundException - Nếu không tìm thấy
     * @throws BadRequestException - Nếu còn sinh viên đang enrolled
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional
    public void deleteSubjectClass(Long id) {
        log.info("Deleting subject class with ID: {}", id);
        
        SubjectClass subjectClass = subjectClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lớp học phần với ID: " + id));
        
        if (subjectClass.isDeleted()) {
            throw new BadRequestException("Lớp học phần đã bị xóa trước đó");
        }
        
        // Kiểm tra còn sinh viên enrolled không
        int enrolledCount = subjectClass.getEnrolledCount();
        if (enrolledCount > 0) {
            throw new BadRequestException(
                    "Không thể xóa lớp học phần vì còn " + enrolledCount + " sinh viên đang đăng ký");
        }
        
        subjectClass.softDelete();
        subjectClassRepository.save(subjectClass);
        log.info("Deleted subject class with ID: {}", id);
    }
    
    /** ------------------------------------------
     * Mục đích: Tìm kiếm lớp học phần theo keyword
     * @param keyword - Từ khóa tìm kiếm
     * @param pageable - Thông tin phân trang
     * @return Page<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public Page<SubjectClassDTO> searchSubjectClasses(String keyword, Pageable pageable) {
        log.info("Searching subject classes with keyword: {}", keyword);
        return subjectClassRepository.searchByKeyword(keyword, pageable)
                .map(SubjectClassDTO::fromEntity);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp học phần theo subject ID
     * @param subjectId - ID của môn học
     * @return List<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public List<SubjectClassDTO> getSubjectClassesBySubject(Long subjectId) {
        log.info("Getting subject classes by subject ID: {}", subjectId);
        return subjectClassRepository.findBySubjectId(subjectId).stream()
                .map(SubjectClassDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp học phần theo semester
     * @param semester - Học kỳ
     * @return List<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public List<SubjectClassDTO> getSubjectClassesBySemester(String semester) {
        log.info("Getting subject classes by semester: {}", semester);
        return subjectClassRepository.findBySemester(semester).stream()
                .map(SubjectClassDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp học phần của giáo viên
     * @param teacherId - ID của giáo viên
     * @return List<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public List<SubjectClassDTO> getSubjectClassesByTeacher(Long teacherId) {
        log.info("Getting subject classes by teacher ID: {}", teacherId);
        return subjectClassRepository.findByTeacherId(teacherId).stream()
                .map(SubjectClassDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy danh sách sinh viên đã enroll vào lớp
     * @param subjectClassId - ID của lớp học phần
     * @return List<SubjectClassStudentDTO>
     * @throws ResourceNotFoundException - Nếu không tìm thấy lớp
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public List<SubjectClassStudentDTO> getEnrolledStudents(Long subjectClassId) {
        log.info("Getting enrolled students for subject class ID: {}", subjectClassId);
        
        // Validate subject class exists
        SubjectClass subjectClass = subjectClassRepository.findById(subjectClassId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lớp học phần với ID: " + subjectClassId));
        
        if (subjectClass.isDeleted()) {
            throw new BadRequestException("Lớp học phần đã bị xóa");
        }
        
        return subjectClassStudentRepository.findEnrolledStudentsBySubjectClassId(subjectClassId).stream()
                .map(SubjectClassStudentDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    /** ------------------------------------------
     * Mục đích: Đăng ký sinh viên vào lớp học phần
     * @param subjectClassId - ID của lớp học phần
     * @param studentId - ID của sinh viên
     * @return SubjectClassStudentDTO
     * @throws ResourceNotFoundException - Nếu không tìm thấy lớp hoặc sinh viên
     * @throws BadRequestException - Nếu vi phạm business rules
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional
    public SubjectClassStudentDTO enrollStudent(Long subjectClassId, Long studentId) {
        log.info("Enrolling student {} to subject class {}", studentId, subjectClassId);
        
        // Validate subject class exists
        SubjectClass subjectClass = subjectClassRepository.findById(subjectClassId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lớp học phần với ID: " + subjectClassId));
        
        if (subjectClass.isDeleted()) {
            throw new BadRequestException("Không thể đăng ký vào lớp học phần đã bị xóa");
        }
        
        // Validate student exists
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sinh viên với ID: " + studentId));
        
        if (student.getDeletedAt() != null) {
            throw new BadRequestException("Sinh viên đã bị xóa");
        }
        
        // Check if already enrolled
        if (subjectClassRepository.isStudentEnrolled(subjectClassId, studentId)) {
            throw new BadRequestException("Sinh viên đã đăng ký lớp học phần này rồi");
        }
        
        // Check if class is full
        if (subjectClass.isFull()) {
            throw new BadRequestException(
                    "Lớp học phần đã đầy (" + subjectClass.getMaxStudents() + "/" + 
                    subjectClass.getMaxStudents() + ")");
        }
        
        // Check duplicate subject in same semester
        if (subjectClassRepository.hasStudentEnrolledInSubjectThisSemester(
                subjectClass.getSubject().getId(), 
                subjectClass.getSemester(), 
                studentId)) {
            throw new BadRequestException(
                    "Sinh viên đã đăng ký môn học này trong học kỳ " + subjectClass.getSemester());
        }
        
        // Create enrollment
        SubjectClassStudentId enrollmentId = new SubjectClassStudentId(subjectClassId, studentId);
        SubjectClassStudent enrollment = SubjectClassStudent.builder()
                .id(enrollmentId)
                .subjectClass(subjectClass)
                .student(student)
                .status(SubjectClassStudent.EnrollmentStatus.ENROLLED)
                .build();
        
        SubjectClassStudent saved = subjectClassStudentRepository.save(enrollment);
        log.info("Student {} enrolled to subject class {}", studentId, subjectClassId);
        
        return SubjectClassStudentDTO.fromEntity(saved);
    }
    
    /** ------------------------------------------
     * Mục đích: Rút sinh viên khỏi lớp học phần (change status to DROPPED)
     * @param subjectClassId - ID của lớp học phần
     * @param studentId - ID của sinh viên
     * @throws ResourceNotFoundException - Nếu không tìm thấy enrollment
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional
    public void dropStudent(Long subjectClassId, Long studentId) {
        log.info("Dropping student {} from subject class {}", studentId, subjectClassId);
        
        SubjectClassStudent enrollment = subjectClassStudentRepository
                .findBySubjectClassIdAndStudentId(subjectClassId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thông tin đăng ký của sinh viên này"));
        
        if (enrollment.getStatus() == SubjectClassStudent.EnrollmentStatus.DROPPED) {
            throw new BadRequestException("Sinh viên đã rút môn học này rồi");
        }
        
        enrollment.setStatus(SubjectClassStudent.EnrollmentStatus.DROPPED);
        subjectClassStudentRepository.save(enrollment);
        log.info("Student {} dropped from subject class {}", studentId, subjectClassId);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy số chỗ còn trống của lớp học phần
     * @param subjectClassId - ID của lớp học phần
     * @return Integer - số chỗ trống
     * @throws ResourceNotFoundException - Nếu không tìm thấy lớp
     * @author NVMANH with Cline
     * @created 15/11/2025 14:32
     */
    @Transactional(readOnly = true)
    public Integer getAvailableSlots(Long subjectClassId) {
        log.info("Getting available slots for subject class ID: {}", subjectClassId);
        
        SubjectClass subjectClass = subjectClassRepository.findById(subjectClassId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lớp học phần với ID: " + subjectClassId));
        
        if (subjectClass.isDeleted()) {
            throw new BadRequestException("Lớp học phần đã bị xóa");
        }
        
        return subjectClass.getAvailableSlots();
    }
}

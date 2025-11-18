package com.mstrust.exam.service;

import com.mstrust.exam.dto.CreateSubjectRequest;
import com.mstrust.exam.dto.SubjectDTO;
import com.mstrust.exam.dto.UpdateSubjectRequest;
import com.mstrust.exam.entity.Department;
import com.mstrust.exam.entity.Subject;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.DuplicateResourceException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.DepartmentRepository;
import com.mstrust.exam.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** ------------------------------------------
 * Mục đích: Service xử lý business logic cho Subject (Môn học)
 * Cung cấp các chức năng CRUD và quản lý môn học
 * @author NVMANH with Cline
 * @created 15/11/2025 14:16
 */
@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;

    /** ------------------------------------------
     * Mục đích: Tạo mới một môn học
     * @param request Thông tin môn học cần tạo
     * @return SubjectDTO của môn học vừa tạo
     * @throws DuplicateResourceException nếu mã môn học đã tồn tại
     * @throws ResourceNotFoundException nếu không tìm thấy department (khi có departmentId)
     * @author NVMANH with Cline
     * @created 15/11/2025 14:16
     */
    @Transactional
    public SubjectDTO createSubject(CreateSubjectRequest request) {
        // Kiểm tra mã môn học đã tồn tại chưa
        if (subjectRepository.existsBySubjectCodeAndDeletedAtIsNull(request.getSubjectCode())) {
            throw new DuplicateResourceException("Mã môn học '" + request.getSubjectCode() + "' đã tồn tại");
        }

        // Tìm department nếu có departmentId
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findByIdAndDeletedAtIsNull(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy khoa với ID: " + request.getDepartmentId()));
        }

        // Tạo entity từ request
        Subject subject = request.toEntity(department);

        // Lưu vào database
        Subject savedSubject = subjectRepository.save(subject);

        return SubjectDTO.fromEntity(savedSubject);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả các môn học (chưa bị xóa)
     * @return Danh sách SubjectDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:16
     */
    @Transactional(readOnly = true)
    public List<SubjectDTO> getAllSubjects() {
        return subjectRepository.findByDeletedAtIsNull()
                .stream()
                .map(SubjectDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các môn học với phân trang
     * @param pageable Thông tin phân trang
     * @return Page chứa SubjectDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:16
     */
    @Transactional(readOnly = true)
    public Page<SubjectDTO> getSubjectsPage(Pageable pageable) {
        return subjectRepository.findByDeletedAtIsNull(pageable)
                .map(SubjectDTO::fromEntity);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một môn học theo ID
     * @param id ID của môn học
     * @return SubjectDTO
     * @throws ResourceNotFoundException nếu không tìm thấy môn học
     * @author NVMANH with Cline
     * @created 15/11/2025 14:16
     */
    @Transactional(readOnly = true)
    public SubjectDTO getSubjectById(Long id) {
        Subject subject = subjectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + id));

        return SubjectDTO.fromEntity(subject);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một môn học theo mã môn
     * @param code Mã môn học
     * @return SubjectDTO
     * @throws ResourceNotFoundException nếu không tìm thấy môn học
     * @author NVMANH with Cline
     * @created 15/11/2025 14:16
     */
    @Transactional(readOnly = true)
    public SubjectDTO getSubjectByCode(String code) {
        Subject subject = subjectRepository.findBySubjectCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với mã: " + code));

        return SubjectDTO.fromEntity(subject);
    }

    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin một môn học
     * @param id ID của môn học cần cập nhật
     * @param request Thông tin cập nhật
     * @return SubjectDTO sau khi cập nhật
     * @throws ResourceNotFoundException nếu không tìm thấy môn học
     * @throws DuplicateResourceException nếu mã môn học mới đã tồn tại
     * @author NVMANH with Cline
     * @created 15/11/2025 14:16
     */
    @Transactional
    public SubjectDTO updateSubject(Long id, UpdateSubjectRequest request) {
        // Tìm môn học cần cập nhật
        Subject subject = subjectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + id));

        // Nếu có thay đổi mã môn học, kiểm tra trùng lặp
        if (request.getSubjectCode() != null && !request.getSubjectCode().equals(subject.getSubjectCode())) {
            if (subjectRepository.existsBySubjectCodeAndDeletedAtIsNull(request.getSubjectCode())) {
                throw new DuplicateResourceException("Mã môn học '" + request.getSubjectCode() + "' đã tồn tại");
            }
        }

        // Nếu có thay đổi department
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndDeletedAtIsNull(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy khoa với ID: " + request.getDepartmentId()));
            subject.setDepartment(department);
        }

        // Cập nhật các field khác
        request.updateEntity(subject);

        // Lưu thay đổi
        Subject updatedSubject = subjectRepository.save(subject);

        return SubjectDTO.fromEntity(updatedSubject);
    }

    /** ------------------------------------------
     * Mục đích: Xóa mềm một môn học
     * @param id ID của môn học cần xóa
     * @throws ResourceNotFoundException nếu không tìm thấy môn học
     * @throws BadRequestException nếu môn học còn subject classes đang active
     * @author NVMANH with Cline
     * @created 15/11/2025 14:16
     */
    @Transactional
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + id));

        // Kiểm tra xem môn học còn subject classes không
        Long subjectClassCount = subjectRepository.countActiveSubjectClassesBySubjectId(id);
        if (subjectClassCount > 0) {
            throw new BadRequestException(
                    "Không thể xóa môn học vì còn " + subjectClassCount + 
                    " lớp học phần đang active. Vui lòng xóa các lớp học phần trước.");
        }

        // Soft delete
        subject.markAsDeleted();
        subjectRepository.save(subject);
    }

    /** ------------------------------------------
     * Mục đích: Tìm kiếm môn học theo từ khóa (tên hoặc mã)
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @return Page chứa SubjectDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:16
     */
    @Transactional(readOnly = true)
    public Page<SubjectDTO> searchSubjects(String keyword, Pageable pageable) {
        return subjectRepository.searchByKeyword(keyword, pageable)
                .map(SubjectDTO::fromEntity);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách môn học theo khoa
     * @param departmentId ID của khoa
     * @return Danh sách SubjectDTO thuộc khoa
     * @throws ResourceNotFoundException nếu không tìm thấy khoa
     * @author NVMANH with Cline
     * @created 15/11/2025 14:16
     */
    @Transactional(readOnly = true)
    public List<SubjectDTO> getSubjectsByDepartment(Long departmentId) {
        // Kiểm tra department tồn tại
        departmentRepository.findByIdAndDeletedAtIsNull(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + departmentId));

        return subjectRepository.findByDepartmentId(departmentId)
                .stream()
                .map(SubjectDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

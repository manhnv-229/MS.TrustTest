package com.mstrust.exam.service;

import com.mstrust.exam.dto.ClassDTO;
import com.mstrust.exam.dto.CreateClassRequest;
import com.mstrust.exam.dto.UpdateClassRequest;
import com.mstrust.exam.dto.UserDTO;
import com.mstrust.exam.entity.ClassEntity;
import com.mstrust.exam.entity.Department;
import com.mstrust.exam.entity.User;
import com.mstrust.exam.exception.BadRequestException;
import com.mstrust.exam.exception.DuplicateResourceException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.ClassRepository;
import com.mstrust.exam.repository.DepartmentRepository;
import com.mstrust.exam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** ------------------------------------------
 * Mục đích: Service xử lý business logic cho Class (Lớp hành chính)
 * Cung cấp các chức năng CRUD và quản lý sinh viên trong lớp
 * @author NVMANH with Cline
 * @created 15/11/2025 14:00
 */
@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    /** ------------------------------------------
     * Mục đích: Tạo mới một lớp học
     * @param request Thông tin lớp cần tạo
     * @return ClassDTO của lớp vừa tạo
     * @throws DuplicateResourceException nếu mã lớp đã tồn tại
     * @throws ResourceNotFoundException nếu không tìm thấy department
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional
    public ClassDTO createClass(CreateClassRequest request) {
        // Kiểm tra mã lớp đã tồn tại chưa
        if (classRepository.existsByClassCodeAndDeletedAtIsNull(request.getClassCode())) {
            throw new DuplicateResourceException("Mã lớp '" + request.getClassCode() + "' đã tồn tại");
        }

        // Tìm department
        Department department = departmentRepository.findByIdAndDeletedAtIsNull(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy khoa với ID: " + request.getDepartmentId()));

        // Tạo entity từ request
        ClassEntity classEntity = request.toEntity(department);

        // Lưu vào database
        ClassEntity savedClass = classRepository.save(classEntity);

        return ClassDTO.fromEntity(savedClass);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả các lớp (chưa bị xóa)
     * @return Danh sách ClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional(readOnly = true)
    public List<ClassDTO> getAllClasses() {
        return classRepository.findByDeletedAtIsNull()
                .stream()
                .map(ClassDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các lớp với phân trang
     * @param pageable Thông tin phân trang
     * @return Page chứa ClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional(readOnly = true)
    public Page<ClassDTO> getClassesPage(Pageable pageable) {
        return classRepository.findByDeletedAtIsNull(pageable)
                .map(ClassDTO::fromEntity);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một lớp theo ID
     * @param id ID của lớp
     * @return ClassDTO
     * @throws ResourceNotFoundException nếu không tìm thấy lớp
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional(readOnly = true)
    public ClassDTO getClassById(Long id) {
        ClassEntity classEntity = classRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID: " + id));

        return ClassDTO.fromEntity(classEntity);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một lớp theo mã lớp
     * @param code Mã lớp
     * @return ClassDTO
     * @throws ResourceNotFoundException nếu không tìm thấy lớp
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional(readOnly = true)
    public ClassDTO getClassByCode(String code) {
        ClassEntity classEntity = classRepository.findByClassCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với mã: " + code));

        return ClassDTO.fromEntity(classEntity);
    }

    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin một lớp
     * @param id ID của lớp cần cập nhật
     * @param request Thông tin cập nhật
     * @return ClassDTO sau khi cập nhật
     * @throws ResourceNotFoundException nếu không tìm thấy lớp
     * @throws DuplicateResourceException nếu mã lớp mới đã tồn tại
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional
    public ClassDTO updateClass(Long id, UpdateClassRequest request) {
        // Tìm lớp cần cập nhật
        ClassEntity classEntity = classRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID: " + id));

        // Nếu có thay đổi mã lớp, kiểm tra trùng lặp
        if (request.getClassCode() != null
                && !request.getClassCode().equals(classEntity.getClassCode())) {
            if (classRepository.existsByClassCodeAndDeletedAtIsNull(request.getClassCode())) {
                throw new DuplicateResourceException("Mã lớp '" + request.getClassCode() + "' đã tồn tại");
            }
        }

        // Nếu có thay đổi department
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findByIdAndDeletedAtIsNull(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy khoa với ID: " + request.getDepartmentId()));
            classEntity.setDepartment(department);
        }

        // Cập nhật các field khác
        request.updateEntity(classEntity);

        // Lưu thay đổi
        ClassEntity updatedClass = classRepository.save(classEntity);

        return ClassDTO.fromEntity(updatedClass);
    }

    /** ------------------------------------------
     * Mục đích: Xóa mềm một lớp
     * @param id ID của lớp cần xóa
     * @throws ResourceNotFoundException nếu không tìm thấy lớp
     * @throws BadRequestException nếu lớp còn sinh viên
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional
    public void deleteClass(Long id) {
        ClassEntity classEntity = classRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID: " + id));

        // Kiểm tra xem lớp còn sinh viên không
        Long studentCount = classRepository.countStudentsByClassId(id);
        if (studentCount > 0) {
            throw new BadRequestException(
                    "Không thể xóa lớp vì còn " + studentCount + " sinh viên. Vui lòng chuyển sinh viên sang lớp khác trước.");
        }

        // Soft delete
        classEntity.markAsDeleted();
        classRepository.save(classEntity);
    }

    /** ------------------------------------------
     * Mục đích: Tìm kiếm lớp theo từ khóa (tên hoặc mã)
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @return Page chứa ClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional(readOnly = true)
    public Page<ClassDTO> searchClasses(String keyword, Pageable pageable) {
        return classRepository.searchByKeyword(keyword, pageable)
                .map(ClassDTO::fromEntity);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các lớp đang hoạt động
     * @return Danh sách ClassDTO với isActive = true
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional(readOnly = true)
    public List<ClassDTO> getActiveClasses() {
        return classRepository.findByIsActiveTrueAndDeletedAtIsNull()
                .stream()
                .map(ClassDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp theo khoa
     * @param departmentId ID của khoa
     * @return Danh sách ClassDTO thuộc khoa
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByDepartment(Long departmentId) {
        // Kiểm tra department tồn tại
        departmentRepository.findByIdAndDeletedAtIsNull(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + departmentId));

        return classRepository.findByDepartmentId(departmentId)
                .stream()
                .map(ClassDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp theo năm học
     * @param academicYear Năm học (ví dụ: 2023-2024)
     * @return Danh sách ClassDTO trong năm học
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional(readOnly = true)
    public List<ClassDTO> getClassesByAcademicYear(String academicYear) {
        return classRepository.findByAcademicYear(academicYear)
                .stream()
                .map(ClassDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách sinh viên trong một lớp
     * @param classId ID của lớp
     * @return Danh sách UserDTO của sinh viên
     * @throws ResourceNotFoundException nếu không tìm thấy lớp
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getStudentsByClassId(Long classId) {
        // Kiểm tra class tồn tại
        ClassEntity classEntity = classRepository.findByIdAndDeletedAtIsNull(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID: " + classId));

        return classEntity.getStudents()
                .stream()
                .filter(user -> user.getDeletedAt() == null)
                .map(UserDTO::from)
                .collect(Collectors.toList());
    }

    /** ------------------------------------------
     * Mục đích: Thêm sinh viên vào lớp
     * @param classId ID của lớp
     * @param studentId ID của sinh viên
     * @throws ResourceNotFoundException nếu không tìm thấy lớp hoặc sinh viên
     * @throws BadRequestException nếu sinh viên đã có lớp khác
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional
    public void addStudentToClass(Long classId, Long studentId) {
        // Tìm class
        ClassEntity classEntity = classRepository.findByIdAndDeletedAtIsNull(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID: " + classId));

        // Tìm student
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + studentId));

        // Kiểm tra student có bị xóa không
        if (student.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Sinh viên đã bị xóa");
        }

        // Kiểm tra student đã có lớp chưa
        if (student.getClassEntity() != null && student.getClassEntity().getDeletedAt() == null) {
            throw new BadRequestException(
                    "Sinh viên đã thuộc lớp " + student.getClassEntity().getClassName() + 
                    ". Vui lòng xóa khỏi lớp cũ trước.");
        }

        // Gán student vào class
        student.setClassEntity(classEntity);
        userRepository.save(student);
    }

    /** ------------------------------------------
     * Mục đích: Xóa sinh viên khỏi lớp
     * @param classId ID của lớp
     * @param studentId ID của sinh viên
     * @throws ResourceNotFoundException nếu không tìm thấy lớp hoặc sinh viên
     * @author NVMANH with Cline
     * @created 15/11/2025 14:00
     */
    @Transactional
    public void removeStudentFromClass(Long classId, Long studentId) {
        // Tìm class
        classRepository.findByIdAndDeletedAtIsNull(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID: " + classId));

        // Tìm student
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + studentId));

        // Kiểm tra student có bị xóa không
        if (student.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Sinh viên đã bị xóa");
        }

        // Kiểm tra student có thuộc lớp này không
        if (student.getClassEntity() == null || !student.getClassEntity().getId().equals(classId)) {
            throw new BadRequestException("Sinh viên không thuộc lớp này");
        }

        // Xóa student khỏi class
        student.setClassEntity(null);
        userRepository.save(student);
    }
}

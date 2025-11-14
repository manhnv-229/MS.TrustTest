package com.mstrust.exam.service;

import com.mstrust.exam.dto.CreateDepartmentRequest;
import com.mstrust.exam.dto.DepartmentDTO;
import com.mstrust.exam.dto.UpdateDepartmentRequest;
import com.mstrust.exam.entity.Department;
import com.mstrust.exam.exception.DuplicateResourceException;
import com.mstrust.exam.exception.ResourceNotFoundException;
import com.mstrust.exam.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/* ---------------------------------------------------
 * Service xử lý business logic cho Department
 * Cung cấp các chức năng CRUD và quản lý khoa
 * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
 * --------------------------------------------------- */
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    /* ---------------------------------------------------
     * Tạo mới một khoa
     * @param request Thông tin khoa cần tạo
     * @returns DepartmentDTO của khoa vừa tạo
     * @throws DuplicateResourceException nếu mã khoa đã tồn tại
     * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
     * --------------------------------------------------- */
    @Transactional
    public DepartmentDTO createDepartment(CreateDepartmentRequest request) {
        // Kiểm tra mã khoa đã tồn tại chưa
        if (departmentRepository.existsByDepartmentCodeAndDeletedAtIsNull(request.getDepartmentCode())) {
            throw new DuplicateResourceException("Mã khoa '" + request.getDepartmentCode() + "' đã tồn tại");
        }

        // Tạo entity từ request
        Department department = request.toEntity();
        
        // Lưu vào database
        Department savedDepartment = departmentRepository.save(department);
        
        // Trả về DTO
        return DepartmentDTO.fromEntity(savedDepartment);
    }

    /* ---------------------------------------------------
     * Lấy danh sách tất cả các khoa (chưa bị xóa)
     * @returns Danh sách DepartmentDTO
     * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findByDeletedAtIsNull()
                .stream()
                .map(DepartmentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /* ---------------------------------------------------
     * Lấy danh sách các khoa với phân trang
     * @param pageable Thông tin phân trang
     * @returns Page chứa DepartmentDTO
     * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public Page<DepartmentDTO> getDepartmentsPage(Pageable pageable) {
        return departmentRepository.findByDeletedAtIsNull(pageable)
                .map(DepartmentDTO::fromEntity);
    }

    /* ---------------------------------------------------
     * Lấy thông tin một khoa theo ID
     * @param id ID của khoa
     * @returns DepartmentDTO
     * @throws ResourceNotFoundException nếu không tìm thấy khoa
     * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + id));
        
        return DepartmentDTO.fromEntity(department);
    }

    /* ---------------------------------------------------
     * Lấy thông tin một khoa theo mã khoa
     * @param code Mã khoa
     * @returns DepartmentDTO
     * @throws ResourceNotFoundException nếu không tìm thấy khoa
     * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentByCode(String code) {
        Department department = departmentRepository.findByDepartmentCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với mã: " + code));
        
        return DepartmentDTO.fromEntity(department);
    }

    /* ---------------------------------------------------
     * Cập nhật thông tin một khoa
     * @param id ID của khoa cần cập nhật
     * @param request Thông tin cập nhật
     * @returns DepartmentDTO sau khi cập nhật
     * @throws ResourceNotFoundException nếu không tìm thấy khoa
     * @throws DuplicateResourceException nếu mã khoa mới đã tồn tại
     * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
     * --------------------------------------------------- */
    @Transactional
    public DepartmentDTO updateDepartment(Long id, UpdateDepartmentRequest request) {
        // Tìm khoa cần cập nhật
        Department department = departmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + id));

        // Nếu có thay đổi mã khoa, kiểm tra trùng lặp
        if (request.getDepartmentCode() != null 
            && !request.getDepartmentCode().equals(department.getDepartmentCode())) {
            if (departmentRepository.existsByDepartmentCodeAndDeletedAtIsNull(request.getDepartmentCode())) {
                throw new DuplicateResourceException("Mã khoa '" + request.getDepartmentCode() + "' đã tồn tại");
            }
        }

        // Cập nhật các field
        request.updateEntity(department);
        
        // Lưu thay đổi
        Department updatedDepartment = departmentRepository.save(department);
        
        return DepartmentDTO.fromEntity(updatedDepartment);
    }

    /* ---------------------------------------------------
     * Xóa mềm một khoa
     * @param id ID của khoa cần xóa
     * @throws ResourceNotFoundException nếu không tìm thấy khoa
     * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
     * --------------------------------------------------- */
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + id));

        // Soft delete
        department.markAsDeleted();
        departmentRepository.save(department);
    }

    /* ---------------------------------------------------
     * Tìm kiếm khoa theo từ khóa (tên hoặc mã)
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @returns Page chứa DepartmentDTO
     * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public Page<DepartmentDTO> searchDepartments(String keyword, Pageable pageable) {
        return departmentRepository.searchByKeyword(keyword, pageable)
                .map(DepartmentDTO::fromEntity);
    }

    /* ---------------------------------------------------
     * Lấy danh sách các khoa đang hoạt động
     * @returns Danh sách DepartmentDTO với isActive = true
     * @author: K24DTCN210-NVMANH (14/11/2025 14:10)
     * --------------------------------------------------- */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getActiveDepartments() {
        return departmentRepository.findByIsActiveTrueAndDeletedAtIsNull()
                .stream()
                .map(DepartmentDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

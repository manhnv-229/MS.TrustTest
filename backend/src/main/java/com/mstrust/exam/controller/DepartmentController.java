package com.mstrust.exam.controller;

import com.mstrust.exam.dto.CreateDepartmentRequest;
import com.mstrust.exam.dto.DepartmentDTO;
import com.mstrust.exam.dto.UpdateDepartmentRequest;
import com.mstrust.exam.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* ---------------------------------------------------
 * REST Controller cho Department APIs
 * Cung cấp các endpoint để quản lý khoa
 * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
 * --------------------------------------------------- */
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /* ---------------------------------------------------
     * Tạo mới một khoa
     * POST /api/departments
     * @param request Thông tin khoa cần tạo
     * @returns DepartmentDTO với status 201 Created
     * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
     * --------------------------------------------------- */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentDTO department = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(department);
    }

    /* ---------------------------------------------------
     * Lấy danh sách tất cả các khoa (không phân trang)
     * GET /api/departments
     * @returns Danh sách DepartmentDTO
     * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
     * --------------------------------------------------- */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    /* ---------------------------------------------------
     * Lấy danh sách các khoa với phân trang
     * GET /api/departments/page?page=0&size=10&sort=departmentName,asc
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @param sort Trường sắp xếp (default: id,asc)
     * @returns Page chứa DepartmentDTO
     * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
     * --------------------------------------------------- */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<Page<DepartmentDTO>> getDepartmentsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        // Xử lý sort parameter
        String sortField = sort[0];
        Sort.Direction sortDirection = sort.length > 1 && sort[1].equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<DepartmentDTO> departments = departmentService.getDepartmentsPage(pageable);
        
        return ResponseEntity.ok(departments);
    }

    /* ---------------------------------------------------
     * Lấy thông tin một khoa theo ID
     * GET /api/departments/{id}
     * @param id ID của khoa
     * @returns DepartmentDTO
     * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
     * --------------------------------------------------- */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    /* ---------------------------------------------------
     * Lấy thông tin một khoa theo mã khoa
     * GET /api/departments/code/{code}
     * @param code Mã khoa
     * @returns DepartmentDTO
     * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
     * --------------------------------------------------- */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<DepartmentDTO> getDepartmentByCode(@PathVariable String code) {
        DepartmentDTO department = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(department);
    }

    /* ---------------------------------------------------
     * Cập nhật thông tin một khoa
     * PUT /api/departments/{id}
     * @param id ID của khoa cần cập nhật
     * @param request Thông tin cập nhật
     * @returns DepartmentDTO sau khi cập nhật
     * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
     * --------------------------------------------------- */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        DepartmentDTO department = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(department);
    }

    /* ---------------------------------------------------
     * Xóa mềm một khoa
     * DELETE /api/departments/{id}
     * @param id ID của khoa cần xóa
     * @returns ResponseEntity với status 204 No Content
     * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
     * --------------------------------------------------- */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    /* ---------------------------------------------------
     * Tìm kiếm khoa theo từ khóa
     * GET /api/departments/search?keyword=cntt&page=0&size=10
     * @param keyword Từ khóa tìm kiếm
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @returns Page chứa DepartmentDTO
     * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
     * --------------------------------------------------- */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<Page<DepartmentDTO>> searchDepartments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DepartmentDTO> departments = departmentService.searchDepartments(keyword, pageable);
        
        return ResponseEntity.ok(departments);
    }

    /* ---------------------------------------------------
     * Lấy danh sách các khoa đang hoạt động
     * GET /api/departments/active
     * @returns Danh sách DepartmentDTO với isActive = true
     * @author: K24DTCN210-NVMANH (14/11/2025 14:11)
     * --------------------------------------------------- */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<List<DepartmentDTO>> getActiveDepartments() {
        List<DepartmentDTO> departments = departmentService.getActiveDepartments();
        return ResponseEntity.ok(departments);
    }
}

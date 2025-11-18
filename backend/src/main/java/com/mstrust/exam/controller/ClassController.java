package com.mstrust.exam.controller;

import com.mstrust.exam.dto.ClassDTO;
import com.mstrust.exam.dto.CreateClassRequest;
import com.mstrust.exam.dto.UpdateClassRequest;
import com.mstrust.exam.dto.UserDTO;
import com.mstrust.exam.service.ClassService;
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

/** ------------------------------------------
 * Mục đích: REST Controller cho Class APIs
 * Cung cấp các endpoint để quản lý lớp hành chính
 * @author NVMANH with Cline
 * @created 15/11/2025 14:02
 */
@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    /** ------------------------------------------
     * Mục đích: Tạo mới một lớp
     * POST /api/classes
     * @param request Thông tin lớp cần tạo
     * @return ClassDTO với status 201 Created
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<ClassDTO> createClass(@Valid @RequestBody CreateClassRequest request) {
        ClassDTO classDTO = classService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(classDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả các lớp (không phân trang)
     * GET /api/classes
     * @return Danh sách ClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<List<ClassDTO>> getAllClasses() {
        List<ClassDTO> classes = classService.getAllClasses();
        return ResponseEntity.ok(classes);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các lớp với phân trang
     * GET /api/classes/page?page=0&size=10&sortBy=className&sortDir=asc
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @param sortBy Trường sắp xếp (default: id)
     * @param sortDir Hướng sắp xếp (default: asc)
     * @return Page chứa ClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<Page<ClassDTO>> getClassesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction sortDirection = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<ClassDTO> classes = classService.getClassesPage(pageable);

        return ResponseEntity.ok(classes);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một lớp theo ID
     * GET /api/classes/{id}
     * @param id ID của lớp
     * @return ClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ClassDTO> getClassById(@PathVariable Long id) {
        ClassDTO classDTO = classService.getClassById(id);
        return ResponseEntity.ok(classDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một lớp theo mã lớp
     * GET /api/classes/code/{code}
     * @param code Mã lớp
     * @return ClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ClassDTO> getClassByCode(@PathVariable String code) {
        ClassDTO classDTO = classService.getClassByCode(code);
        return ResponseEntity.ok(classDTO);
    }

    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin một lớp
     * PUT /api/classes/{id}
     * @param id ID của lớp cần cập nhật
     * @param request Thông tin cập nhật
     * @return ClassDTO sau khi cập nhật
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER')")
    public ResponseEntity<ClassDTO> updateClass(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassRequest request) {
        ClassDTO classDTO = classService.updateClass(id, request);
        return ResponseEntity.ok(classDTO);
    }

    /** ------------------------------------------
     * Mục đích: Xóa mềm một lớp
     * DELETE /api/classes/{id}
     * @param id ID của lớp cần xóa
     * @return ResponseEntity với status 204 No Content
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }

    /** ------------------------------------------
     * Mục đích: Tìm kiếm lớp theo từ khóa
     * GET /api/classes/search?keyword=cntt&page=0&size=10
     * @param keyword Từ khóa tìm kiếm
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @return Page chứa ClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<Page<ClassDTO>> searchClasses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ClassDTO> classes = classService.searchClasses(keyword, pageable);

        return ResponseEntity.ok(classes);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các lớp đang hoạt động
     * GET /api/classes/active
     * @return Danh sách ClassDTO với isActive = true
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<List<ClassDTO>> getActiveClasses() {
        List<ClassDTO> classes = classService.getActiveClasses();
        return ResponseEntity.ok(classes);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp theo khoa
     * GET /api/classes/department/{departmentId}
     * @param departmentId ID của khoa
     * @return Danh sách ClassDTO thuộc khoa
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<List<ClassDTO>> getClassesByDepartment(@PathVariable Long departmentId) {
        List<ClassDTO> classes = classService.getClassesByDepartment(departmentId);
        return ResponseEntity.ok(classes);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp theo năm học
     * GET /api/classes/academic-year/{year}
     * @param year Năm học (ví dụ: 2023-2024)
     * @return Danh sách ClassDTO trong năm học
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @GetMapping("/academic-year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<List<ClassDTO>> getClassesByAcademicYear(@PathVariable String year) {
        List<ClassDTO> classes = classService.getClassesByAcademicYear(year);
        return ResponseEntity.ok(classes);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách sinh viên trong một lớp
     * GET /api/classes/{id}/students
     * @param id ID của lớp
     * @return Danh sách UserDTO của sinh viên
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER', 'TEACHER')")
    public ResponseEntity<List<UserDTO>> getStudentsByClassId(@PathVariable Long id) {
        List<UserDTO> students = classService.getStudentsByClassId(id);
        return ResponseEntity.ok(students);
    }

    /** ------------------------------------------
     * Mục đích: Thêm sinh viên vào lớp
     * POST /api/classes/{id}/students/{studentId}
     * @param id ID của lớp
     * @param studentId ID của sinh viên
     * @return ResponseEntity với status 200 OK
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @PostMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER')")
    public ResponseEntity<Void> addStudentToClass(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        classService.addStudentToClass(id, studentId);
        return ResponseEntity.ok().build();
    }

    /** ------------------------------------------
     * Mục đích: Xóa sinh viên khỏi lớp
     * DELETE /api/classes/{id}/students/{studentId}
     * @param id ID của lớp
     * @param studentId ID của sinh viên
     * @return ResponseEntity với status 204 No Content
     * @author NVMANH with Cline
     * @created 15/11/2025 14:02
     */
    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'CLASS_MANAGER')")
    public ResponseEntity<Void> removeStudentFromClass(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        classService.removeStudentFromClass(id, studentId);
        return ResponseEntity.noContent().build();
    }
}

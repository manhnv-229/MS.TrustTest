package com.mstrust.exam.controller;

import com.mstrust.exam.dto.CreateSubjectRequest;
import com.mstrust.exam.dto.SubjectDTO;
import com.mstrust.exam.dto.UpdateSubjectRequest;
import com.mstrust.exam.service.SubjectService;
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
 * Mục đích: REST Controller cho Subject APIs
 * Cung cấp các endpoint để quản lý môn học
 * @author NVMANH with Cline
 * @created 15/11/2025 14:17
 */
@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /** ------------------------------------------
     * Mục đích: Tạo mới một môn học
     * POST /api/subjects
     * @param request Thông tin môn học cần tạo
     * @return SubjectDTO với status 201 Created
     * @author NVMANH with Cline
     * @created 15/11/2025 14:17
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<SubjectDTO> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        SubjectDTO subjectDTO = subjectService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả các môn học (không phân trang)
     * GET /api/subjects
     * @return Danh sách SubjectDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:17
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<SubjectDTO>> getAllSubjects() {
        List<SubjectDTO> subjects = subjectService.getAllSubjects();
        return ResponseEntity.ok(subjects);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các môn học với phân trang
     * GET /api/subjects/page?page=0&size=10&sortBy=name&sortDir=asc
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @param sortBy Trường sắp xếp (default: id)
     * @param sortDir Hướng sắp xếp (default: asc)
     * @return Page chứa SubjectDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:17
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<SubjectDTO>> getSubjectsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction sortDirection = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<SubjectDTO> subjects = subjectService.getSubjectsPage(pageable);

        return ResponseEntity.ok(subjects);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một môn học theo ID
     * GET /api/subjects/{id}
     * @param id ID của môn học
     * @return SubjectDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:17
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<SubjectDTO> getSubjectById(@PathVariable Long id) {
        SubjectDTO subjectDTO = subjectService.getSubjectById(id);
        return ResponseEntity.ok(subjectDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một môn học theo mã môn
     * GET /api/subjects/code/{code}
     * @param code Mã môn học
     * @return SubjectDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:17
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<SubjectDTO> getSubjectByCode(@PathVariable String code) {
        SubjectDTO subjectDTO = subjectService.getSubjectByCode(code);
        return ResponseEntity.ok(subjectDTO);
    }

    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin một môn học
     * PUT /api/subjects/{id}
     * @param id ID của môn học cần cập nhật
     * @param request Thông tin cập nhật
     * @return SubjectDTO sau khi cập nhật
     * @author NVMANH with Cline
     * @created 15/11/2025 14:17
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<SubjectDTO> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectRequest request) {
        SubjectDTO subjectDTO = subjectService.updateSubject(id, request);
        return ResponseEntity.ok(subjectDTO);
    }

    /** ------------------------------------------
     * Mục đích: Xóa mềm một môn học
     * DELETE /api/subjects/{id}
     * @param id ID của môn học cần xóa
     * @return ResponseEntity với status 204 No Content
     * @author NVMANH with Cline
     * @created 15/11/2025 14:17
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    /** ------------------------------------------
     * Mục đích: Tìm kiếm môn học theo từ khóa
     * GET /api/subjects/search?keyword=java&page=0&size=10
     * @param keyword Từ khóa tìm kiếm
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @return Page chứa SubjectDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:17
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<SubjectDTO>> searchSubjects(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SubjectDTO> subjects = subjectService.searchSubjects(keyword, pageable);

        return ResponseEntity.ok(subjects);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách môn học theo khoa
     * GET /api/subjects/department/{departmentId}
     * @param departmentId ID của khoa
     * @return Danh sách SubjectDTO thuộc khoa
     * @author NVMANH with Cline
     * @created 15/11/2025 14:17
     */
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<SubjectDTO>> getSubjectsByDepartment(@PathVariable Long departmentId) {
        List<SubjectDTO> subjects = subjectService.getSubjectsByDepartment(departmentId);
        return ResponseEntity.ok(subjects);
    }
}

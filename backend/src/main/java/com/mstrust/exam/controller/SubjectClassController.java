package com.mstrust.exam.controller;

import com.mstrust.exam.dto.*;
import com.mstrust.exam.service.SubjectClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** ------------------------------------------
 * Mục đích: REST Controller cho SubjectClass APIs
 * @author NVMANH with Cline
 * @created 15/11/2025 14:35
 */
@RestController
@RequestMapping("/subject-classes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SubjectClass", description = "APIs quản lý lớp học phần")
public class SubjectClassController {
    
    private final SubjectClassService subjectClassService;
    
    /** ------------------------------------------
     * Mục đích: Tạo mới lớp học phần
     * POST /api/subject-classes
     * @param request - Thông tin lớp học phần
     * @return SubjectClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER')")
    @Operation(summary = "Tạo mới lớp học phần", description = "Tạo một lớp học phần mới với thông tin đầy đủ")
    public ResponseEntity<SubjectClassDTO> createSubjectClass(@Valid @RequestBody CreateSubjectClassRequest request) {
        log.info("REST request to create SubjectClass: {}", request.getCode());
        SubjectClassDTO result = subjectClassService.createSubjectClass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy tất cả lớp học phần (không phân trang)
     * GET /api/subject-classes
     * @return List<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Lấy tất cả lớp học phần", description = "Lấy danh sách tất cả lớp học phần (không phân trang)")
    public ResponseEntity<List<SubjectClassDTO>> getAllSubjectClasses() {
        log.info("REST request to get all SubjectClasses");
        List<SubjectClassDTO> result = subjectClassService.getAllSubjectClasses();
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy tất cả lớp học phần với phân trang
     * GET /api/subject-classes/page?page=0&size=10&sort=code,asc
     * @param pageable - Thông tin phân trang
     * @return Page<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Lấy lớp học phần với phân trang", description = "Lấy danh sách lớp học phần có hỗ trợ phân trang và sắp xếp")
    public ResponseEntity<Page<SubjectClassDTO>> getAllSubjectClassesWithPagination(Pageable pageable) {
        log.info("REST request to get SubjectClasses with pagination");
        Page<SubjectClassDTO> result = subjectClassService.getAllSubjectClassesWithPagination(pageable);
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy thông tin lớp học phần theo ID
     * GET /api/subject-classes/{id}
     * @param id - ID của lớp học phần
     * @return SubjectClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Lấy lớp học phần theo ID", description = "Lấy thông tin chi tiết một lớp học phần theo ID")
    public ResponseEntity<SubjectClassDTO> getSubjectClassById(@PathVariable Long id) {
        log.info("REST request to get SubjectClass by ID: {}", id);
        SubjectClassDTO result = subjectClassService.getSubjectClassById(id);
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy thông tin lớp học phần theo code
     * GET /api/subject-classes/code/{code}
     * @param code - Mã lớp học phần
     * @return SubjectClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Lấy lớp học phần theo code", description = "Lấy thông tin chi tiết một lớp học phần theo mã code")
    public ResponseEntity<SubjectClassDTO> getSubjectClassByCode(@PathVariable String code) {
        log.info("REST request to get SubjectClass by code: {}", code);
        SubjectClassDTO result = subjectClassService.getSubjectClassByCode(code);
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin lớp học phần
     * PUT /api/subject-classes/{id}
     * @param id - ID của lớp học phần
     * @param request - Thông tin cập nhật
     * @return SubjectClassDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER')")
    @Operation(summary = "Cập nhật lớp học phần", description = "Cập nhật thông tin một lớp học phần theo ID")
    public ResponseEntity<SubjectClassDTO> updateSubjectClass(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSubjectClassRequest request) {
        log.info("REST request to update SubjectClass with ID: {}", id);
        SubjectClassDTO result = subjectClassService.updateSubjectClass(id, request);
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Xóa mềm lớp học phần
     * DELETE /api/subject-classes/{id}
     * @param id - ID của lớp học phần
     * @return ResponseEntity<Void>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER')")
    @Operation(summary = "Xóa lớp học phần", description = "Xóa mềm một lớp học phần (chỉ khi không còn sinh viên enrolled)")
    public ResponseEntity<Void> deleteSubjectClass(@PathVariable Long id) {
        log.info("REST request to delete SubjectClass with ID: {}", id);
        subjectClassService.deleteSubjectClass(id);
        return ResponseEntity.noContent().build();
    }
    
    /** ------------------------------------------
     * Mục đích: Tìm kiếm lớp học phần theo keyword
     * GET /api/subject-classes/search?keyword=MATH&page=0&size=10
     * @param keyword - Từ khóa tìm kiếm
     * @param pageable - Thông tin phân trang
     * @return Page<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Tìm kiếm lớp học phần", description = "Tìm kiếm lớp học phần theo code, tên môn học, hoặc tên giáo viên")
    public ResponseEntity<Page<SubjectClassDTO>> searchSubjectClasses(
            @RequestParam String keyword,
            Pageable pageable) {
        log.info("REST request to search SubjectClasses with keyword: {}", keyword);
        Page<SubjectClassDTO> result = subjectClassService.searchSubjectClasses(keyword, pageable);
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp học phần theo môn học
     * GET /api/subject-classes/subject/{subjectId}
     * @param subjectId - ID của môn học
     * @return List<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Lấy lớp học phần theo môn học", description = "Lấy tất cả lớp học phần của một môn học")
    public ResponseEntity<List<SubjectClassDTO>> getSubjectClassesBySubject(@PathVariable Long subjectId) {
        log.info("REST request to get SubjectClasses by subject ID: {}", subjectId);
        List<SubjectClassDTO> result = subjectClassService.getSubjectClassesBySubject(subjectId);
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp học phần theo semester
     * GET /api/subject-classes/semester/{semester}
     * @param semester - Học kỳ (format: YYYY-YYYY-N)
     * @return List<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping("/semester/{semester}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Lấy lớp học phần theo semester", description = "Lấy tất cả lớp học phần trong một học kỳ")
    public ResponseEntity<List<SubjectClassDTO>> getSubjectClassesBySemester(@PathVariable String semester) {
        log.info("REST request to get SubjectClasses by semester: {}", semester);
        List<SubjectClassDTO> result = subjectClassService.getSubjectClassesBySemester(semester);
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy danh sách lớp học phần của giáo viên
     * GET /api/subject-classes/teacher/{teacherId}
     * @param teacherId - ID của giáo viên
     * @return List<SubjectClassDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER')")
    @Operation(summary = "Lấy lớp học phần theo giáo viên", description = "Lấy tất cả lớp học phần của một giáo viên")
    public ResponseEntity<List<SubjectClassDTO>> getSubjectClassesByTeacher(@PathVariable Long teacherId) {
        log.info("REST request to get SubjectClasses by teacher ID: {}", teacherId);
        List<SubjectClassDTO> result = subjectClassService.getSubjectClassesByTeacher(teacherId);
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy danh sách sinh viên đã enroll vào lớp
     * GET /api/subject-classes/{id}/students
     * @param id - ID của lớp học phần
     * @return List<SubjectClassStudentDTO>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER')")
    @Operation(summary = "Lấy sinh viên đã đăng ký", description = "Lấy danh sách sinh viên đã đăng ký vào lớp học phần")
    public ResponseEntity<List<SubjectClassStudentDTO>> getEnrolledStudents(@PathVariable Long id) {
        log.info("REST request to get enrolled students for SubjectClass ID: {}", id);
        List<SubjectClassStudentDTO> result = subjectClassService.getEnrolledStudents(id);
        return ResponseEntity.ok(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Đăng ký sinh viên vào lớp học phần
     * POST /api/subject-classes/{id}/students/{studentId}
     * @param id - ID của lớp học phần
     * @param studentId - ID của sinh viên
     * @return SubjectClassStudentDTO
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @PostMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Đăng ký sinh viên", description = "Đăng ký một sinh viên vào lớp học phần")
    public ResponseEntity<SubjectClassStudentDTO> enrollStudent(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        log.info("REST request to enroll student {} to SubjectClass {}", studentId, id);
        SubjectClassStudentDTO result = subjectClassService.enrollStudent(id, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    /** ------------------------------------------
     * Mục đích: Rút sinh viên khỏi lớp học phần
     * DELETE /api/subject-classes/{id}/students/{studentId}
     * @param id - ID của lớp học phần
     * @param studentId - ID của sinh viên
     * @return ResponseEntity<Void>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @DeleteMapping("/{id}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Rút sinh viên", description = "Rút một sinh viên khỏi lớp học phần (change status to DROPPED)")
    public ResponseEntity<Void> dropStudent(
            @PathVariable Long id,
            @PathVariable Long studentId) {
        log.info("REST request to drop student {} from SubjectClass {}", studentId, id);
        subjectClassService.dropStudent(id, studentId);
        return ResponseEntity.noContent().build();
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy số chỗ còn trống của lớp học phần
     * GET /api/subject-classes/{id}/available-slots
     * @param id - ID của lớp học phần
     * @return Integer - số chỗ trống
     * @author NVMANH with Cline
     * @created 15/11/2025 14:35
     */
    @GetMapping("/{id}/available-slots")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Lấy số chỗ trống", description = "Lấy số chỗ còn trống của lớp học phần")
    public ResponseEntity<Integer> getAvailableSlots(@PathVariable Long id) {
        log.info("REST request to get available slots for SubjectClass ID: {}", id);
        Integer result = subjectClassService.getAvailableSlots(id);
        return ResponseEntity.ok(result);
    }
}

package com.mstrust.exam.controller;

import com.mstrust.exam.dto.CreateExamRequest;
import com.mstrust.exam.dto.ExamDTO;
import com.mstrust.exam.dto.UpdateExamRequest;
import com.mstrust.exam.service.ExamService;
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
 * Mục đích: REST Controller cho Exam Management APIs
 * Cung cấp các endpoint để quản lý bài thi
 * @author NVMANH with Cline
 * @created 18/11/2025 23:33
 */
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    /** ------------------------------------------
     * Mục đích: Tạo mới một bài thi
     * POST /api/exams
     * @param request Thông tin bài thi cần tạo
     * @return ExamDTO với status 201 Created
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ExamDTO> createExam(@Valid @RequestBody CreateExamRequest request) {
        ExamDTO examDTO = examService.createExam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(examDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả các bài thi (không phân trang)
     * GET /api/exams
     * @return Danh sách ExamDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ExamDTO>> getAllExams() {
        List<ExamDTO> exams = examService.getAllExams();
        return ResponseEntity.ok(exams);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các bài thi với phân trang
     * GET /api/exams/page?page=0&size=10&sortBy=id&sortDir=asc
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @param sortBy Trường sắp xếp (default: id)
     * @param sortDir Hướng sắp xếp (default: asc)
     * @return Page chứa ExamDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<ExamDTO>> getExamsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction sortDirection = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<ExamDTO> exams = examService.getExamsPage(pageable);

        return ResponseEntity.ok(exams);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một bài thi theo ID
     * GET /api/exams/{id}
     * @param id ID của bài thi
     * @return ExamDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ExamDTO> getExamById(@PathVariable Long id) {
        ExamDTO examDTO = examService.getExamById(id);
        return ResponseEntity.ok(examDTO);
    }

    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin một bài thi
     * PUT /api/exams/{id}
     * @param id ID của bài thi cần cập nhật
     * @param request Thông tin cập nhật
     * @return ExamDTO sau khi cập nhật
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ExamDTO> updateExam(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExamRequest request) {
        ExamDTO examDTO = examService.updateExam(id, request);
        return ResponseEntity.ok(examDTO);
    }

    /** ------------------------------------------
     * Mục đích: Publish một bài thi (cho phép học sinh tham gia)
     * PUT /api/exams/{id}/publish
     * @param id ID của bài thi
     * @return ResponseEntity với status 200 OK
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> publishExam(@PathVariable Long id) {
        examService.publishExam(id);
        return ResponseEntity.ok().build();
    }

    /** ------------------------------------------
     * Mục đích: Unpublish một bài thi (hủy publish)
     * PUT /api/exams/{id}/unpublish
     * @param id ID của bài thi
     * @return ResponseEntity với status 200 OK
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> unpublishExam(@PathVariable Long id) {
        examService.unpublishExam(id);
        return ResponseEntity.ok().build();
    }

    /** ------------------------------------------
     * Mục đích: Xóa mềm một bài thi
     * DELETE /api/exams/{id}
     * @param id ID của bài thi cần xóa
     * @return ResponseEntity với status 204 No Content
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    /** ------------------------------------------
     * Mục đích: Tìm kiếm bài thi theo từ khóa
     * GET /api/exams/search?keyword=midterm&page=0&size=10
     * @param keyword Từ khóa tìm kiếm
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @return Page chứa ExamDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<ExamDTO>> searchExams(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ExamDTO> exams = examService.searchExams(keyword, pageable);

        return ResponseEntity.ok(exams);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách bài thi theo subject class
     * GET /api/exams/subject-class/{subjectClassId}
     * @param subjectClassId ID của subject class
     * @return Danh sách ExamDTO thuộc subject class
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @GetMapping("/subject-class/{subjectClassId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ExamDTO>> getExamsBySubjectClass(@PathVariable Long subjectClassId) {
        List<ExamDTO> exams = examService.getExamsBySubjectClass(subjectClassId);
        return ResponseEntity.ok(exams);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách bài thi theo người tạo
     * GET /api/exams/creator/{creatorId}
     * @param creatorId ID người tạo
     * @return Danh sách ExamDTO do người này tạo
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @GetMapping("/creator/{creatorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ExamDTO>> getExamsByCreator(@PathVariable Long creatorId) {
        List<ExamDTO> exams = examService.getExamsByCreator(creatorId);
        return ResponseEntity.ok(exams);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách bài thi đang diễn ra
     * GET /api/exams/ongoing
     * @return Danh sách ExamDTO đang diễn ra
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @GetMapping("/ongoing")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<ExamDTO>> getOngoingExams() {
        List<ExamDTO> exams = examService.getOngoingExams();
        return ResponseEntity.ok(exams);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách bài thi sắp diễn ra với phân trang
     * GET /api/exams/upcoming?page=0&size=10
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @return Page chứa ExamDTO sắp diễn ra
     * @author NVMANH with Cline
     * @created 18/11/2025 23:33
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<ExamDTO>> getUpcomingExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ExamDTO> exams = examService.getUpcomingExams(pageable);

        return ResponseEntity.ok(exams);
    }
}

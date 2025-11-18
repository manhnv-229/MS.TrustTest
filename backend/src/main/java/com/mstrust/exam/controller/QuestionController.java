package com.mstrust.exam.controller;

import com.mstrust.exam.dto.CreateQuestionRequest;
import com.mstrust.exam.dto.QuestionDTO;
import com.mstrust.exam.dto.UpdateQuestionRequest;
import com.mstrust.exam.entity.Difficulty;
import com.mstrust.exam.entity.QuestionType;
import com.mstrust.exam.service.QuestionService;
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
 * Mục đích: REST Controller cho Question Bank APIs
 * Cung cấp các endpoint để quản lý ngân hàng câu hỏi
 * @author NVMANH with Cline
 * @created 18/11/2025 23:32
 */
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /** ------------------------------------------
     * Mục đích: Tạo mới một câu hỏi trong Question Bank
     * POST /api/questions
     * @param request Thông tin câu hỏi cần tạo
     * @return QuestionDTO với status 201 Created
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<QuestionDTO> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        QuestionDTO questionDTO = questionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(questionDTO);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả các câu hỏi (không phân trang)
     * GET /api/questions
     * @return Danh sách QuestionDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        List<QuestionDTO> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách các câu hỏi với phân trang
     * GET /api/questions/page?page=0&size=10&sortBy=id&sortDir=asc
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @param sortBy Trường sắp xếp (default: id)
     * @param sortDir Hướng sắp xếp (default: asc)
     * @return Page chứa QuestionDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<QuestionDTO>> getQuestionsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction sortDirection = sortDir.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<QuestionDTO> questions = questionService.getQuestionsPage(pageable);

        return ResponseEntity.ok(questions);
    }

    /** ------------------------------------------
     * Mục đích: Lấy thông tin một câu hỏi theo ID
     * GET /api/questions/{id}
     * @param id ID của câu hỏi
     * @return QuestionDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable Long id) {
        QuestionDTO questionDTO = questionService.getQuestionById(id);
        return ResponseEntity.ok(questionDTO);
    }

    /** ------------------------------------------
     * Mục đích: Cập nhật thông tin một câu hỏi
     * PUT /api/questions/{id}
     * @param id ID của câu hỏi cần cập nhật
     * @param request Thông tin cập nhật
     * @return QuestionDTO sau khi cập nhật
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuestionRequest request) {
        QuestionDTO questionDTO = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(questionDTO);
    }

    /** ------------------------------------------
     * Mục đích: Xóa mềm một câu hỏi
     * DELETE /api/questions/{id}
     * @param id ID của câu hỏi cần xóa
     * @return ResponseEntity với status 204 No Content
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    /** ------------------------------------------
     * Mục đích: Tìm kiếm câu hỏi theo từ khóa
     * GET /api/questions/search?keyword=java&page=0&size=10
     * @param keyword Từ khóa tìm kiếm
     * @param page Số trang (default: 0)
     * @param size Kích thước trang (default: 10)
     * @return Page chứa QuestionDTO
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<QuestionDTO>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<QuestionDTO> questions = questionService.searchQuestions(keyword, pageable);

        return ResponseEntity.ok(questions);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách câu hỏi theo môn học
     * GET /api/questions/subject/{subjectId}
     * @param subjectId ID của môn học
     * @return Danh sách QuestionDTO thuộc môn học
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @GetMapping("/subject/{subjectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsBySubject(@PathVariable Long subjectId) {
        List<QuestionDTO> questions = questionService.getQuestionsBySubject(subjectId);
        return ResponseEntity.ok(questions);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách câu hỏi theo loại
     * GET /api/questions/type/{type}
     * @param type Loại câu hỏi (MULTIPLE_CHOICE, ESSAY, etc.)
     * @return Danh sách QuestionDTO theo loại
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByType(@PathVariable QuestionType type) {
        List<QuestionDTO> questions = questionService.getQuestionsByType(type);
        return ResponseEntity.ok(questions);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách câu hỏi theo độ khó
     * GET /api/questions/difficulty/{difficulty}
     * @param difficulty Độ khó (EASY, MEDIUM, HARD)
     * @return Danh sách QuestionDTO theo độ khó
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @GetMapping("/difficulty/{difficulty}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByDifficulty(@PathVariable Difficulty difficulty) {
        List<QuestionDTO> questions = questionService.getQuestionsByDifficulty(difficulty);
        return ResponseEntity.ok(questions);
    }

    /** ------------------------------------------
     * Mục đích: Lấy danh sách câu hỏi theo người tạo
     * GET /api/questions/creator/{creatorId}
     * @param creatorId ID người tạo
     * @return Danh sách QuestionDTO do người này tạo
     * @author NVMANH with Cline
     * @created 18/11/2025 23:32
     */
    @GetMapping("/creator/{creatorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPT_MANAGER', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByCreator(@PathVariable Long creatorId) {
        List<QuestionDTO> questions = questionService.getQuestionsByCreator(creatorId);
        return ResponseEntity.ok(questions);
    }
}

package com.mstrust.exam.controller;

import com.mstrust.exam.dto.CreateQuestionBankRequest;
import com.mstrust.exam.dto.QuestionBankDTO;
import com.mstrust.exam.dto.UpdateQuestionBankRequest;
import com.mstrust.exam.entity.Difficulty;
import com.mstrust.exam.entity.QuestionType;
import com.mstrust.exam.service.QuestionBankService;
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

/**
 * Controller xử lý Question Bank APIs (Phase 4)
 * CreatedBy: K24DTCN210-NVMANH (19/11/2025 02:09)
 */
@RestController
@RequestMapping("/question-banks")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    /* ---------------------------------------------------
     * Tạo mới câu hỏi vào Question Bank
     * POST /api/question-bank
     * @param request Thông tin câu hỏi
     * @return QuestionBankDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 02:09)
     * --------------------------------------------------- */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<QuestionBankDTO> createQuestion(@Valid @RequestBody CreateQuestionBankRequest request) {
        QuestionBankDTO created = questionBankService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* ---------------------------------------------------
     * Lấy danh sách câu hỏi với filter và phân trang
     * GET /api/question-bank
     * @param subjectId ID môn học (optional)
     * @param difficulty Độ khó (optional)
     * @param type Loại câu hỏi (optional)
     * @param keyword Từ khóa (optional)
     * @param page Trang (default 0)
     * @param size Kích thước (default 20)
     * @param sort Sắp xếp (default createdAt,desc)
     * @return Page<QuestionBankDTO>
     * @author: K24DTCN210-NVMANH (19/11/2025 02:09)
     * --------------------------------------------------- */
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Page<QuestionBankDTO>> getQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<QuestionBankDTO> questions = questionBankService.filterQuestions(
                subjectId, difficulty, type, keyword, pageable
        );
        return ResponseEntity.ok(questions);
    }

    /* ---------------------------------------------------
     * Lấy chi tiết một câu hỏi
     * GET /api/question-bank/{id}
     * @param id ID câu hỏi
     * @return QuestionBankDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 02:09)
     * --------------------------------------------------- */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<QuestionBankDTO> getQuestionById(@PathVariable Long id) {
        QuestionBankDTO question = questionBankService.getQuestionById(id);
        return ResponseEntity.ok(question);
    }

    /* ---------------------------------------------------
     * Cập nhật câu hỏi
     * PUT /api/question-bank/{id}
     * @param id ID câu hỏi
     * @param request Thông tin cập nhật
     * @return QuestionBankDTO
     * @author: K24DTCN210-NVMANH (19/11/2025 02:09)
     * --------------------------------------------------- */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<QuestionBankDTO> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuestionBankRequest request
    ) {
        QuestionBankDTO updated = questionBankService.updateQuestion(id, request);
        return ResponseEntity.ok(updated);
    }

    /* ---------------------------------------------------
     * Xóa câu hỏi (soft delete)
     * DELETE /api/question-bank/{id}
     * @param id ID câu hỏi
     * @return Success message
     * @author: K24DTCN210-NVMANH (19/11/2025 02:09)
     * --------------------------------------------------- */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        questionBankService.deleteQuestion(id);
        return ResponseEntity.ok("Xóa câu hỏi thành công");
    }

    /* ---------------------------------------------------
     * Lấy thống kê câu hỏi theo môn học
     * GET /api/question-bank/statistics/{subjectId}
     * @param subjectId ID môn học
     * @return Thống kê
     * @author: K24DTCN210-NVMANH (19/11/2025 02:09)
     * --------------------------------------------------- */
    @GetMapping("/statistics/{subjectId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<Object> getStatistics(@PathVariable Long subjectId) {
        Object statistics = questionBankService.getStatistics(subjectId);
        return ResponseEntity.ok(statistics);
    }
}

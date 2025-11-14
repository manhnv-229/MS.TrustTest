package com.mstrust.exam.repository;

import com.mstrust.exam.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* ---------------------------------------------------
 * Repository interface cho Subject entity
 * Xử lý các thao tác database với bảng subjects
 * @author: K24DTCN210-NVMANH (14/11/2025 14:01)
 * --------------------------------------------------- */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /* ---------------------------------------------------
     * Tìm môn học theo mã môn học
     * @param code Mã môn học
     * @returns Optional chứa Subject nếu tìm thấy
     * @author: K24DTCN210-NVMANH (14/11/2025 14:01)
     * --------------------------------------------------- */
    Optional<Subject> findByCode(String code);

    /* ---------------------------------------------------
     * Tìm môn học theo mã và chưa bị xóa
     * @param code Mã môn học
     * @returns Optional chứa Subject nếu tìm thấy
     * @author: K24DTCN210-NVMANH (14/11/2025 14:01)
     * --------------------------------------------------- */
    Optional<Subject> findByCodeAndDeletedAtIsNull(String code);

    /* ---------------------------------------------------
     * Tìm tất cả môn học chưa bị xóa
     * @returns Danh sách Subject
     * @author: K24DTCN210-NVMANH (14/11/2025 14:01)
     * --------------------------------------------------- */
    List<Subject> findByDeletedAtIsNull();

    /* ---------------------------------------------------
     * Tìm môn học theo Department và chưa bị xóa
     * @param departmentId ID của Department
     * @returns Danh sách Subject
     * @author: K24DTCN210-NVMANH (14/11/2025 14:01)
     * --------------------------------------------------- */
    List<Subject> findByDepartmentIdAndDeletedAtIsNull(Long departmentId);

    /* ---------------------------------------------------
     * Tìm môn học theo Department với phân trang
     * @param departmentId ID của Department
     * @param pageable Thông tin phân trang
     * @returns Page chứa Subject
     * @author: K24DTCN210-NVMANH (14/11/2025 14:01)
     * --------------------------------------------------- */
    Page<Subject> findByDepartmentIdAndDeletedAtIsNull(Long departmentId, Pageable pageable);

    /* ---------------------------------------------------
     * Tìm môn học chưa xóa với phân trang
     * @param pageable Thông tin phân trang
     * @returns Page chứa Subject
     * @author: K24DTCN210-NVMANH (14/11/2025 14:01)
     * --------------------------------------------------- */
    Page<Subject> findByDeletedAtIsNull(Pageable pageable);

    /* ---------------------------------------------------
     * Kiểm tra mã môn học đã tồn tại chưa (không tính môn đã xóa)
     * @param code Mã môn học
     * @returns true nếu đã tồn tại
     * @author: K24DTCN210-NVMANH (14/11/2025 14:01)
     * --------------------------------------------------- */
    boolean existsByCodeAndDeletedAtIsNull(String code);

    /* ---------------------------------------------------
     * Tìm kiếm môn học theo tên hoặc mã (không phân biệt hoa thường)
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @returns Page chứa Subject
     * @author: K24DTCN210-NVMANH (14/11/2025 14:01)
     * --------------------------------------------------- */
    @Query("SELECT s FROM Subject s WHERE s.deletedAt IS NULL AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Subject> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

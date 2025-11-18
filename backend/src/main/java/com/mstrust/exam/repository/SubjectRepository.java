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

/** ------------------------------------------
 * Mục đích: Repository interface cho entity Subject
 * Quản lý truy vấn cho bảng subjects
 * @author NVMANH with Cline
 * @created 15/11/2025 14:14
 */
@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /** ------------------------------------------
     * Mục đích: Tìm subject theo mã môn học (chưa bị xóa)
     * @param code Mã môn học
     * @return Optional chứa subject nếu tìm thấy
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     */
    Optional<Subject> findBySubjectCodeAndDeletedAtIsNull(String subjectCode);

    /** ------------------------------------------
     * Mục đích: Tìm subject theo ID (chưa bị xóa)
     * @param id ID của subject
     * @return Optional chứa subject nếu tìm thấy
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     */
    Optional<Subject> findByIdAndDeletedAtIsNull(Long id);

    /** ------------------------------------------
     * Mục đích: Kiểm tra mã môn học đã tồn tại chưa (chưa bị xóa)
     * @param code Mã môn học cần kiểm tra
     * @return true nếu đã tồn tại
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     */
    boolean existsBySubjectCodeAndDeletedAtIsNull(String subjectCode);

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả subjects (chưa bị xóa)
     * @return List các subject chưa bị xóa
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     */
    List<Subject> findByDeletedAtIsNull();

    /** ------------------------------------------
     * Mục đích: Lấy danh sách subjects với phân trang (chưa bị xóa)
     * @param pageable Thông tin phân trang
     * @return Page chứa các subject
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     */
    Page<Subject> findByDeletedAtIsNull(Pageable pageable);

    /** ------------------------------------------
     * Mục đích: Lấy danh sách subjects theo department (chưa bị xóa)
     * @param departmentId ID của khoa
     * @return List các subject thuộc khoa
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     */
    @Query("SELECT s FROM Subject s WHERE s.department.id = :departmentId AND s.deletedAt IS NULL")
    List<Subject> findByDepartmentId(@Param("departmentId") Long departmentId);

    /** ------------------------------------------
     * Mục đích: Tìm kiếm subjects theo từ khóa (mã hoặc tên)
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @return Page chứa kết quả tìm kiếm
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     */
    @Query("SELECT s FROM Subject s WHERE s.deletedAt IS NULL AND " +
           "(LOWER(s.subjectCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Subject> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /** ------------------------------------------
     * Mục đích: Đếm số lượng subject classes đang active cho một subject
     * @param subjectId ID của subject
     * @return Số lượng subject classes
     * @author NVMANH with Cline
     * @created 15/11/2025 14:14
     */
    @Query("SELECT COUNT(sc) FROM SubjectClass sc WHERE sc.subject.id = :subjectId AND sc.deletedAt IS NULL")
    Long countActiveSubjectClassesBySubjectId(@Param("subjectId") Long subjectId);
}

package com.mstrust.exam.repository;

import com.mstrust.exam.entity.ClassEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** ------------------------------------------
 * Mục đích: Repository interface cho entity ClassEntity
 * Quản lý truy vấn cho bảng classes
 * @author NVMANH with Cline
 * @created 13/11/2025 14:53
 * @updated 15/11/2025 13:58
 */
@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    /** ------------------------------------------
     * Mục đích: Tìm class theo mã lớp (chưa bị xóa)
     * @param classCode Mã lớp
     * @return Optional chứa class nếu tìm thấy
     * @author NVMANH with Cline
     * @created 13/11/2025 14:53
     */
    Optional<ClassEntity> findByClassCodeAndDeletedAtIsNull(String classCode);

    /** ------------------------------------------
     * Mục đích: Tìm class theo ID (chưa bị xóa)
     * @param id ID của class
     * @return Optional chứa class nếu tìm thấy
     * @author NVMANH with Cline
     * @created 15/11/2025 13:58
     */
    Optional<ClassEntity> findByIdAndDeletedAtIsNull(Long id);

    /** ------------------------------------------
     * Mục đích: Kiểm tra mã lớp đã tồn tại chưa (chưa bị xóa)
     * @param classCode Mã lớp cần kiểm tra
     * @return true nếu đã tồn tại
     * @author NVMANH with Cline
     * @created 13/11/2025 14:53
     */
    boolean existsByClassCodeAndDeletedAtIsNull(String classCode);

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả classes (chưa bị xóa)
     * @return List các class chưa bị xóa
     * @author NVMANH with Cline
     * @created 15/11/2025 13:58
     */
    List<ClassEntity> findByDeletedAtIsNull();

    /** ------------------------------------------
     * Mục đích: Lấy danh sách classes với phân trang (chưa bị xóa)
     * @param pageable Thông tin phân trang
     * @return Page chứa các class
     * @author NVMANH with Cline
     * @created 15/11/2025 13:58
     */
    Page<ClassEntity> findByDeletedAtIsNull(Pageable pageable);

    /** ------------------------------------------
     * Mục đích: Lấy danh sách classes theo department (chưa bị xóa)
     * @param departmentId ID của khoa
     * @return List các class thuộc khoa
     * @author NVMANH with Cline
     * @created 13/11/2025 14:53
     */
    @Query("SELECT c FROM ClassEntity c WHERE c.department.id = :departmentId AND c.deletedAt IS NULL")
    List<ClassEntity> findByDepartmentId(@Param("departmentId") Long departmentId);

    /** ------------------------------------------
     * Mục đích: Lấy danh sách classes theo năm học (chưa bị xóa)
     * @param academicYear Năm học (ví dụ: 2023-2024)
     * @return List các class trong năm học
     * @author NVMANH with Cline
     * @created 15/11/2025 13:58
     */
    @Query("SELECT c FROM ClassEntity c WHERE c.academicYear = :academicYear AND c.deletedAt IS NULL")
    List<ClassEntity> findByAcademicYear(@Param("academicYear") String academicYear);

    /** ------------------------------------------
     * Mục đích: Lấy danh sách tất cả classes đang hoạt động (chưa bị xóa)
     * @return List các class active
     * @author NVMANH with Cline
     * @created 13/11/2025 14:53
     */
    @Query("SELECT c FROM ClassEntity c WHERE c.deletedAt IS NULL AND c.isActive = true")
    List<ClassEntity> findByIsActiveTrueAndDeletedAtIsNull();

    /** ------------------------------------------
     * Mục đích: Tìm kiếm classes theo từ khóa (mã hoặc tên)
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @return Page chứa kết quả tìm kiếm
     * @author NVMANH with Cline
     * @created 15/11/2025 13:58
     */
    @Query("SELECT c FROM ClassEntity c WHERE c.deletedAt IS NULL AND " +
           "(LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.className) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ClassEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /** ------------------------------------------
     * Mục đích: Đếm số lượng students trong một class
     * @param classId ID của class
     * @return Số lượng students
     * @author NVMANH with Cline
     * @created 15/11/2025 13:58
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.classEntity.id = :classId AND u.deletedAt IS NULL")
    Long countStudentsByClassId(@Param("classId") Long classId);
}

package com.mstrust.exam.repository;

import com.mstrust.exam.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* ---------------------------------------------------
 * Repository interface cho entity Department
 * Quản lý truy vấn cho bảng departments
 * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
 * EditBy: K24DTCN210-NVMANH (14/11/2025 14:12) - Thêm methods cho Phase 3
 * --------------------------------------------------- */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /* ---------------------------------------------------
     * Tìm department theo mã khoa
     * @param departmentCode Mã khoa
     * @returns Optional chứa department nếu tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    Optional<Department> findByDepartmentCode(String departmentCode);

    /* ---------------------------------------------------
     * Tìm department theo mã khoa (chưa bị xóa)
     * @param departmentCode Mã khoa
     * @returns Optional chứa department nếu tìm thấy
     * @author: K24DTCN210-NVMANH (14/11/2025 14:12)
     * --------------------------------------------------- */
    Optional<Department> findByDepartmentCodeAndDeletedAtIsNull(String departmentCode);

    /* ---------------------------------------------------
     * Tìm department theo ID (chưa bị xóa)
     * @param id ID của department
     * @returns Optional chứa department nếu tìm thấy
     * @author: K24DTCN210-NVMANH (14/11/2025 14:12)
     * --------------------------------------------------- */
    Optional<Department> findByIdAndDeletedAtIsNull(Long id);

    /* ---------------------------------------------------
     * Kiểm tra mã khoa đã tồn tại chưa
     * @param departmentCode Mã khoa cần kiểm tra
     * @returns true nếu đã tồn tại
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    boolean existsByDepartmentCode(String departmentCode);

    /* ---------------------------------------------------
     * Kiểm tra mã khoa đã tồn tại chưa (không tính đã xóa)
     * @param departmentCode Mã khoa cần kiểm tra
     * @returns true nếu đã tồn tại
     * @author: K24DTCN210-NVMANH (14/11/2025 14:12)
     * --------------------------------------------------- */
    boolean existsByDepartmentCodeAndDeletedAtIsNull(String departmentCode);

    /* ---------------------------------------------------
     * Lấy danh sách tất cả departments chưa bị xóa
     * @returns List các department
     * @author: K24DTCN210-NVMANH (14/11/2025 14:12)
     * --------------------------------------------------- */
    List<Department> findByDeletedAtIsNull();

    /* ---------------------------------------------------
     * Lấy danh sách departments chưa bị xóa với phân trang
     * @param pageable Thông tin phân trang
     * @returns Page chứa departments
     * @author: K24DTCN210-NVMANH (14/11/2025 14:12)
     * --------------------------------------------------- */
    Page<Department> findByDeletedAtIsNull(Pageable pageable);

    /* ---------------------------------------------------
     * Lấy danh sách departments đang hoạt động (chưa bị xóa)
     * @returns List các department active
     * @author: K24DTCN210-NVMANH (14/11/2025 14:12)
     * --------------------------------------------------- */
    List<Department> findByIsActiveTrueAndDeletedAtIsNull();

    /* ---------------------------------------------------
     * Tìm kiếm department theo từ khóa (tên hoặc mã)
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @returns Page chứa departments
     * @author: K24DTCN210-NVMANH (14/11/2025 14:12)
     * --------------------------------------------------- */
    @Query("SELECT d FROM Department d WHERE d.deletedAt IS NULL AND " +
           "(LOWER(d.departmentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.departmentCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Department> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /* ---------------------------------------------------
     * Lấy danh sách tất cả departments active (legacy method từ Phase 2)
     * @returns List các department chưa bị xóa và đang active
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    @Query("SELECT d FROM Department d WHERE d.deletedAt IS NULL AND d.isActive = true")
    List<Department> findAllActive();
}

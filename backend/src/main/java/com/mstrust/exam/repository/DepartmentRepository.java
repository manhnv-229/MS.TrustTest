package com.mstrust.exam.repository;

import com.mstrust.exam.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* ---------------------------------------------------
 * Repository interface cho entity Department
 * Quản lý truy vấn cho bảng departments
 * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
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
     * Kiểm tra mã khoa đã tồn tại chưa
     * @param departmentCode Mã khoa cần kiểm tra
     * @returns true nếu đã tồn tại
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    boolean existsByDepartmentCode(String departmentCode);

    /* ---------------------------------------------------
     * Lấy danh sách tất cả departments active
     * @returns List các department chưa bị xóa
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    @Query("SELECT d FROM Department d WHERE d.deletedAt IS NULL AND d.isActive = true")
    List<Department> findAllActive();
}

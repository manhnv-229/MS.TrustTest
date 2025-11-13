package com.mstrust.exam.repository;

import com.mstrust.exam.entity.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* ---------------------------------------------------
 * Repository interface cho entity ClassEntity
 * Quản lý truy vấn cho bảng classes
 * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
 * --------------------------------------------------- */
@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    /* ---------------------------------------------------
     * Tìm class theo mã lớp
     * @param classCode Mã lớp
     * @returns Optional chứa class nếu tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    Optional<ClassEntity> findByClassCode(String classCode);

    /* ---------------------------------------------------
     * Kiểm tra mã lớp đã tồn tại chưa
     * @param classCode Mã lớp cần kiểm tra
     * @returns true nếu đã tồn tại
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    boolean existsByClassCode(String classCode);

    /* ---------------------------------------------------
     * Lấy danh sách classes theo department
     * @param departmentId ID của khoa
     * @returns List các class thuộc khoa
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    @Query("SELECT c FROM ClassEntity c WHERE c.department.id = :departmentId AND c.deletedAt IS NULL")
    List<ClassEntity> findByDepartmentId(@Param("departmentId") Long departmentId);

    /* ---------------------------------------------------
     * Lấy danh sách tất cả classes active
     * @returns List các class chưa bị xóa
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    @Query("SELECT c FROM ClassEntity c WHERE c.deletedAt IS NULL AND c.isActive = true")
    List<ClassEntity> findAllActive();
}

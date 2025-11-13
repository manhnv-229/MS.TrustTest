package com.mstrust.exam.repository;

import com.mstrust.exam.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/* ---------------------------------------------------
 * Repository interface cho entity Role
 * Cung cấp các phương thức truy vấn database cho bảng roles
 * @author: K24DTCN210-NVMANH (13/11/2025 14:52)
 * --------------------------------------------------- */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /* ---------------------------------------------------
     * Tìm role theo tên role
     * @param roleName Tên role (VD: STUDENT, TEACHER, ADMIN)
     * @returns Optional chứa role nếu tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 14:52)
     * --------------------------------------------------- */
    Optional<Role> findByRoleName(String roleName);

    /* ---------------------------------------------------
     * Kiểm tra role có tồn tại theo tên không
     * @param roleName Tên role cần kiểm tra
     * @returns true nếu role tồn tại
     * @author: K24DTCN210-NVMANH (13/11/2025 14:52)
     * --------------------------------------------------- */
    boolean existsByRoleName(String roleName);

    /* ---------------------------------------------------
     * Tìm role theo tên và chưa bị xóa mềm
     * @param roleName Tên role
     * @returns Optional chứa role active
     * @author: K24DTCN210-NVMANH (13/11/2025 14:52)
     * --------------------------------------------------- */
    @Query("SELECT r FROM Role r WHERE r.roleName = :roleName AND r.deletedAt IS NULL")
    Optional<Role> findActiveByRoleName(String roleName);
}

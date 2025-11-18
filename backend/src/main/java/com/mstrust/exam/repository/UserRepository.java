package com.mstrust.exam.repository;

import com.mstrust.exam.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/* ---------------------------------------------------
 * Repository interface cho entity User
 * Hỗ trợ multi-login: student_code, email, phone_number
 * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
 * --------------------------------------------------- */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /* ---------------------------------------------------
     * Tìm user theo mã sinh viên
     * @param studentCode Mã sinh viên
     * @returns Optional chứa user nếu tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    Optional<User> findByStudentCode(String studentCode);

    /* ---------------------------------------------------
     * Tìm user theo email
     * @param email Email của user
     * @returns Optional chứa user nếu tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    Optional<User> findByEmail(String email);

    /* ---------------------------------------------------
     * Tìm user theo số điện thoại
     * @param phoneNumber Số điện thoại
     * @returns Optional chứa user nếu tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /* ---------------------------------------------------
     * Tìm user theo username (có thể là student_code, email hoặc phone)
     * Dùng cho login multi-method
     * @param username Username để login
     * @returns Optional chứa user nếu tìm thấy
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    @Query("SELECT u FROM User u WHERE (u.studentCode = :username OR u.email = :username OR u.phoneNumber = :username) AND u.deletedAt IS NULL")
    Optional<User> findByUsername(@Param("username") String username);

    /* ---------------------------------------------------
     * Kiểm tra student code đã tồn tại chưa
     * @param studentCode Mã sinh viên cần kiểm tra
     * @returns true nếu đã tồn tại
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    boolean existsByStudentCode(String studentCode);

    /* ---------------------------------------------------
     * Kiểm tra email đã tồn tại chưa
     * @param email Email cần kiểm tra
     * @returns true nếu đã tồn tại
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    boolean existsByEmail(String email);

    /* ---------------------------------------------------
     * Kiểm tra phone number đã tồn tại chưa
     * @param phoneNumber Số điện thoại cần kiểm tra
     * @returns true nếu đã tồn tại
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    boolean existsByPhoneNumber(String phoneNumber);

    /* ---------------------------------------------------
     * Tìm user active theo student code
     * @param studentCode Mã sinh viên
     * @returns Optional chứa user active
     * @author: K24DTCN210-NVMANH (13/11/2025 14:53)
     * --------------------------------------------------- */
    @Query("SELECT u FROM User u WHERE u.studentCode = :studentCode AND u.deletedAt IS NULL AND u.isActive = true")
    Optional<User> findActiveByStudentCode(@Param("studentCode") String studentCode);

    /* ---------------------------------------------------
     * Update last login time và reset failed login attempts
     * @param userId ID của user
     * @author: K24DTCN210-NVMANH (14/11/2025 13:43)
     * --------------------------------------------------- */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId);

    /* ---------------------------------------------------
     * Tìm users theo role name (không bao gồm deleted)
     * @param roleName Tên role cần tìm
     * @returns List các user có role đó
     * @author NVMANH with Cline (15/11/2025 14:59)
     * --------------------------------------------------- */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName AND u.deletedAt IS NULL")
    java.util.List<User> findByRoleName(@Param("roleName") String roleName);

    /* ---------------------------------------------------
     * Tìm users theo department (không bao gồm deleted)
     * @param departmentId ID của department
     * @returns List các user trong department đó
     * @author NVMANH with Cline (15/11/2025 14:59)
     * --------------------------------------------------- */
    @Query("SELECT u FROM User u WHERE u.department.id = :departmentId AND u.deletedAt IS NULL")
    java.util.List<User> findByDepartmentId(@Param("departmentId") Long departmentId);

    /* ---------------------------------------------------
     * Tìm users theo class (không bao gồm deleted)
     * @param classId ID của class
     * @returns List các user trong class đó
     * @author NVMANH with Cline (15/11/2025 14:59)
     * --------------------------------------------------- */
    @Query("SELECT u FROM User u WHERE u.classEntity.id = :classId AND u.deletedAt IS NULL")
    java.util.List<User> findByClassId(@Param("classId") Long classId);

    /* ---------------------------------------------------
     * Tìm users theo active status (không bao gồm deleted)
     * @param isActive Active status cần tìm
     * @returns List các user có status tương ứng
     * @author NVMANH with Cline (15/11/2025 14:59)
     * --------------------------------------------------- */
    @Query("SELECT u FROM User u WHERE u.isActive = :isActive AND u.deletedAt IS NULL")
    java.util.List<User> findByIsActive(@Param("isActive") Boolean isActive);

    /* ---------------------------------------------------
     * Advanced search users với multiple criteria
     * @param keyword Keyword search trong fullName, email, studentCode
     * @param roleName Filter theo role name (nullable)
     * @param departmentId Filter theo department (nullable)
     * @param classId Filter theo class (nullable)
     * @param isActive Filter theo active status (nullable)
     * @param gender Filter theo gender (nullable)
     * @returns List các user match criteria
     * @author NVMANH with Cline (15/11/2025 15:00)
     * --------------------------------------------------- */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "u.deletedAt IS NULL AND " +
           "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:roleName IS NULL OR r.roleName = :roleName) AND " +
           "(:departmentId IS NULL OR u.department.id = :departmentId) AND " +
           "(:classId IS NULL OR u.classEntity.id = :classId) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) AND " +
           "(:gender IS NULL OR CAST(u.gender AS string) = :gender)")
    java.util.List<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("roleName") String roleName,
            @Param("departmentId") Long departmentId,
            @Param("classId") Long classId,
            @Param("isActive") Boolean isActive,
            @Param("gender") String gender
    );

    /* ---------------------------------------------------
     * Đếm total users (không bao gồm deleted)
     * @returns Số lượng users
     * @author NVMANH with Cline (15/11/2025 15:00)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    Long countActiveUsers();

    /* ---------------------------------------------------
     * Đếm users theo active status (không bao gồm deleted)
     * @param isActive Active status
     * @returns Số lượng users
     * @author NVMANH with Cline (15/11/2025 15:00)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = :isActive AND u.deletedAt IS NULL")
    Long countByIsActive(@Param("isActive") Boolean isActive);

    /* ---------------------------------------------------
     * Đếm deleted users
     * @returns Số lượng users đã bị xóa
     * @author NVMANH with Cline (15/11/2025 15:00)
     * --------------------------------------------------- */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NOT NULL")
    Long countDeletedUsers();
}

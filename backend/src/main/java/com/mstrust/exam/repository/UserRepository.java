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
}

package com.mstrust.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** ------------------------------------------
 * Mục đích: Entity cho bảng subject_classes (Lớp học phần)
 * Quản lý thông tin lớp học phần, giáo viên phụ trách và danh sách sinh viên
 * @author NVMANH with Cline
 * @created 15/11/2025 14:25
 */
@Entity
@Table(name = "subject_classes")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectClass {
    
    /** ------------------------------------------
     * Primary key tự động tăng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** ------------------------------------------
     * Mã lớp học phần (unique, không được trùng)
     * Format: MATH101-2023-2024-1 (SubjectCode-Year-Semester)
     */
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;
    
    /** ------------------------------------------
     * Môn học của lớp này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    
    /** ------------------------------------------
     * Học kỳ (format: "YYYY-YYYY-N", ví dụ: "2023-2024-1")
     * N = 1 (học kỳ 1), 2 (học kỳ 2), 3 (học kỳ hè)
     */
    @Column(name = "semester", nullable = false, length = 20)
    private String semester;
    
    /** ------------------------------------------
     * Giáo viên phụ trách lớp này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    /** ------------------------------------------
     * Lịch học (có thể lưu dạng JSON hoặc text format)
     * Ví dụ: "Thứ 2: 7h-9h, Phòng A101; Thứ 4: 13h-15h, Phòng B202"
     */
    @Column(name = "schedule", length = 500)
    private String schedule;
    
    /** ------------------------------------------
     * Số lượng sinh viên tối đa
     */
    @Column(name = "max_students", nullable = false)
    @Builder.Default
    private Integer maxStudents = 50;
    
    /** ------------------------------------------
     * Danh sách sinh viên đăng ký (One-to-Many với SubjectClassStudent)
     */
    @OneToMany(mappedBy = "subjectClass", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SubjectClassStudent> enrolledStudents = new HashSet<>();
    
    /** ------------------------------------------
     * Version cho optimistic locking
     */
    @Version
    private Long version;
    
    /** ------------------------------------------
     * Thời điểm tạo record
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /** ------------------------------------------
     * Thời điểm cập nhật gần nhất
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /** ------------------------------------------
     * User ID của người tạo
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;
    
    /** ------------------------------------------
     * User ID của người cập nhật gần nhất
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;
    
    /** ------------------------------------------
     * Thời điểm xóa mềm (null = chưa xóa)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /** ------------------------------------------
     * Mục đích: Kiểm tra xem lớp học phần đã bị xóa mềm chưa
     * @return boolean - true nếu đã xóa
     * @author NVMANH with Cline
     * @created 15/11/2025 14:25
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    /** ------------------------------------------
     * Mục đích: Đánh dấu xóa mềm (soft delete)
     * @author NVMANH with Cline
     * @created 15/11/2025 14:25
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
    
    /** ------------------------------------------
     * Mục đích: Khôi phục lại sau khi xóa mềm
     * @author NVMANH with Cline
     * @created 15/11/2025 14:25
     */
    public void restore() {
        this.deletedAt = null;
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy số lượng sinh viên đang đăng ký (status = ENROLLED)
     * @return int - số lượng sinh viên enrolled
     * @author NVMANH with Cline
     * @created 15/11/2025 14:25
     */
    public int getEnrolledCount() {
        if (enrolledStudents == null) {
            return 0;
        }
        return (int) enrolledStudents.stream()
                .filter(e -> e.getStatus() == SubjectClassStudent.EnrollmentStatus.ENROLLED)
                .count();
    }
    
    /** ------------------------------------------
     * Mục đích: Kiểm tra xem lớp đã full chưa
     * @return boolean - true nếu đã full
     * @author NVMANH with Cline
     * @created 15/11/2025 14:25
     */
    public boolean isFull() {
        return getEnrolledCount() >= maxStudents;
    }
    
    /** ------------------------------------------
     * Mục đích: Lấy số chỗ còn trống
     * @return int - số chỗ trống
     * @author NVMANH with Cline
     * @created 15/11/2025 14:25
     */
    public int getAvailableSlots() {
        return Math.max(0, maxStudents - getEnrolledCount());
    }
}

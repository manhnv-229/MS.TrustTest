package com.mstrust.exam.repository;

import com.mstrust.exam.entity.SubjectClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** ------------------------------------------
 * Mục đích: Repository cho SubjectClass entity với custom queries
 * @author NVMANH with Cline
 * @created 15/11/2025 14:26
 */
@Repository
public interface SubjectClassRepository extends JpaRepository<SubjectClass, Long> {
    
    /** ------------------------------------------
     * Mục đích: Tìm lớp học phần theo code (chưa bị xóa mềm)
     * @param code - Mã lớp học phần
     * @return Optional<SubjectClass>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    Optional<SubjectClass> findByCodeAndDeletedAtIsNull(String code);
    
    /** ------------------------------------------
     * Mục đích: Lấy tất cả lớp học phần chưa bị xóa
     * @return List<SubjectClass>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    List<SubjectClass> findAllByDeletedAtIsNull();
    
    /** ------------------------------------------
     * Mục đích: Lấy tất cả lớp học phần với phân trang (chưa bị xóa)
     * @param pageable - Thông tin phân trang
     * @return Page<SubjectClass>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    Page<SubjectClass> findAllByDeletedAtIsNull(Pageable pageable);
    
    /** ------------------------------------------
     * Mục đích: Tìm lớp học phần theo subject ID (chưa bị xóa)
     * @param subjectId - ID của môn học
     * @return List<SubjectClass>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    @Query("SELECT sc FROM SubjectClass sc WHERE sc.subject.id = :subjectId AND sc.deletedAt IS NULL")
    List<SubjectClass> findBySubjectId(@Param("subjectId") Long subjectId);
    
    /** ------------------------------------------
     * Mục đích: Tìm lớp học phần theo semester (chưa bị xóa)
     * @param semester - Học kỳ (format: "YYYY-YYYY-N")
     * @return List<SubjectClass>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    @Query("SELECT sc FROM SubjectClass sc WHERE sc.semester = :semester AND sc.deletedAt IS NULL")
    List<SubjectClass> findBySemester(@Param("semester") String semester);
    
    /** ------------------------------------------
     * Mục đích: Tìm lớp học phần theo teacher ID (chưa bị xóa)
     * @param teacherId - ID của giáo viên
     * @return List<SubjectClass>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    @Query("SELECT sc FROM SubjectClass sc WHERE sc.teacher.id = :teacherId AND sc.deletedAt IS NULL")
    List<SubjectClass> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /** ------------------------------------------
     * Mục đích: Tìm lớp học phần theo subject và semester (chưa bị xóa)
     * @param subjectId - ID của môn học
     * @param semester - Học kỳ
     * @return List<SubjectClass>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    @Query("SELECT sc FROM SubjectClass sc WHERE sc.subject.id = :subjectId AND sc.semester = :semester AND sc.deletedAt IS NULL")
    List<SubjectClass> findBySubjectIdAndSemester(@Param("subjectId") Long subjectId, @Param("semester") String semester);
    
    /** ------------------------------------------
     * Mục đích: Tìm kiếm lớp học phần theo keyword (code, subject name, teacher name)
     * @param keyword - Từ khóa tìm kiếm
     * @param pageable - Thông tin phân trang
     * @return Page<SubjectClass>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    @Query("SELECT sc FROM SubjectClass sc " +
           "LEFT JOIN sc.subject s " +
           "LEFT JOIN sc.teacher t " +
           "WHERE sc.deletedAt IS NULL AND " +
           "(LOWER(sc.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.subjectName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SubjectClass> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /** ------------------------------------------
     * Mục đích: Kiểm tra xem student đã enroll vào lớp này chưa (status = ENROLLED)
     * @param subjectClassId - ID của lớp học phần
     * @param studentId - ID của sinh viên
     * @return boolean
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    @Query("SELECT COUNT(scs) > 0 FROM SubjectClassStudent scs " +
           "WHERE scs.subjectClass.id = :subjectClassId " +
           "AND scs.student.id = :studentId " +
           "AND scs.status = 'ENROLLED'")
    boolean isStudentEnrolled(@Param("subjectClassId") Long subjectClassId, @Param("studentId") Long studentId);
    
    /** ------------------------------------------
     * Mục đích: Kiểm tra student đã từng enroll môn học này trong semester này chưa
     * Dùng để tránh enroll duplicate subject trong cùng semester
     * @param subjectId - ID của môn học
     * @param semester - Học kỳ
     * @param studentId - ID của sinh viên
     * @return boolean
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    @Query("SELECT COUNT(scs) > 0 FROM SubjectClassStudent scs " +
           "WHERE scs.subjectClass.subject.id = :subjectId " +
           "AND scs.subjectClass.semester = :semester " +
           "AND scs.student.id = :studentId " +
           "AND scs.status = 'ENROLLED'")
    boolean hasStudentEnrolledInSubjectThisSemester(
            @Param("subjectId") Long subjectId,
            @Param("semester") String semester,
            @Param("studentId") Long studentId
    );
    
    /** ------------------------------------------
     * Mục đích: Kiểm tra xem code đã tồn tại chưa (dùng khi create/update)
     * @param code - Mã lớp học phần
     * @param id - ID của lớp (null khi create, not null khi update)
     * @return boolean
     * @author NVMANH with Cline
     * @created 15/11/2025 14:26
     */
    @Query("SELECT COUNT(sc) > 0 FROM SubjectClass sc " +
           "WHERE sc.code = :code AND sc.deletedAt IS NULL " +
           "AND (:id IS NULL OR sc.id != :id)")
    boolean existsByCodeExcludingId(@Param("code") String code, @Param("id") Long id);
}

package com.mstrust.exam.repository;

import com.mstrust.exam.entity.SubjectClassStudent;
import com.mstrust.exam.entity.SubjectClassStudentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** ------------------------------------------
 * Mục đích: Repository cho SubjectClassStudent entity (bảng trung gian)
 * @author NVMANH with Cline
 * @created 15/11/2025 14:27
 */
@Repository
public interface SubjectClassStudentRepository extends JpaRepository<SubjectClassStudent, SubjectClassStudentId> {
    
    /** ------------------------------------------
     * Mục đích: Lấy danh sách sinh viên đã enroll vào lớp (status = ENROLLED)
     * @param subjectClassId - ID của lớp học phần
     * @return List<SubjectClassStudent>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:27
     */
    @Query("SELECT scs FROM SubjectClassStudent scs " +
           "WHERE scs.subjectClass.id = :subjectClassId " +
           "AND scs.status = 'ENROLLED'")
    List<SubjectClassStudent> findEnrolledStudentsBySubjectClassId(@Param("subjectClassId") Long subjectClassId);
    
    /** ------------------------------------------
     * Mục đích: Lấy tất cả enrollment records của lớp (bao gồm cả DROPPED, COMPLETED)
     * @param subjectClassId - ID của lớp học phần
     * @return List<SubjectClassStudent>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:27
     */
    @Query("SELECT scs FROM SubjectClassStudent scs WHERE scs.subjectClass.id = :subjectClassId")
    List<SubjectClassStudent> findAllBySubjectClassId(@Param("subjectClassId") Long subjectClassId);
    
    /** ------------------------------------------
     * Mục đích: Lấy tất cả lớp học phần mà sinh viên đã enroll (status = ENROLLED)
     * @param studentId - ID của sinh viên
     * @return List<SubjectClassStudent>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:27
     */
    @Query("SELECT scs FROM SubjectClassStudent scs " +
           "WHERE scs.student.id = :studentId " +
           "AND scs.status = 'ENROLLED'")
    List<SubjectClassStudent> findEnrolledClassesByStudentId(@Param("studentId") Long studentId);
    
    /** ------------------------------------------
     * Mục đích: Tìm enrollment record cụ thể (subjectClassId + studentId)
     * @param subjectClassId - ID của lớp học phần
     * @param studentId - ID của sinh viên
     * @return Optional<SubjectClassStudent>
     * @author NVMANH with Cline
     * @created 15/11/2025 14:27
     */
    @Query("SELECT scs FROM SubjectClassStudent scs " +
           "WHERE scs.subjectClass.id = :subjectClassId " +
           "AND scs.student.id = :studentId")
    Optional<SubjectClassStudent> findBySubjectClassIdAndStudentId(
            @Param("subjectClassId") Long subjectClassId,
            @Param("studentId") Long studentId
    );
    
    /** ------------------------------------------
     * Mục đích: Đếm số lượng sinh viên ENROLLED trong lớp
     * @param subjectClassId - ID của lớp học phần
     * @return long - số lượng sinh viên
     * @author NVMANH with Cline
     * @created 15/11/2025 14:27
     */
    @Query("SELECT COUNT(scs) FROM SubjectClassStudent scs " +
           "WHERE scs.subjectClass.id = :subjectClassId " +
           "AND scs.status = 'ENROLLED'")
    long countEnrolledStudents(@Param("subjectClassId") Long subjectClassId);
}

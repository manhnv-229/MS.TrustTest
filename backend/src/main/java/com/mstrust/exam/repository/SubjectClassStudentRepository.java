package com.mstrust.exam.repository;

import com.mstrust.exam.entity.SubjectClassStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/* ---------------------------------------------------
 * Repository interface cho SubjectClassStudent entity
 * Xử lý các thao tác database với bảng subject_class_students
 * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
 * --------------------------------------------------- */
@Repository
public interface SubjectClassStudentRepository extends JpaRepository<SubjectClassStudent, Long> {

    /* ---------------------------------------------------
     * Tìm tất cả sinh viên đăng ký trong một lớp môn học
     * @param subjectClassId ID của lớp môn học
     * @returns Danh sách SubjectClassStudent
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    List<SubjectClassStudent> findBySubjectClassId(Long subjectClassId);

    /* ---------------------------------------------------
     * Tìm tất cả sinh viên đang học (ENROLLED) trong lớp môn học
     * @param subjectClassId ID của lớp môn học
     * @param status Trạng thái đăng ký
     * @returns Danh sách SubjectClassStudent
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    List<SubjectClassStudent> findBySubjectClassIdAndStatus(Long subjectClassId, SubjectClassStudent.EnrollmentStatus status);

    /* ---------------------------------------------------
     * Tìm tất cả lớp môn học mà sinh viên đã đăng ký
     * @param studentId ID của sinh viên
     * @returns Danh sách SubjectClassStudent
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    List<SubjectClassStudent> findByStudentId(Long studentId);

    /* ---------------------------------------------------
     * Tìm lớp môn học mà sinh viên đang học (ENROLLED)
     * @param studentId ID của sinh viên
     * @param status Trạng thái đăng ký
     * @returns Danh sách SubjectClassStudent
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    List<SubjectClassStudent> findByStudentIdAndStatus(Long studentId, SubjectClassStudent.EnrollmentStatus status);

    /* ---------------------------------------------------
     * Tìm đăng ký cụ thể của sinh viên trong lớp môn học
     * @param subjectClassId ID của lớp môn học
     * @param studentId ID của sinh viên
     * @returns Optional chứa SubjectClassStudent nếu tìm thấy
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    Optional<SubjectClassStudent> findBySubjectClassIdAndStudentId(Long subjectClassId, Long studentId);

    /* ---------------------------------------------------
     * Kiểm tra sinh viên đã đăng ký lớp môn học chưa
     * @param subjectClassId ID của lớp môn học
     * @param studentId ID của sinh viên
     * @returns true nếu đã đăng ký
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    boolean existsBySubjectClassIdAndStudentId(Long subjectClassId, Long studentId);

    /* ---------------------------------------------------
     * Kiểm tra sinh viên có đang học lớp môn học không (status = ENROLLED)
     * @param subjectClassId ID của lớp môn học
     * @param studentId ID của sinh viên
     * @param status Trạng thái đăng ký
     * @returns true nếu đang học
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    boolean existsBySubjectClassIdAndStudentIdAndStatus(Long subjectClassId, Long studentId, SubjectClassStudent.EnrollmentStatus status);

    /* ---------------------------------------------------
     * Đếm số lượng sinh viên đang học trong lớp môn học
     * @param subjectClassId ID của lớp môn học
     * @param status Trạng thái đăng ký
     * @returns Số lượng sinh viên
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    long countBySubjectClassIdAndStatus(Long subjectClassId, SubjectClassStudent.EnrollmentStatus status);

    /* ---------------------------------------------------
     * Xóa đăng ký của sinh viên khỏi lớp môn học
     * @param subjectClassId ID của lớp môn học
     * @param studentId ID của sinh viên
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    void deleteBySubjectClassIdAndStudentId(Long subjectClassId, Long studentId);

    /* ---------------------------------------------------
     * Tìm tất cả lớp môn học của sinh viên trong một học kỳ
     * @param studentId ID của sinh viên
     * @param semester Học kỳ
     * @returns Danh sách SubjectClassStudent
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    @Query("SELECT scs FROM SubjectClassStudent scs WHERE scs.student.id = :studentId " +
           "AND scs.subjectClass.semester = :semester AND scs.subjectClass.deletedAt IS NULL")
    List<SubjectClassStudent> findStudentEnrollmentsBySemester(@Param("studentId") Long studentId, 
                                                                @Param("semester") String semester);

    /* ---------------------------------------------------
     * Tìm sinh viên đang học một môn học cụ thể
     * @param subjectId ID của môn học
     * @param status Trạng thái đăng ký
     * @returns Danh sách SubjectClassStudent
     * @author: K24DTCN210-NVMANH (14/11/2025 14:03)
     * --------------------------------------------------- */
    @Query("SELECT scs FROM SubjectClassStudent scs WHERE scs.subjectClass.subject.id = :subjectId " +
           "AND scs.status = :status AND scs.subjectClass.deletedAt IS NULL")
    List<SubjectClassStudent> findBySubjectIdAndStatus(@Param("subjectId") Long subjectId, 
                                                        @Param("status") SubjectClassStudent.EnrollmentStatus status);
}

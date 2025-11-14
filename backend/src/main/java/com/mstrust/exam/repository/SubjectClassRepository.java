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

/* ---------------------------------------------------
 * Repository interface cho SubjectClass entity
 * Xử lý các thao tác database với bảng subject_classes
 * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
 * --------------------------------------------------- */
@Repository
public interface SubjectClassRepository extends JpaRepository<SubjectClass, Long> {

    /* ---------------------------------------------------
     * Tìm lớp môn học theo mã
     * @param code Mã lớp môn học
     * @returns Optional chứa SubjectClass nếu tìm thấy
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    Optional<SubjectClass> findByCode(String code);

    /* ---------------------------------------------------
     * Tìm lớp môn học theo mã và chưa bị xóa
     * @param code Mã lớp môn học
     * @returns Optional chứa SubjectClass nếu tìm thấy
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    Optional<SubjectClass> findByCodeAndDeletedAtIsNull(String code);

    /* ---------------------------------------------------
     * Tìm tất cả lớp môn học chưa bị xóa
     * @returns Danh sách SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    List<SubjectClass> findByDeletedAtIsNull();

    /* ---------------------------------------------------
     * Tìm lớp môn học chưa xóa với phân trang
     * @param pageable Thông tin phân trang
     * @returns Page chứa SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    Page<SubjectClass> findByDeletedAtIsNull(Pageable pageable);

    /* ---------------------------------------------------
     * Tìm lớp môn học theo Subject
     * @param subjectId ID của môn học
     * @returns Danh sách SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    List<SubjectClass> findBySubjectIdAndDeletedAtIsNull(Long subjectId);

    /* ---------------------------------------------------
     * Tìm lớp môn học theo Subject với phân trang
     * @param subjectId ID của môn học
     * @param pageable Thông tin phân trang
     * @returns Page chứa SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    Page<SubjectClass> findBySubjectIdAndDeletedAtIsNull(Long subjectId, Pageable pageable);

    /* ---------------------------------------------------
     * Tìm lớp môn học theo giáo viên
     * @param teacherId ID của giáo viên
     * @returns Danh sách SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    List<SubjectClass> findByTeacherIdAndDeletedAtIsNull(Long teacherId);

    /* ---------------------------------------------------
     * Tìm lớp môn học theo giáo viên với phân trang
     * @param teacherId ID của giáo viên
     * @param pageable Thông tin phân trang
     * @returns Page chứa SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    Page<SubjectClass> findByTeacherIdAndDeletedAtIsNull(Long teacherId, Pageable pageable);

    /* ---------------------------------------------------
     * Tìm lớp môn học theo học kỳ
     * @param semester Học kỳ (VD: "HK1_2024-2025")
     * @returns Danh sách SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    List<SubjectClass> findBySemesterAndDeletedAtIsNull(String semester);

    /* ---------------------------------------------------
     * Tìm lớp môn học theo học kỳ với phân trang
     * @param semester Học kỳ
     * @param pageable Thông tin phân trang
     * @returns Page chứa SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    Page<SubjectClass> findBySemesterAndDeletedAtIsNull(String semester, Pageable pageable);

    /* ---------------------------------------------------
     * Kiểm tra mã lớp môn học đã tồn tại chưa
     * @param code Mã lớp môn học
     * @returns true nếu đã tồn tại
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    boolean existsByCodeAndDeletedAtIsNull(String code);

    /* ---------------------------------------------------
     * Tìm kiếm lớp môn học theo từ khóa (mã hoặc tên môn)
     * @param keyword Từ khóa tìm kiếm
     * @param pageable Thông tin phân trang
     * @returns Page chứa SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    @Query("SELECT sc FROM SubjectClass sc WHERE sc.deletedAt IS NULL AND " +
           "(LOWER(sc.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sc.subject.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(sc.subject.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SubjectClass> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /* ---------------------------------------------------
     * Tìm lớp môn học theo Subject và Semester
     * @param subjectId ID của môn học
     * @param semester Học kỳ
     * @returns Danh sách SubjectClass
     * @author: K24DTCN210-NVMANH (14/11/2025 14:02)
     * --------------------------------------------------- */
    List<SubjectClass> findBySubjectIdAndSemesterAndDeletedAtIsNull(Long subjectId, String semester);
}

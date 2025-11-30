package com.mstrust.client. teacher.dto;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO để nhận thông tin Class từ backend API
 * Tương ứng với backend ClassDTO
 * @author: K24DTCN210-NVMANH (28/11/2025 15:58)
 * --------------------------------------------------- */
public class ClassDTO {
    
    private Long id;
    private String classCode;
    private String className;
    private Long departmentId;
    private String departmentName;
    private String academicYear;
    private String homeroomTeacher;
    private Long classManagerId;
    private Boolean isActive;
    private Integer studentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Long version;

    // Constructors
    public ClassDTO() {}

    public ClassDTO(Long id, String classCode, String className, String departmentName, 
                   String academicYear, String homeroomTeacher, Integer studentCount) {
        this.id = id;
        this.classCode = classCode;
        this.className = className;
        this.departmentName = departmentName;
        this.academicYear = academicYear;
        this.homeroomTeacher = homeroomTeacher;
        this.studentCount = studentCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this. departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getHomeroomTeacher() {
        return homeroomTeacher;
    }

    public void setHomeroomTeacher(String homeroomTeacher) {
        this.homeroomTeacher = homeroomTeacher;
    }

    public Long getClassManagerId() {
        return classManagerId;
    }

    public void setClassManagerId(Long classManagerId) {
        this.classManagerId = classManagerId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /* ---------------------------------------------------
     * Hiển thị thông tin class cho UI - format: "className - departmentName"
     * @returns String formatted cho ListView
     * @author: K24DTCN210-NVMANH (28/11/2025 15:58)
     * --------------------------------------------------- */
    @Override
    public String toString() {
        return className + " - " + departmentName + " (" + studentCount + " SV)";
    }

    /* ---------------------------------------------------
     * So sánh 2 ClassDTO objects theo ID
     * @param obj Object cần so sánh
     * @returns boolean
     * @author: K24DTCN210-NVMANH (28/11/2025 15:58)
     * --------------------------------------------------- */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClassDTO classDTO = (ClassDTO) obj;
        return id != null && id.equals(classDTO.id);
    }

    /* ---------------------------------------------------
     * Hash code cho ClassDTO based on ID
     * @returns int hash code
     * @author: K24DTCN210-NVMANH (28/11/2025 15:58)
     * --------------------------------------------------- */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

package com.mstrust.client. teacher.dto;

import com. fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO đại diện cho môn học trong hệ thống
 * Match với backend SubjectDTO structure (flat fields)
 * @author: K24DTCN210-NVMANH (19/11/2025 23:03)
 * EditBy: K24DTCN210-NVMANH (27/11/2025 08:18) - Fix: Use flat departmentId/departmentName instead of nested DepartmentDTO
 * --------------------------------------------------- */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubjectDTO {
    private Long id;
    
    @JsonProperty("subjectCode")
    private String subjectCode;
    
    @JsonProperty("subjectName")
    private String subjectName;
    
    private String description;
    private Integer credits;
    
    // Backend returns flat fields, not nested object
    private Long departmentId;
    private String departmentName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Long version;
    
    /* ---------------------------------------------------
     * Default constructor
     * @author: K24DTCN210-NVMANH (19/11/2025 23:03)
     * --------------------------------------------------- */
    public SubjectDTO() {
    }
    
    /* ---------------------------------------------------
     * Constructor với basic fields
     * @param id ID của môn học
     * @param subjectCode Mã môn học
     * @param subjectName Tên môn học
     * @author: K24DTCN210-NVMANH (19/11/2025 23:03)
     * --------------------------------------------------- */
    public SubjectDTO(Long id, String subjectCode, String subjectName) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSubjectCode() {
        return subjectCode;
    }
    
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }
    
    public String getSubjectName() {
        return subjectName;
    }
    
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getCredits() {
        return credits;
    }
    
    public void setCredits(Integer credits) {
        this.credits = credits;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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
     * Override toString để hiển thị tên môn học
     * @return Tên môn học
     * @author: K24DTCN210-NVMANH (19/11/2025 23:03)
     * --------------------------------------------------- */
    @Override
    public String toString() {
        return subjectName;
    }
}

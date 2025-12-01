package com.mstrust.client.teacher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/* ---------------------------------------------------
 * Request DTO để tạo mới môn học
 * @author: K24DTCN210-NVMANH (26/11/2025 01:46)
 * --------------------------------------------------- */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubjectRequest {
    private String subjectCode;     // Mã môn học (required, max 20, unique)
    private String subjectName;     // Tên môn học (required, max 255)
    private String description;     // Mô tả (optional)
    private Integer credits;        // Số tín chỉ (required, >= 0)
    private Long departmentId;      // ID khoa (required)
    
    // Manual getters/setters (backup for Lombok issues)
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}

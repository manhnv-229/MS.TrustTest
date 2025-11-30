package com.mstrust.client.teacher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/* ---------------------------------------------------
 * DTO đại diện cho lớp học phần (SubjectClass) trong hệ thống
 * Match với backend SubjectClassDTO structure
 * @author: K24DTCN210-NVMANH (30/11/2025)
 * --------------------------------------------------- */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubjectClassDTO {
    
    private Long id;
    private String code;
    private Long subjectId;
    private SubjectDTO subject;
    private String semester;
    private Long teacherId;
    private String schedule;
    private Integer maxStudents;
    private Integer enrolledCount;
    private Integer availableSlots;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Long version;
    
    /* ---------------------------------------------------
     * Default constructor
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public SubjectClassDTO() {
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public Long getSubjectId() {
        return subjectId;
    }
    
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
    
    public SubjectDTO getSubject() {
        return subject;
    }
    
    public void setSubject(SubjectDTO subject) {
        this.subject = subject;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    public Long getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getSchedule() {
        return schedule;
    }
    
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
    
    public Integer getMaxStudents() {
        return maxStudents;
    }
    
    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }
    
    public Integer getEnrolledCount() {
        return enrolledCount;
    }
    
    public void setEnrolledCount(Integer enrolledCount) {
        this.enrolledCount = enrolledCount;
    }
    
    public Integer getAvailableSlots() {
        return availableSlots;
    }
    
    public void setAvailableSlots(Integer availableSlots) {
        this.availableSlots = availableSlots;
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
     * Lấy tên hiển thị: "code - subjectName"
     * @return String để hiển thị trong ComboBox
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public String getDisplayName() {
        String subjectName = (subject != null && subject.getSubjectName() != null) 
            ? subject.getSubjectName() 
            : "N/A";
        return code + " - " + subjectName;
    }
    
    /* ---------------------------------------------------
     * Override toString để hiển thị trong ComboBox
     * @return String formatted
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    @Override
    public String toString() {
        return getDisplayName();
    }
}


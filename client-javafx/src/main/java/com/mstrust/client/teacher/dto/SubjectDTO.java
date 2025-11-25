package com.mstrust.client.teacher.dto;

/* ---------------------------------------------------
 * DTO đại diện cho môn học trong hệ thống
 * Dùng để hiển thị trong ComboBox và các UI component
 * @author: K24DTCN210-NVMANH (19/11/2025 23:03)
 * --------------------------------------------------- */
public class SubjectDTO {
    private Long id;
    private String code;
    private String name;
    
    public SubjectDTO() {
    }
    
    public SubjectDTO(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }
    
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
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}

package com.mstrust.exam.exception;

/* ---------------------------------------------------
 * Exception khi resource đã tồn tại (duplicate email, student_code, etc.)
 * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
 * --------------------------------------------------- */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

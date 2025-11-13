package com.mstrust.exam.exception;

/* ---------------------------------------------------
 * Exception khi không tìm thấy resource (User, Role, etc.)
 * @author: K24DTCN210-NVMANH (13/11/2025 14:59)
 * --------------------------------------------------- */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}

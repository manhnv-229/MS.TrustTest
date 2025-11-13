package com.mstrust.exam.exception;

/* ---------------------------------------------------
 * Exception khi login credentials không đúng
 * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
 * --------------------------------------------------- */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

package com.mstrust.exam.exception;

/* ---------------------------------------------------
 * Exception cho bad request (invalid input, validation errors)
 * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
 * --------------------------------------------------- */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
}

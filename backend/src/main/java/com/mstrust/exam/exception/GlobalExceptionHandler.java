package com.mstrust.exam.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/* ---------------------------------------------------
 * Global Exception Handler
 * Xử lý tất cả exceptions và trả về JSON response thống nhất
 * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
 * --------------------------------------------------- */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ---------------------------------------------------
     * Xử lý ResourceNotFoundException
     * @param ex Exception
     * @returns ResponseEntity với HTTP 404
     * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
     * --------------------------------------------------- */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /* ---------------------------------------------------
     * Xử lý DuplicateResourceException
     * @param ex Exception
     * @returns ResponseEntity với HTTP 409 Conflict
     * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
     * --------------------------------------------------- */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /* ---------------------------------------------------
     * Xử lý InvalidCredentialsException và BadCredentialsException
     * @param ex Exception
     * @returns ResponseEntity với HTTP 401 Unauthorized
     * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
     * --------------------------------------------------- */
    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /* ---------------------------------------------------
     * Xử lý UsernameNotFoundException
     * @param ex Exception
     * @returns ResponseEntity với HTTP 401 Unauthorized
     * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
     * --------------------------------------------------- */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /* ---------------------------------------------------
     * Xử lý BadRequestException
     * @param ex Exception
     * @returns ResponseEntity với HTTP 400 Bad Request
     * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
     * --------------------------------------------------- */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /* ---------------------------------------------------
     * Xử lý validation errors (@Valid annotation)
     * @param ex MethodArgumentNotValidException
     * @returns ResponseEntity với HTTP 400 và danh sách lỗi validation
     * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
     * --------------------------------------------------- */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Validation failed");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /* ---------------------------------------------------
     * Xử lý lỗi invalid enum value (JSON parse error)
     * @param ex HttpMessageNotReadableException
     * @returns ResponseEntity với HTTP 400 Bad Request
     * @author: K24DTCN210-NVMANH (19/11/2025 09:54)
     * --------------------------------------------------- */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Invalid request format";
        
        // Check if it's an enum parsing error
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                String fieldName = ife.getPath().get(0).getFieldName();
                String invalidValue = ife.getValue().toString();
                Class<?> enumClass = ife.getTargetType();
                
                // Get valid enum values
                Object[] enumConstants = enumClass.getEnumConstants();
                StringBuilder validValues = new StringBuilder();
                for (int i = 0; i < enumConstants.length; i++) {
                    validValues.append(enumConstants[i].toString());
                    if (i < enumConstants.length - 1) {
                        validValues.append(", ");
                    }
                }
                
                message = String.format("Invalid value '%s' for field '%s'. Valid values are: %s", 
                    invalidValue, fieldName, validValues.toString());
            }
        }
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /* ---------------------------------------------------
     * Xử lý tất cả exceptions khác
     * @param ex Exception
     * @returns ResponseEntity với HTTP 500 Internal Server Error
     * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
     * --------------------------------------------------- */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /* ---------------------------------------------------
     * Inner class cho error response format
     * @author: K24DTCN210-NVMANH (13/11/2025 15:00)
     * --------------------------------------------------- */
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;

        public ErrorResponse(int status, String message, LocalDateTime timestamp) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}

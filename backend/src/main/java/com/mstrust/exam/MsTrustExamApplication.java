package com.mstrust.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/* ---------------------------------------------------
 * Main Application class cho MS.TrustTest Backend
 * Khởi động Spring Boot application
 * @author: K24DTCN210-NVMANH (13/11/2025 14:22)
 * EditBy: K24DTCN210-NVMANH (21/11/2025 01:51) - Enable scheduling cho WebSocket timer sync
 * --------------------------------------------------- */
@SpringBootApplication
@EnableScheduling
public class MsTrustExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsTrustExamApplication.class, args);
    }

}

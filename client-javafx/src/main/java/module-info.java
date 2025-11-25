module com.mstrust.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.github.kwhat.jnativehook;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires com.google.gson;
    requires static lombok;
    requires org.slf4j;
    requires java.net.http;  // Phase 8: HttpClient for ExamApiClient
    
    // Phase 8.4: Auto-Save Services - Required for automatic modules (commented temporarily)
    // requires okhttp3;  // OkHttp HTTP client
    // requires org.fxmisc.richtext;  // RichTextFX code editor
    
    opens com.mstrust.client to javafx.fxml;
    opens com.mstrust.client.dto to com.google.gson;
    opens com.mstrust.client.exam.controller to javafx.fxml;
    opens com.mstrust.client.exam.dto to com.google.gson;
    opens com.mstrust.client.exam.api to com.google.gson;  // Phase 8.6: For ExamResultResponse
    opens com.mstrust.client.exam.service to com.google.gson;  // Phase 8.4: For QueuedAnswer JSON
    opens com.mstrust.client.teacher.controller to javafx.fxml;  // Phase 9: Teacher Dashboard
    opens com.mstrust.client.teacher.dto to com.google.gson;  // Phase 9.2: Question Bank DTOs
    opens com.mstrust.client.teacher.api to com.google.gson;  // Phase 9.3: QuestionBankResponse JSON
    
    exports com.mstrust.client;
    exports com.mstrust.client.monitoring;
    exports com.mstrust.client.api;
    exports com.mstrust.client.config;
    exports com.mstrust.client.dto;
    exports com.mstrust.client.util;
    exports com.mstrust.client.exam;  // Phase 8.6: ExamClientApplication
    exports com.mstrust.client.exam.controller;
    exports com.mstrust.client.exam.component;
    exports com.mstrust.client.exam.dto;
    exports com.mstrust.client.exam.model;
    exports com.mstrust.client.exam.api;
    exports com.mstrust.client.exam.util;
    exports com.mstrust.client.exam.service;
    exports com.mstrust.client.teacher.controller;
    exports com.mstrust.client.teacher.dto;  // Phase 9.2: Question Bank DTOs
    exports com.mstrust.client.teacher.api;  // Phase 9.2: API Clients
}

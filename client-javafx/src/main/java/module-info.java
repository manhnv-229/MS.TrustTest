module com.mstrust.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.github.kwhat.jnativehook;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires com.google.gson;
    requires static lombok;
    requires java.net.http;
    requires java.desktop;
    requires org.slf4j;
    
    // Phase 8.4: Auto-Save Services - Required for automatic modules
    requires okhttp3;  // OkHttp HTTP client
    requires org.fxmisc.richtext;  // RichTextFX code editor
    
    opens com.mstrust.client to javafx.fxml;
    opens com.mstrust.client.dto to com.google.gson;
    opens com.mstrust.client.exam.controller to javafx.fxml;
    opens com.mstrust.client.exam.dto to com.google.gson;
    opens com.mstrust.client.exam.api to com.google.gson;  // Phase 8.6: For ExamResultResponse
    opens com.mstrust.client.exam.service to com.google.gson;  // Phase 8.4: For QueuedAnswer JSON
    
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
}

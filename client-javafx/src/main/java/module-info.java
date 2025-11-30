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
    requires java.net.http;
    
    // Ikonli Icon Library
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    
    opens com.mstrust.client to javafx.fxml;
    opens com.mstrust.client.dto to com.google.gson;
    opens com.mstrust.client.exam.controller to javafx.fxml;
    opens com.mstrust.client.exam.dto to com.google.gson;
    opens com.mstrust.client.exam.api to com.google.gson;
    opens com.mstrust.client.exam.service to com.google.gson;
    opens com.mstrust.client.teacher.controller to javafx.fxml;
    opens com.mstrust.client.teacher.controller.wizard to javafx.fxml;
    opens com.mstrust.client.teacher.dto to com.google.gson;
    opens com.mstrust.client.teacher.api to com.google.gson;
    
    exports com.mstrust.client;
    exports com.mstrust.client.monitoring;
    exports com.mstrust.client.api;
    exports com.mstrust.client.config;
    exports com.mstrust.client.dto;
    exports com.mstrust.client.util;
    exports com.mstrust.client.exam;
    exports com.mstrust.client.exam.controller;
    exports com.mstrust.client.exam.component;
    exports com.mstrust.client.exam.dto;
    exports com.mstrust.client.exam.model;
    exports com.mstrust.client.exam.api;
    exports com.mstrust.client.exam.util;
    exports com.mstrust.client.exam.service;
    exports com.mstrust.client.teacher.controller;
    exports com.mstrust.client.teacher.controller.wizard;
    exports com.mstrust.client.teacher.dto;
    exports com.mstrust.client.teacher.api;
}

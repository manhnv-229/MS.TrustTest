module com.mstrust.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.github.kwhat.jnativehook;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires com.google.gson;
    requires static lombok;
    requires java.net.http;
    requires java.desktop;
    requires org.slf4j;
    
    opens com.mstrust.client to javafx.fxml;
    opens com.mstrust.client.dto to com.google.gson;
    
    exports com.mstrust.client;
    exports com.mstrust.client.monitoring;
    exports com.mstrust.client.api;
    exports com.mstrust.client.config;
    exports com.mstrust.client.dto;
    exports com.mstrust.client.util;
}

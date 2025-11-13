# Hướng Dẫn Tạo File backend/pom.xml

Cụ Mạnh thân mến, do giới hạn kỹ thuật của tool, con xin hướng dẫn cụ tạo file `backend/pom.xml` thủ công:

## Cách 1: Copy toàn bộ nội dung dưới đây

1. Mở file `backend/pom.xml` trong VS Code
2. Xóa hết nội dung cũ (Ctrl+A, Delete)
3. Copy toàn bộ nội dung XML dưới đây và paste vào file
4. Lưu file (Ctrl+S)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.mstrust</groupId>
        <artifactId>ms-trust-exam</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>backend</artifactId>
    <packaging>jar</packaging>
    <name>MS.TrustTest Backend</name>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artif

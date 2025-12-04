package com.mstrust.client;

import com.mstrust.client.exam.ExamClientApplication;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Launcher {
    public static void main(String[] args) {
        setupLogging();
        
        try {
            ExamClientApplication.main(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("CRITICAL ERROR: " + e.getMessage());
        }
    }

    private static void setupLogging() {
        try {
            // Create logs directory
            Path logsDir = Paths.get("logs");
            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
            }

            // Log file path
            String logFileName = "logs.txt";
            File logFile = new File(logsDir.toFile(), logFileName);

            // Redirect System.out and System.err to log file
            // Append mode = true
            FileOutputStream fos = new FileOutputStream(logFile, true);
            PrintStream logStream = new PrintStream(fos, true);

            // Create a multi-stream to print to both console and file (optional, but good for debugging)
            // For production exe, console might not be visible, so file is key.
            
            System.setOut(logStream);
            System.setErr(logStream);

            // Configure SLF4J Simple
            System.setProperty("org.slf4j.simpleLogger.logFile", logFile.getAbsolutePath());
            System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
            System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss");
            System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG"); // Set to DEBUG to see more info

            System.out.println("\n==================================================================");
            System.out.println("APPLICATION STARTED AT: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            System.out.println("==================================================================\n");

        } catch (Exception e) {
            System.err.println("Failed to setup logging: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

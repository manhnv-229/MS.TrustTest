package com.mstrust.exam.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/* ---------------------------------------------------
 * Service xử lý upload file lên FTP server
 * @author: K24DTCN210-NVMANH (21/11/2025 10:12)
 * --------------------------------------------------- */
@Service
@Slf4j
public class FtpStorageService {
    
    @Value("${ftp.server:153.92.11.239}")
    private String ftpServer;
    
    @Value("${ftp.port:21}")
    private int ftpPort;
    
    @Value("${ftp.username:u341775345.admin}")
    private String ftpUsername;
    
    @Value("${ftp.password:!M@nh1989}")
    private String ftpPassword;
    
    @Value("${ftp.base-path:/trusttest/screenshots}")
    private String ftpBasePath;
    
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    private static final float JPEG_QUALITY = 0.7f;
    
    /* ---------------------------------------------------
     * Upload screenshot lên FTP server với compression
     * @param file File ảnh cần upload
     * @param submissionId ID của submission
     * @returns Đường dẫn file trên FTP server
     * @author: K24DTCN210-NVMANH (21/11/2025 10:12)
     * --------------------------------------------------- */
    public String uploadScreenshot(MultipartFile file, Long submissionId) throws IOException {
        FTPClient ftpClient = new FTPClient();
        
        try {
            // Connect to FTP server
            ftpClient.connect(ftpServer, ftpPort);
            int replyCode = ftpClient.getReplyCode();
            
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new IOException("FTP server refused connection");
            }
            
            // Login
            boolean logged = ftpClient.login(ftpUsername, ftpPassword);
            if (!logged) {
                throw new IOException("Failed to login to FTP server");
            }
            
            log.info("Connected to FTP server: {}", ftpServer);
            
            // Set transfer mode
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            
            // Create directory structure: /trusttest/screenshots/YYYY-MM/submission_id/
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String remoteDirPath = ftpBasePath + "/screenshots/" + dateFolder + "/" + submissionId;
            
            createRemoteDirectory(ftpClient, remoteDirPath);
            
            // Generate filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String filename = "screenshot_" + timestamp + ".jpg";
            String remoteFilePath = remoteDirPath + "/" + filename;
            
            // Process and compress image
            ByteArrayInputStream compressedImageStream = compressImage(file);
            
            // Upload file
            boolean uploaded = ftpClient.storeFile(remoteFilePath, compressedImageStream);
            
            if (!uploaded) {
                throw new IOException("Failed to upload file to FTP server");
            }
            
            log.info("Successfully uploaded screenshot to: {}", remoteFilePath);
            
            return remoteFilePath;
            
        } finally {
            // Disconnect
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    log.error("Error disconnecting from FTP server", ex);
                }
            }
        }
    }
    
    /* ---------------------------------------------------
     * Tạo thư mục trên FTP server (tạo nested directories nếu chưa tồn tại)
     * @param ftpClient FTP client
     * @param dirPath Đường dẫn thư mục
     * @author: K24DTCN210-NVMANH (21/11/2025 10:12)
     * --------------------------------------------------- */
    private void createRemoteDirectory(FTPClient ftpClient, String dirPath) throws IOException {
        String[] folders = dirPath.split("/");
        String currentPath = "";
        
        for (String folder : folders) {
            if (folder.isEmpty()) continue;
            
            currentPath += "/" + folder;
            
            if (!ftpClient.changeWorkingDirectory(currentPath)) {
                // Directory doesn't exist, create it
                if (ftpClient.makeDirectory(currentPath)) {
                    log.debug("Created directory: {}", currentPath);
                } else {
                    throw new IOException("Failed to create directory: " + currentPath);
                }
            }
        }
    }
    
    /* ---------------------------------------------------
     * Compress và resize ảnh (JPEG 70%, max 1920x1080)
     * @param file File ảnh gốc
     * @returns InputStream của ảnh đã compress
     * @author: K24DTCN210-NVMANH (21/11/2025 10:12)
     * --------------------------------------------------- */
    private ByteArrayInputStream compressImage(MultipartFile file) throws IOException {
        // Read original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IOException("Failed to read image file");
        }
        
        // Resize if necessary
        BufferedImage resizedImage = originalImage;
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        if (width > MAX_WIDTH || height > MAX_HEIGHT) {
            resizedImage = Scalr.resize(
                originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_TO_WIDTH,
                MAX_WIDTH,
                MAX_HEIGHT,
                Scalr.OP_ANTIALIAS
            );
            log.debug("Resized image from {}x{} to {}x{}", 
                width, height, resizedImage.getWidth(), resizedImage.getHeight());
        }
        
        // Compress to JPEG
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);
        
        byte[] imageBytes = outputStream.toByteArray();
        log.debug("Compressed image size: {} KB", imageBytes.length / 1024);
        
        return new ByteArrayInputStream(imageBytes);
    }
    
    /* ---------------------------------------------------
     * Xóa file trên FTP server
     * @param remoteFilePath Đường dẫn file trên FTP
     * @returns true nếu xóa thành công
     * @author: K24DTCN210-NVMANH (21/11/2025 10:12)
     * --------------------------------------------------- */
    public boolean deleteFile(String remoteFilePath) {
        FTPClient ftpClient = new FTPClient();
        
        try {
            ftpClient.connect(ftpServer, ftpPort);
            ftpClient.login(ftpUsername, ftpPassword);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            boolean deleted = ftpClient.deleteFile(remoteFilePath);
            
            if (deleted) {
                log.info("Deleted file from FTP: {}", remoteFilePath);
            }
            
            return deleted;
            
        } catch (IOException ex) {
            log.error("Error deleting file from FTP: {}", remoteFilePath, ex);
            return false;
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException ex) {
                    log.error("Error disconnecting from FTP", ex);
                }
            }
        }
    }
}

package com.mstrust.client.exam.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/* ---------------------------------------------------
 * Utility class để format thời gian cho UI
 * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
 * --------------------------------------------------- */
public class TimeFormatter {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM");

    /* ---------------------------------------------------
     * Format seconds thành HH:MM:SS
     * @param seconds Số giây
     * @returns String dạng "01:23:45"
     * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
     * --------------------------------------------------- */
    public static String formatSeconds(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /* ---------------------------------------------------
     * Format seconds thành MM:SS (cho timer dưới 1 giờ)
     * @param seconds Số giây
     * @returns String dạng "23:45"
     * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
     * --------------------------------------------------- */
    public static String formatMinutesSeconds(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /* ---------------------------------------------------
     * Format LocalDateTime thành string hiển thị
     * @param dateTime LocalDateTime cần format
     * @returns String dạng "23/11/2025 12:00"
     * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
     * --------------------------------------------------- */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /* ---------------------------------------------------
     * Format giờ từ LocalDateTime
     * @param dateTime LocalDateTime cần format
     * @returns String dạng "12:00"
     * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
     * EditBy: K24DTCN210-NVMANH (02/12/2025 17:15) - Fixed format to only show time
     * --------------------------------------------------- */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_FORMATTER);
    }
    
    /* ---------------------------------------------------
     * Format ngày từ LocalDateTime
     * @param dateTime LocalDateTime cần format
     * @returns String dạng "23/11"
     * @author: K24DTCN210-NVMANH (02/12/2025 17:15)
     * --------------------------------------------------- */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMATTER);
    }
    
    /* ---------------------------------------------------
     * Format thời gian từ số giây (dạng long) sang dạng HH:mm:ss
     * @param seconds Số giây cần format
     * @returns Chuỗi thời gian định dạng HH:mm:ss
     * @author: K24DTCN210-NVMANH (24/11/2025 08:06)
     * --------------------------------------------------- */
    public static String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /* ---------------------------------------------------
     * Tính thời gian còn lại (friendly format với ngày)
     * @param endTime Thời điểm kết thúc
     * @returns String dạng "2 ngày 15 giờ 4 phút", "2 giờ 30 phút" hoặc "45 phút"
     * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
     * EditBy: K24DTCN210-NVMANH (02/12/2025 18:30) - Added days support
     * --------------------------------------------------- */
    public static String formatTimeRemaining(LocalDateTime endTime) {
        if (endTime == null) return "";
        
        Duration duration = Duration.between(LocalDateTime.now(), endTime);
        if (duration.isNegative()) {
            return "Đã kết thúc";
        }
        
        long totalHours = duration.toHours();
        long days = totalHours / 24;
        long hours = totalHours % 24;
        long minutes = duration.toMinutes() % 60;
        
        StringBuilder result = new StringBuilder();
        
        if (days > 0) {
            result.append(days).append(" ngày");
            if (hours > 0) {
                result.append(" ").append(hours).append(" giờ");
            }
            if (minutes > 0) {
                result.append(" ").append(minutes).append(" phút");
            }
        } else if (hours > 0) {
            result.append(hours).append(" giờ");
            if (minutes > 0) {
                result.append(" ").append(minutes).append(" phút");
            }
        } else {
            result.append(minutes).append(" phút");
        }
        
        return result.toString();
    }

    /* ---------------------------------------------------
     * Tính phần trăm thời gian đã qua
     * @param startTime Thời điểm bắt đầu
     * @param endTime Thời điểm kết thúc
     * @returns Phần trăm 0-100
     * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
     * --------------------------------------------------- */
    public static double calculateTimeProgress(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) return 0.0;
        
        LocalDateTime now = LocalDateTime.now();
        Duration total = Duration.between(startTime, endTime);
        Duration elapsed = Duration.between(startTime, now);
        
        if (elapsed.isNegative()) return 0.0;
        if (elapsed.compareTo(total) > 0) return 100.0;
        
        return (elapsed.toSeconds() * 100.0) / total.toSeconds();
    }

    /* ---------------------------------------------------
     * Tính remaining seconds từ endTime
     * @param endTime Thời điểm kết thúc
     * @returns Số giây còn lại (hoặc 0 nếu đã hết)
     * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
     * --------------------------------------------------- */
    public static long calculateRemainingSeconds(LocalDateTime endTime) {
        if (endTime == null) return 0;
        
        Duration duration = Duration.between(LocalDateTime.now(), endTime);
        if (duration.isNegative()) return 0;
        
        return duration.getSeconds();
    }

    /* ---------------------------------------------------
     * Kiểm tra timer ở giai đoạn nào (cho color coding)
     * @param remainingSeconds Số giây còn lại
     * @param totalSeconds Tổng số giây ban đầu
     * @returns TimerPhase enum (GREEN, YELLOW, RED)
     * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
     * EditBy: K24DTCN210-NVMANH (23/11/2025 13:20) - Return enum thay vì String
     * --------------------------------------------------- */
    public static TimerPhase getTimerPhase(long remainingSeconds, long totalSeconds) {
        if (totalSeconds == 0) return TimerPhase.GREEN;
        
        double percentage = (remainingSeconds * 100.0) / totalSeconds;
        
        if (percentage > 50) {
            return TimerPhase.GREEN;
        } else if (percentage > 20) {
            return TimerPhase.YELLOW;
        } else {
            return TimerPhase.RED;
        }
    }

    /* ---------------------------------------------------
     * Format duration (phút) sang string hiển thị
     * @param minutes Số phút
     * @returns String dạng "90 phút" hoặc "1 giờ 30 phút"
     * @author: K24DTCN210-NVMANH (23/11/2025 12:01)
     * --------------------------------------------------- */
    public static String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " phút";
        }
        
        int hours = minutes / 60;
        int mins = minutes % 60;
        
        if (mins == 0) {
            return hours + " giờ";
        }
        
        return String.format("%d giờ %d phút", hours, mins);
    }
}

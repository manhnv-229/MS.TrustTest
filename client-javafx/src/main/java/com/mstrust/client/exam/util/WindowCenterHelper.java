package com.mstrust.client.exam.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

/* ---------------------------------------------------
 * Utility class để center windows/dialogs trên màn hình
 * Hỗ trợ center cho cả Stage và Dialog
 * @author: K24DTCN210-NVMANH (25/11/2025 15:03)
 * --------------------------------------------------- */
public class WindowCenterHelper {
    
    /* ---------------------------------------------------
     * Center stage trên màn hình chính
     * @param stage Stage cần center
     * @author: K24DTCN210-NVMANH (25/11/2025 15:03)
     * --------------------------------------------------- */
    public static void centerStage(Stage stage) {
        if (stage == null) return;
        
        // Get primary screen bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Calculate center position
        double centerX = (screenBounds.getWidth() - stage.getWidth()) / 2;
        double centerY = (screenBounds.getHeight() - stage.getHeight()) / 2;
        
        // Set position
        stage.setX(screenBounds.getMinX() + centerX);
        stage.setY(screenBounds.getMinY() + centerY);
    }
    
    /* ---------------------------------------------------
     * Center stage sau khi nó được show (vì width/height chưa có trước show)
     * @param stage Stage cần center
     * @author: K24DTCN210-NVMANH (25/11/2025 15:03)
     * --------------------------------------------------- */
    public static void centerStageOnScreen(Stage stage) {
        if (stage == null) return;
        
        // Wait for stage to be shown and get actual dimensions
        stage.setOnShown(event -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            double centerX = (screenBounds.getWidth() - stage.getWidth()) / 2;
            double centerY = (screenBounds.getHeight() - stage.getHeight()) / 2;
            
            stage.setX(screenBounds.getMinX() + centerX);
            stage.setY(screenBounds.getMinY() + centerY);
        });
    }
    
    /* ---------------------------------------------------
     * Center window (generic method cho bất kỳ Window nào)
     * @param window Window cần center
     * @author: K24DTCN210-NVMANH (25/11/2025 15:03)
     * --------------------------------------------------- */
    public static void centerWindow(Window window) {
        if (window == null) return;
        
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        double centerX = (screenBounds.getWidth() - window.getWidth()) / 2;
        double centerY = (screenBounds.getHeight() - window.getHeight()) / 2;
        
        window.setX(screenBounds.getMinX() + centerX);
        window.setY(screenBounds.getMinY() + centerY);
    }
    
    /* ---------------------------------------------------
     * Center window sau khi show
     * @param window Window cần center
     * @author: K24DTCN210-NVMANH (25/11/2025 15:03)
     * --------------------------------------------------- */
    public static void centerWindowOnShown(Window window) {
        if (window == null) return;
        
        window.setOnShown(event -> centerWindow(window));
    }
}

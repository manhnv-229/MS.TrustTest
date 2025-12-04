package com.mstrust.client.util;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.beans.value.ChangeListener;

import java.util.Optional;

/**
 * Utility class for showing dialogs with dimmed background effect
 */
public class DialogUtils {

    /* ---------------------------------------------------
     * Hiển thị Alert Dialog với hiệu ứng làm mờ nền và custom buttons
     * @param type Loại Alert
     * @param title Tiêu đề
     * @param header Header text
     * @param content Nội dung
     * @param owner Cửa sổ cha
     * @param buttonTypes Các nút tùy chỉnh (nếu có)
     * @returns Kết quả người dùng chọn
     * @author: K24DTCN210-NVMANH (05/12/2025)
     * --------------------------------------------------- */
    public static Optional<ButtonType> showAlert(Alert.AlertType type, String title, String header, String content, Window owner, ButtonType... buttonTypes) {
        // Tìm owner nếu chưa có
        if (owner == null) {
            owner = Stage.getWindows().stream()
                    .filter(Window::isShowing)
                    .findFirst()
                    .orElse(null);
        }

        final Window finalOwner = owner;
        Stage dimStage = null;

        // Tạo lớp mờ (Overlay Stage) nếu tìm thấy owner
        if (finalOwner != null) {
            dimStage = new Stage(StageStyle.TRANSPARENT);
            dimStage.initOwner(finalOwner);
            dimStage.initModality(Modality.NONE);
            
            StackPane root = new StackPane();
            root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);"); // Màu đen mờ 40%
            Scene scene = new Scene(root, Color.TRANSPARENT);
            dimStage.setScene(scene);
            
            // Bind kích thước và vị trí theo owner
            dimStage.setX(finalOwner.getX());
            dimStage.setY(finalOwner.getY());
            dimStage.setWidth(finalOwner.getWidth());
            dimStage.setHeight(finalOwner.getHeight());
            
            // Listener để cập nhật khi owner di chuyển/resize
            final Stage fDimStage = dimStage;
            ChangeListener<Number> xListener = (obs, oldVal, newVal) -> fDimStage.setX(newVal.doubleValue());
            ChangeListener<Number> yListener = (obs, oldVal, newVal) -> fDimStage.setY(newVal.doubleValue());
            ChangeListener<Number> wListener = (obs, oldVal, newVal) -> fDimStage.setWidth(newVal.doubleValue());
            ChangeListener<Number> hListener = (obs, oldVal, newVal) -> fDimStage.setHeight(newVal.doubleValue());
            
            finalOwner.xProperty().addListener(xListener);
            finalOwner.yProperty().addListener(yListener);
            finalOwner.widthProperty().addListener(wListener);
            finalOwner.heightProperty().addListener(hListener);
            
            dimStage.show();
            
            // Store listeners in userData to remove them later
            dimStage.setUserData(new Object[]{xListener, yListener, wListener, hListener});
        }

        // Tạo và hiển thị Alert
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Set custom buttons nếu có
        if (buttonTypes != null && buttonTypes.length > 0) {
            alert.getButtonTypes().setAll(buttonTypes);
        }

        // Set owner cho Alert để nó nằm trên lớp mờ
        if (dimStage != null) {
            alert.initOwner(dimStage);
        } else if (owner != null) {
            alert.initOwner(owner);
        }

        // Show dialog và đợi kết quả
        Optional<ButtonType> result = alert.showAndWait();

        // Đóng lớp mờ sau khi Alert đóng
        if (dimStage != null) {
            // Remove listeners
            Object userData = dimStage.getUserData();
            if (userData instanceof Object[]) {
                Object[] listeners = (Object[]) userData;
                finalOwner.xProperty().removeListener((ChangeListener<Number>) listeners[0]);
                finalOwner.yProperty().removeListener((ChangeListener<Number>) listeners[1]);
                finalOwner.widthProperty().removeListener((ChangeListener<Number>) listeners[2]);
                finalOwner.heightProperty().removeListener((ChangeListener<Number>) listeners[3]);
            }
            
            dimStage.close();
        }

        return result;
    }

    /* ---------------------------------------------------
     * Overload showAlert cho trường hợp không có custom buttons
     * @author: K24DTCN210-NVMANH (05/12/2025)
     * --------------------------------------------------- */
    public static Optional<ButtonType> showAlert(Alert.AlertType type, String title, String header, String content, Window owner) {
        return showAlert(type, title, header, content, owner, (ButtonType[]) null);
    }

    /* ---------------------------------------------------
     * Helper: Hiển thị Error Alert
     * @author: K24DTCN210-NVMANH (05/12/2025)
     * --------------------------------------------------- */
    public static void showError(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, null, content, null);
    }

    public static void showError(String title, String header, String content) {
        showAlert(Alert.AlertType.ERROR, title, header, content, null);
    }

    /* ---------------------------------------------------
     * Helper: Hiển thị Information Alert
     * @author: K24DTCN210-NVMANH (05/12/2025)
     * --------------------------------------------------- */
    public static void showInfo(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, null, content, null);
    }

    /* ---------------------------------------------------
     * Helper: Hiển thị Warning Alert
     * @author: K24DTCN210-NVMANH (05/12/2025)
     * --------------------------------------------------- */
    public static void showWarning(String title, String content) {
        showAlert(Alert.AlertType.WARNING, title, null, content, null);
    }

    /* ---------------------------------------------------
     * Helper: Hiển thị Confirmation Alert
     * @author: K24DTCN210-NVMANH (05/12/2025)
     * --------------------------------------------------- */
    public static Optional<ButtonType> showConfirmation(String title, String content) {
        return showAlert(Alert.AlertType.CONFIRMATION, title, null, content, null);
    }
    
    public static Optional<ButtonType> showConfirmation(String title, String header, String content) {
        return showAlert(Alert.AlertType.CONFIRMATION, title, header, content, null);
    }
}

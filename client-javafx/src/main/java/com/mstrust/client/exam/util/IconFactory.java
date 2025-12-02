package com.mstrust.client.exam.util;

import javafx.scene.paint.Color;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
// Material Design và Bootstrap Icons đã bị xóa do Aliyun mirror không hỗ trợ

/* ---------------------------------------------------
 * Factory class để tạo các icon Ikonli cho ứng dụng
 * Cung cấp các method tiện ích để tạo icon với size và color phù hợp
 * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
 * EditBy: K24DTCN210-NVMANH (27/11/2025 19:49) - Đổi màu menu icons sang WHITE
 * --------------------------------------------------- */
public class IconFactory {
    
    // Kích thước chuẩn
    public static final int SIZE_SMALL = 14;
    public static final int SIZE_NORMAL = 16;
    public static final int SIZE_MEDIUM = 20;
    public static final int SIZE_LARGE = 24;
    public static final int SIZE_XLARGE = 32;
    
    // Màu sắc chuẩn
    public static final Color COLOR_PRIMARY = Color.web("#2196F3");
    public static final Color COLOR_SUCCESS = Color.web("#4CAF50");
    public static final Color COLOR_WARNING = Color.web("#FF9800");
    public static final Color COLOR_DANGER = Color.web("#F44336");
    public static final Color COLOR_INFO = Color.web("#00BCD4");
    public static final Color COLOR_WHITE = Color.WHITE;
    public static final Color COLOR_GRAY = Color.web("#757575");
    public static final Color COLOR_DARK = Color.web("#424242");
    
    /* ---------------------------------------------------
     * Tạo icon user cho login screen
     * @returns FontIcon với icon user
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createUserIcon() {
        return createIcon(FontAwesomeSolid.USER, SIZE_NORMAL, COLOR_GRAY);
    }
    
    /* ---------------------------------------------------
     * Tạo icon lock cho password field
     * @returns FontIcon với icon lock
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createLockIcon() {
        return createIcon(FontAwesomeSolid.LOCK, SIZE_NORMAL, COLOR_GRAY);
    }
    
    /* ---------------------------------------------------
     * Tạo icon login cho button đăng nhập
     * @returns FontIcon với icon sign-in
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createLoginIcon() {
        return createIcon(FontAwesomeSolid.SIGN_IN_ALT, SIZE_NORMAL, COLOR_WHITE);
    }
    
    // Kích thước chuẩn cho menu icons (18px)
    public static final int MENU_ICON_SIZE = 18;
    
    /* ---------------------------------------------------
     * Tạo icon ngân hàng câu hỏi (Question Bank)
     * @returns FontIcon với icon book màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 19:56) - Cố định size 18px
     * --------------------------------------------------- */
    public static FontIcon createQuestionBankIcon() {
        return createIcon(FontAwesomeSolid.BOOK, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon quản lý môn học (Subject)
     * @returns FontIcon với icon book-open màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 19:56) - Cố định size 18px
     * --------------------------------------------------- */
    public static FontIcon createSubjectIcon() {
        return createIcon(FontAwesomeSolid.BOOK_OPEN, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon quản lý đề thi (Exam)
     * @returns FontIcon với icon file-alt màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 19:56) - Cố định size 18px
     * --------------------------------------------------- */
    public static FontIcon createExamIcon() {
        return createIcon(FontAwesomeSolid.FILE_ALT, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon chấm bài (Grading)
     * @returns FontIcon với icon edit màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 19:56) - Cố định size 18px
     * --------------------------------------------------- */
    public static FontIcon createGradingIcon() {
        return createIcon(FontAwesomeSolid.EDIT, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon giám sát thi (Monitoring)
     * @returns FontIcon với icon chart-bar màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 19:56) - Cố định size 18px
     * --------------------------------------------------- */
    public static FontIcon createMonitoringIcon() {
        return createIcon(FontAwesomeSolid.CHART_BAR, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon quản lý người dùng (User Management)
     * @returns FontIcon với icon users màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 19:56) - Cố định size 18px
     * --------------------------------------------------- */
    public static FontIcon createUserManagementIcon() {
        return createIcon(FontAwesomeSolid.USERS, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon quản lý tổ chức (Organization)
     * @returns FontIcon với icon building màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 19:56) - Cố định size 18px
     * --------------------------------------------------- */
    public static FontIcon createOrganizationIcon() {
        return createIcon(FontAwesomeSolid.BUILDING, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon cấu hình hệ thống (Settings)
     * @returns FontIcon với icon cog màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 19:56) - Cố định size 18px
     * --------------------------------------------------- */
    public static FontIcon createSettingsIcon() {
        return createIcon(FontAwesomeSolid.COG, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon Dashboard (Admin Dashboard)
     * @returns FontIcon với icon tachometer-alt màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public static FontIcon createDashboardIcon() {
        return createIcon(FontAwesomeSolid.TACHOMETER_ALT, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon Báo cáo (Reports)
     * @returns FontIcon với icon chart-pie màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * --------------------------------------------------- */
    public static FontIcon createReportsIcon() {
        return createIcon(FontAwesomeSolid.CHART_PIE, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon trợ giúp (Help)
     * @returns FontIcon với icon question-circle
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createHelpIcon() {
        return createIcon(FontAwesomeSolid.QUESTION_CIRCLE, SIZE_MEDIUM, COLOR_INFO);
    }
    
    /* ---------------------------------------------------
     * Tạo icon đăng xuất (Logout)
     * @returns FontIcon với icon sign-out-alt màu trắng, size 18px
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * EditBy: K24DTCN210-NVMANH (27/11/2025 19:56) - Cố định size 18px
     * --------------------------------------------------- */
    public static FontIcon createLogoutIcon() {
        return createIcon(FontAwesomeSolid.SIGN_OUT_ALT, MENU_ICON_SIZE, COLOR_WHITE);
    }
    
    /* ---------------------------------------------------
     * Tạo icon save
     * @returns FontIcon với icon save
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createSaveIcon() {
        return createIcon(FontAwesomeSolid.SAVE, SIZE_NORMAL, COLOR_SUCCESS);
    }
    
    /* ---------------------------------------------------
     * Tạo icon add/plus
     * @returns FontIcon với icon plus
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createAddIcon() {
        return createIcon(FontAwesomeSolid.PLUS, SIZE_NORMAL, COLOR_SUCCESS);
    }
    
    /* ---------------------------------------------------
     * Tạo icon delete/trash
     * @returns FontIcon với icon trash
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createDeleteIcon() {
        return createIcon(FontAwesomeSolid.TRASH, SIZE_NORMAL, COLOR_DANGER);
    }
    
    /* ---------------------------------------------------
     * Tạo icon edit/pencil
     * @returns FontIcon với icon pencil-alt
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createEditIcon() {
        return createIcon(FontAwesomeSolid.PENCIL_ALT, SIZE_NORMAL, COLOR_PRIMARY);
    }
    
    /* ---------------------------------------------------
     * Tạo icon search
     * @returns FontIcon với icon search
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createSearchIcon() {
        return createIcon(FontAwesomeSolid.SEARCH, SIZE_NORMAL, COLOR_GRAY);
    }
    
    /* ---------------------------------------------------
     * Tạo icon view/eye cho xem chi tiết
     * @returns FontIcon với icon eye, size 18px
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public static FontIcon createViewIcon() {
        return createIcon(FontAwesomeSolid.EYE, SIZE_MEDIUM, COLOR_PRIMARY);
    }
    
    /* ---------------------------------------------------
     * Tạo icon publish/bullhorn cho xuất bản
     * @returns FontIcon với icon bullhorn, size 18px
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public static FontIcon createPublishIcon() {
        return createIcon(FontAwesomeSolid.BULLHORN, SIZE_MEDIUM, COLOR_SUCCESS);
    }
    
    /* ---------------------------------------------------
     * Tạo icon lock cho ẩn/unpublish
     * @returns FontIcon với icon lock, size 18px
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * --------------------------------------------------- */
    public static FontIcon createLockIconForButton() {
        return createIcon(FontAwesomeSolid.LOCK, SIZE_MEDIUM, COLOR_WARNING);
    }
    
    /* ---------------------------------------------------
     * Tạo icon edit cho button, size 14px
     * @returns FontIcon với icon pencil-alt
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Giảm size xuống SIZE_SMALL
     * --------------------------------------------------- */
    public static FontIcon createEditIconForButton() {
        return createIcon(FontAwesomeSolid.PENCIL_ALT, SIZE_SMALL, COLOR_PRIMARY);
    }
    
    /* ---------------------------------------------------
     * Tạo icon delete cho button, size 14px
     * @returns FontIcon với icon trash
     * @author: K24DTCN210-NVMANH (30/11/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Giảm size xuống SIZE_SMALL
     * --------------------------------------------------- */
    public static FontIcon createDeleteIconForButton() {
        return createIcon(FontAwesomeSolid.TRASH, SIZE_SMALL, COLOR_DANGER);
    }
    
    /* ---------------------------------------------------
     * Tạo icon activate/check cho button, size 14px
     * @returns FontIcon với icon check-circle
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Giảm size xuống SIZE_SMALL
     * --------------------------------------------------- */
    public static FontIcon createActivateIcon() {
        return createIcon(FontAwesomeSolid.CHECK_CIRCLE, SIZE_SMALL, COLOR_SUCCESS);
    }
    
    /* ---------------------------------------------------
     * Tạo icon deactivate/ban cho button, size 14px
     * @returns FontIcon với icon ban
     * @author: K24DTCN210-NVMANH (02/12/2025)
     * EditBy: K24DTCN210-NVMANH (02/12/2025) - Giảm size xuống SIZE_SMALL
     * --------------------------------------------------- */
    public static FontIcon createDeactivateIcon() {
        return createIcon(FontAwesomeSolid.BAN, SIZE_SMALL, COLOR_WARNING);
    }
    
    /* ---------------------------------------------------
     * Tạo FontIcon từ Ikon với size và color tùy chỉnh
     * @param icon Ikon instance (từ FontAwesome, MaterialDesign, Bootstrap)
     * @param size Kích thước icon (pixels)
     * @param color Màu sắc của icon
     * @returns FontIcon đã được cấu hình
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createIcon(Ikon icon, int size, Color color) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(size);
        fontIcon.setIconColor(color);
        return fontIcon;
    }
    
    /* ---------------------------------------------------
     * Tạo FontIcon từ icon literal string (dùng trong FXML)
     * VD: "fas-user", "mdi2-home", "bi-heart"
     * @param iconLiteral String literal của icon
     * @param size Kích thước icon (pixels)
     * @param color Màu sắc của icon
     * @returns FontIcon đã được cấu hình
     * @author: K24DTCN210-NVMANH (27/11/2025 16:45)
     * --------------------------------------------------- */
    public static FontIcon createIconFromLiteral(String iconLiteral, int size, Color color) {
        FontIcon fontIcon = new FontIcon(iconLiteral);
        fontIcon.setIconSize(size);
        fontIcon.setIconColor(color);
        return fontIcon;
    }
}

## Brief overview
Guidelines for JavaFX Client UI development within the MS.TrustTest project, specifically focusing on standardized dialog usage to ensure consistent visual effects (dimmed background).

## Dialogs and Notifications
- **Strictly forbidden:** Do not instantiate `javafx.scene.control.Alert` directly for showing messages to the user.
- **Requirement:** ALWAYS use the `com.mstrust.client.util.DialogUtils` class for displaying any form of alert, notification, error message, or confirmation dialog. This ensures the dimmed background (overlay) effect is applied consistently across the application.

## Standard Methods
- **Errors:** Use `DialogUtils.showError(title, content)` or `DialogUtils.showError(title, header, content)`.
- **Information:** Use `DialogUtils.showInfo(title, content)`.
- **Warnings:** Use `DialogUtils.showWarning(title, content)`.
- **Confirmations:** Use `DialogUtils.showConfirmation(title, content)` or `DialogUtils.showConfirmation(title, header, content)`.
- **Custom/Generic:** Use `DialogUtils.showAlert(...)` for other alert types or when custom buttons are required.

package org.home.filesanalyzer.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

/**
 * Creates window alerts.
 */
public class DialogWindows {

    public static void showInformationAlert(String alertText) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("");
            alert.setHeaderText("");
            alert.setContentText(alertText);
            alert.showAndWait();
        });

    }
}

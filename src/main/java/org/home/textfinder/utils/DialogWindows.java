package org.home.textfinder.utils;

import javafx.scene.control.Alert;

/**
 * @author Sergei Viacheslaev
 */
public class DialogWindows {

    public static void showInformationAlert(String alertText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("");
        alert.setHeaderText("");
        alert.setContentText(alertText);
        alert.showAndWait();
    }
}

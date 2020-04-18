package org.home.filesanalyzer.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.home.filesanalyzer.config.AppConfig;

public class MenuAboutController {

    @FXML
    private TextArea system_info;
    @FXML
    private ImageView authorImage;

    @FXML
    void initialize() {
        String java_runtime = System.getProperties().getProperty("java.runtime.name");
        String java_version = System.getProperties().getProperty("java.version");
        String os_name = System.getProperties().getProperty("os.name");
        String os_version = System.getProperties().getProperty("os.version");
        String encoding = System.getProperty("file.encoding");
        String javafx_version = System.getProperty("javafx.version");

        system_info.setText(String.format("OS: %s %s%n%s%s%s%n%s",
                os_name,
                os_version,
                String.format("Java: %s %s%n", java_runtime, java_version),
                String.format("Encoding: %s%n", encoding),
                String.format("Available processors: %d%n", AppConfig.AVAILABLE_PROCESSORS),
                String.format("Powered by OpenJFX %s%n", javafx_version)));
    }

    /**
     * "Easter-egg"
     */
    @FXML
    private void showAuthorImage(MouseEvent event) {
        if (event.getClickCount() >= 2) {
            authorImage.setVisible(true);
        }
    }
}

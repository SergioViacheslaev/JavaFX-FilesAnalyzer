package org.home.textfinder;

import javafx.application.Application;

/**
 * Launcher for JAR.
 * <p>
 * If you run from Idea or -jar add VM options:
 * --module-path C:\Java\javafx-sdk-14\lib --add-modules javafx.controls,javafx.fxml
 * Choose your right path to javaFX sdk lib folder.
 *
 * @author Sergei Viacheslaev
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(TextFinderApp.class, args);
    }
}

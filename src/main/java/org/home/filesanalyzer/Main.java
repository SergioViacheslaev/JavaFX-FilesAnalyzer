package org.home.filesanalyzer;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;

/**
 * Launcher for JAR.
 * <p>
 * If you run from Idea or -jar add VM options:
 * --module-path C:\Java\javafx-sdk-14\lib --add-modules javafx.controls,javafx.fxml
 * Choose your right path to javaFX sdk lib folder.
 * <p>
 * Optimize work with setting JVM-heap size: -Xms256m -Xmx1024m
 *
 * @author Sergei Viacheslaev
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        Application.launch(FilesAnalyzerApp.class, args);
    }
}

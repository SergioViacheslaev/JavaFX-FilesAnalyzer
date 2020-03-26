package org.home.textfinder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.home.textfinder.api.Observable;
import org.home.textfinder.api.Observer;
import org.home.textfinder.config.AppConfig;
import org.home.textfinder.controllers.MainStageController;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * JavaFX App
 */
public class TextFinderApp extends Application implements Observer {
    private AppConfig config;
    private AnchorPane currentRootContainer;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfig.MAIN_STAGE_FXML_PATH));
        fxmlLoader.setResources(ResourceBundle.getBundle("bundles.locale", AppConfig.APP_LOCALE_RUSSIAN));
        currentRootContainer = fxmlLoader.load();
        Scene scene = new Scene(currentRootContainer);
        primaryStage.setScene(scene);
        this.primaryStage = primaryStage;

        config = new AppConfig(fxmlLoader.getResources());
        config.applyConfig(primaryStage);

        MainStageController controller = fxmlLoader.getController();
        controller.setAppConfig(config);
        controller.addObserver(this);

        primaryStage.show();
    }


    /**
     * Invokes when user changes language.
     *
     * @param o
     * @param arg
     */

    @SneakyThrows
    @Override
    public void update(Observable o, Object arg) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfig.MAIN_STAGE_FXML_PATH));
        fxmlLoader.setResources(ResourceBundle.getBundle("bundles.locale", new Locale((String) arg)));
        AnchorPane newNode = fxmlLoader.load();
        MainStageController controller = fxmlLoader.getController();
        currentRootContainer.getChildren().setAll(newNode.getChildren());

        config.setBundle(fxmlLoader.getResources());
        config.initStageParams(primaryStage);

        controller.setAppConfig(config);
        controller.addObserver(this);
    }

}
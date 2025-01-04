package org.home.filesanalyzer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.home.filesanalyzer.controllers.api.Observable;
import org.home.filesanalyzer.controllers.api.Observer;
import org.home.filesanalyzer.config.AppConfig;
import org.home.filesanalyzer.controllers.MainStageController;
import org.home.filesanalyzer.utils.StatusMessages;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class FilesAnalyzerApp extends Application implements Observer {
    private AppConfig config;
    private AnchorPane currentRootContainer;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfig.MAIN_STAGE_FXML_PATH));
        fxmlLoader.setResources(ResourceBundle.getBundle("bundles.locale", AppConfig.APP_LOCALE_RUSSIAN));
        AppConfig.setBundle(fxmlLoader.getResources());
        StatusMessages.setBundle(fxmlLoader.getResources());
        currentRootContainer = fxmlLoader.load();
        Scene scene = new Scene(currentRootContainer);
        primaryStage.setScene(scene);

        config = new AppConfig(primaryStage);
        config.initStageParams();

        MainStageController controller = fxmlLoader.getController();
        controller.setAppConfig(config);
        controller.setupStageListeners(primaryStage);
        controller.addObserver(this);

        primaryStage.show();
    }

    /**
     * Invokes when user changes language.
     * Loading new scene-node with updated language from bundles.
     *
     * @param observable   MainStageController
     * @param arg user's locale.
     */
    @SneakyThrows
    @Override
    public void update(Observable observable, Object arg) {
        ((MainStageController) observable).getExecutorService().shutdownNow();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfig.MAIN_STAGE_FXML_PATH));
        fxmlLoader.setResources(ResourceBundle.getBundle("bundles.locale", (Locale) arg));
        AppConfig.setBundle(fxmlLoader.getResources());
        StatusMessages.setBundle(fxmlLoader.getResources());
        AnchorPane newNode = fxmlLoader.load();

        MainStageController controller = fxmlLoader.getController();
        controller.setAppConfig(config);
        controller.setupStageListeners(config.getPrimaryStage());
        controller.addObserver(this);

        currentRootContainer.getChildren().setAll(newNode.getChildren());
    }

}
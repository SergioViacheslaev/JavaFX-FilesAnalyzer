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
import org.home.textfinder.utils.StatusMessages;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * JavaFX App
 */
public class LogAnalyzerApp extends Application implements Observer {
    private AppConfig config;
    private AnchorPane currentRootContainer;

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfig.MAIN_STAGE_FXML_PATH));
        fxmlLoader.setResources(ResourceBundle.getBundle("bundles.locale", AppConfig.APP_LOCALE_RUSSIAN));
        StatusMessages.setBundle(fxmlLoader.getResources());
        currentRootContainer = fxmlLoader.load();
        Scene scene = new Scene(currentRootContainer);
        primaryStage.setScene(scene);

        config = new AppConfig(fxmlLoader.getResources(), primaryStage);
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
     * @param o
     * @param arg
     */
    @SneakyThrows
    @Override
    public void update(Observable o, Object arg) {
        ((MainStageController) o).getExecutorService().shutdownNow();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfig.MAIN_STAGE_FXML_PATH));
        fxmlLoader.setResources(ResourceBundle.getBundle("bundles.locale", (Locale) arg));
        StatusMessages.setBundle(fxmlLoader.getResources());
        AnchorPane newNode = fxmlLoader.load();
        config.setBundle(fxmlLoader.getResources());

        MainStageController controller = fxmlLoader.getController();
        controller.setAppConfig(config);
        controller.setupStageListeners(config.getPrimaryStage());
        controller.addObserver(this);

        currentRootContainer.getChildren().setAll(newNode.getChildren());
    }

}
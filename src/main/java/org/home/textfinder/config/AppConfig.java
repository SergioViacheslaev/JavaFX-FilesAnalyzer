package org.home.textfinder.config;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;


import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Sergei Viacheslaev
 */
@Getter
@Setter
public class AppConfig {
    public static final String MAIN_STAGE_FXML_PATH = "/static/view/mainStage.fxml";
    public static final String MENU_ABOUT_FXML_PATH = "/static/view/menuAbout.fxml";
    public static final Locale APP_LOCALE_RUSSIAN = new Locale("ru");
    public static final Locale APP_LOCALE_ENGLISH = new Locale("en");
    private ResourceBundle bundle;

    public AppConfig(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public void applyConfig(Stage primaryStage) {
        initStageParams(primaryStage);
    }


    public void initStageParams(Stage primaryStage) {
        primaryStage.getIcons().add(new Image("static/images/logo.png"));
        primaryStage.setMinWidth(420);
        primaryStage.setMinHeight(450);
        primaryStage.setTitle(bundle.getString("appTitle"));

    }


}

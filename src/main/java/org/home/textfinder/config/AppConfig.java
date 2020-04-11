package org.home.textfinder.config;

import javafx.scene.image.Image;
import javafx.scene.text.Font;
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
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final String MAIN_STAGE_FXML_PATH = "/static/view/mainStage.fxml";
    public static final String MENU_ABOUT_FXML_PATH = "/static/view/menuAbout.fxml";
    public static final Locale APP_LOCALE_RUSSIAN = new Locale("ru");
    public static final Locale APP_LOCALE_ENGLISH = new Locale("en");
    private ResourceBundle bundle;
    private Stage primaryStage;

    public AppConfig(ResourceBundle bundle, Stage primaryStage) {
        this.bundle = bundle;
        this.primaryStage = primaryStage;
    }


    public void initStageParams() {
        primaryStage.getIcons().add(new Image("static/images/logo.png"));
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);
        primaryStage.setTitle(bundle.getString("appTitle"));
        primaryStage.getScene().getStylesheets().add( "/static/css/text-labels.css");
        primaryStage.getScene().getStylesheets().add( "/static/css/text-area.css");
        Font.loadFont(getClass().getResourceAsStream("/static/fonts/aver.ttf"),16);
    }


}

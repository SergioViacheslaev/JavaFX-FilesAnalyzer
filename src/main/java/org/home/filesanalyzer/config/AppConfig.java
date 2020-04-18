package org.home.filesanalyzer.config;

import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;
import java.util.ResourceBundle;

@Getter
@Setter
public class AppConfig {
    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    public static final String MAIN_STAGE_FXML_PATH = "/static/view/mainStage.fxml";
    public static final String MENU_ABOUT_FXML_PATH = "/static/view/menuAbout.fxml";
    public static final String ALL_OCCURENCES_WINDOW_FXML_PATH = "/static/view/showAllOccurrencesWindow.fxml";
    public static final Locale APP_LOCALE_RUSSIAN = new Locale("ru");
    public static final Locale APP_LOCALE_ENGLISH = new Locale("en");
    public static final String FOUND_TEXT_STYLE = "foundText";
    private static ResourceBundle bundle;
    private Stage primaryStage;

    public AppConfig(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }


    public void initStageParams() {
        primaryStage.getIcons().add(new Image("static/images/logo.png"));
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);
        primaryStage.setTitle(bundle.getString("appTitle"));
        primaryStage.getScene().getStylesheets().add("/static/css/text-labels.css");
        primaryStage.getScene().getStylesheets().add("/static/css/text-area.css");
        Font.loadFont(getClass().getResourceAsStream("/static/fonts/aver.ttf"), 16);
    }

    public static void setBundle(ResourceBundle bundle) {
        AppConfig.bundle = bundle;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }


}

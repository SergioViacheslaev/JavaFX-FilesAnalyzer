package org.home.textfinder.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.home.textfinder.api.Observable;
import org.home.textfinder.api.Observer;
import org.home.textfinder.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MainStageController implements Observable {
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final List<Observer> observers = new ArrayList<>();
    private AppConfig appConfig;

    @FXML
    private Button searchButton;
    @FXML
    private TextField searchPathTextField;


    @Override
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object arg) {
        observers.forEach(observer -> observer.update(this, arg));
    }

    @FXML
    void languageMenuEnglishAction(ActionEvent event) {
        notifyObservers(AppConfig.APP_LOCALE_ENGLISH);
    }

    @FXML
    void languageMenuRussianAction(ActionEvent event) {
        notifyObservers(AppConfig.APP_LOCALE_RUSSIAN);
    }


    @FXML
    void handleChooseSearchPathAction(MouseEvent event) {
        File searchPath = directoryChooser.showDialog(appConfig.getPrimaryStage());
        if (searchPath != null) {
            searchPathTextField.setText(searchPath.getAbsolutePath());
            searchButton.setDisable(false);
        }

    }

    @FXML
    void handleSearchAction(ActionEvent event) {

    }


    @FXML
    void showMenuAbout(ActionEvent event) {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource(AppConfig.MENU_ABOUT_FXML_PATH));
            stage.setTitle(appConfig.getBundle().getString("menu.about"));
            stage.setMinHeight(250);
            stage.setMinWidth(300);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void initialize() {

    }


}

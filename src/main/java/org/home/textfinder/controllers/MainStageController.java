package org.home.textfinder.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.home.textfinder.api.Observable;
import org.home.textfinder.api.Observer;
import org.home.textfinder.config.AppConfig;
import org.home.textfinder.utils.DialogWindows;
import org.home.textfinder.utils.FileTreeUtils;
import org.home.textfinder.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.home.textfinder.utils.DialogWindows.showInformationAlert;

@Getter
@Setter
public class MainStageController implements Observable {
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final List<Observer> observers = new ArrayList<>();
    private AppConfig appConfig;
    private ResourceBundle bundle;

    @FXML
    private Button searchButton;
    @FXML
    private TextField searchPathTextField;
    @FXML
    private TreeView<String> searchResultTree;
    @FXML
    private TextArea fileContentTextArea;
    @FXML
    private TextField fileExtensionTextField;
    @FXML
    private TextField fileMaskTextField;
    @FXML
    private TextField fileContentSearchTextField;
    @FXML
    private RadioButton enableFileMaskRadioButton;


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
    void handleFileMaskSearchAction(ActionEvent event) {
        final RadioButton fileMaskRadioButton = (RadioButton) event.getSource();
        if (fileMaskRadioButton.isSelected()) {
            fileExtensionTextField.setDisable(true);
            fileContentSearchTextField.setDisable(true);
            fileMaskTextField.setDisable(false);
            searchPathTextField.setDisable(false);
        } else {
            fileExtensionTextField.setDisable(false);
            fileContentSearchTextField.setDisable(false);
            fileMaskTextField.setDisable(true);
        }

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
        String searchPath = searchPathTextField.getText();
        File searchCatalog = new File(searchPath);

        if (enableFileMaskRadioButton.isSelected()) {
            if (!searchPath.isEmpty() && searchCatalog.exists()) {
                TreeItem<String> rootItem = new TreeItem<>(searchPath);
                rootItem.setExpanded(true);
                searchResultTree.setRoot(rootItem);
                FileTreeUtils.buildFilesMaskedTree(rootItem, fileMaskTextField.getText());
            } else {
                DialogWindows.showInformationAlert("Путь указан неверно !");
            }
        } else {
            if (!searchPath.isEmpty() && searchCatalog.exists()) {
                TreeItem<String> rootItem = new TreeItem<>(searchPath);
                rootItem.setExpanded(true);
                searchResultTree.setRoot(rootItem);
                FileTreeUtils.buildFilesWithExtensionsTree(rootItem, fileExtensionTextField.getText());
            } else {
                DialogWindows.showInformationAlert("Путь указан неверно !");
            }
        }

    }

    @FXML
    void handleMouseClickedTreeItemAction(MouseEvent event) {
        final TreeItem<String> selectedItem = searchResultTree.getSelectionModel().getSelectedItem();
        final String filePath = selectedItem.getValue();

        if (Files.isRegularFile(Paths.get(filePath))) {
            String fileContent = FileUtils.getFileContent(filePath);
            if (!fileContent.isEmpty()) {
                fileContentTextArea.setText(fileContent);
            } else {
                fileContentTextArea.setText("");
                showInformationAlert(bundle.getString("alert.FileEmpty"));
            }
        }
    }

    /*    */

    /**
     * Builds files tree, searched with specified extensions.
     *//*
    @SneakyThrows
    private void buildFilesWithExtensionsTree(TreeItem<String> rootItem) {
        String fileExtension = fileExtensionTextField.getText();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(rootItem.getValue()))) {
            for (Path path : directoryStream) {
                TreeItem<String> treeItem = new TreeItem<>(path.toString());
                treeItem.setExpanded(true);

                if (Files.isDirectory(path)) {
                    rootItem.getChildren().add(treeItem);
                    buildFilesWithExtensionsTree(treeItem);
                    removeEmptyTreeItem(rootItem);

                } else if (path.toString().endsWith(fileExtension)) {
                    rootItem.getChildren().add(treeItem);
                }
            }
        }
    }*/
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

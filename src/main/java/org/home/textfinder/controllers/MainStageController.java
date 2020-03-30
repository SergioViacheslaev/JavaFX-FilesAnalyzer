package org.home.textfinder.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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
import org.home.textfinder.utils.Icons;

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
    @FXML
    private RadioButton enableFileContentRadioButton;


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
        final RadioButton currentButton = (RadioButton) event.getSource();
        if (currentButton.isSelected()) {
            fileExtensionTextField.setDisable(true);
            fileContentSearchTextField.setDisable(true);
            fileMaskTextField.setDisable(false);
            searchPathTextField.setDisable(false);
            fileContentSearchTextField.setDisable(false);
        } else {
            fileExtensionTextField.setDisable(false);
            fileContentSearchTextField.setDisable(false);
            fileMaskTextField.setDisable(true);
        }

    }

    @FXML
    void handleFileContentSearchAction(ActionEvent event) {
        final RadioButton currentButton = (RadioButton) event.getSource();
        if (currentButton.isSelected()) {
            fileContentSearchTextField.setDisable(false);
        } else {
            fileContentSearchTextField.setDisable(true);
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
        fileContentTextArea.setText("");
        String searchText = fileContentSearchTextField.getText().trim();
        String searchPath = searchPathTextField.getText();
        File searchCatalog = new File(searchPath);
        TreeItem<String> rootItem = new TreeItem<>(searchPath, new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
        rootItem.setExpanded(true);
        searchResultTree.setRoot(rootItem);

        if (searchPath.isEmpty() && !searchCatalog.exists()) {
            DialogWindows.showInformationAlert("Путь указан неверно !");
            return;
        }

        if (enableFileContentRadioButton.isSelected() && searchText.isEmpty()) {
            DialogWindows.showInformationAlert("Не задан текст поиска !");
            return;
        }

        if (enableFileMaskRadioButton.isSelected()) {
            FileTreeUtils.buildFilesMaskedTree(rootItem, fileMaskTextField.getText());
        } else if (enableFileContentRadioButton.isSelected()) {
            FileTreeUtils.buildFilesWithContentTree(rootItem, fileExtensionTextField.getText(), searchText);
        } else {
            FileTreeUtils.buildFilesWithExtensionsTree(rootItem, fileExtensionTextField.getText());
        }

    }

    @FXML
    void handleMouseClickedTreeItemAction(MouseEvent event) {
        final TreeItem<String> selectedItem = searchResultTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {

            final String filePath = selectedItem.getValue();

            if (Files.isRegularFile(Paths.get(filePath))) {
                try {
                    String fileContent = FileUtils.getFileContent(filePath);
                    if (!fileContent.isEmpty()) {
                        fileContentTextArea.setText(fileContent);
                    } else {
                        fileContentTextArea.setText("");
                        showInformationAlert(bundle.getString("alert.FileEmpty"));
                    }
                } catch (IOException e) {
                    DialogWindows.showInformationAlert("Не могу прочитать этот файл !");
                }
            }
        }
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

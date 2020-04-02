package org.home.textfinder.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
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
import org.home.textfinder.utils.Icons;
import org.home.textfinder.utils.TabPaneUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class MainStageController implements Observable {
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final List<Observer> observers = new ArrayList<>();
    private AppConfig appConfig;
    private ResourceBundle bundle;
    private ExecutorService executor = Executors.newFixedThreadPool(4);


    @FXML
    private Button searchButton;
    @FXML
    private TextField searchPathTextField;
    @FXML
    private TreeView<String> firstSearchResultTree;
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
    @FXML
    private TabPane resultsTabPane;
    @FXML
    private AnchorPane searchResultAnchorPane;


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
    private void languageMenuEnglishAction(ActionEvent event) {
        notifyObservers(AppConfig.APP_LOCALE_ENGLISH);
    }

    @FXML
    private void languageMenuRussianAction(ActionEvent event) {
        notifyObservers(AppConfig.APP_LOCALE_RUSSIAN);
    }

    @FXML
    private void initialize() {
        searchButton.setOnAction(event -> startSearchTask());
    }


    @FXML
    private void handleFileMaskSearchAction(ActionEvent event) {
        final RadioButton currentButton = (RadioButton) event.getSource();
        if (currentButton.isSelected()) {
            fileExtensionTextField.setDisable(true);
            fileContentSearchTextField.setDisable(true);
            fileMaskTextField.setDisable(false);
            searchPathTextField.setDisable(false);
            enableFileContentRadioButton.setSelected(true);
            fileContentSearchTextField.setDisable(false);
        } else {
            fileExtensionTextField.setDisable(false);
            fileContentSearchTextField.setDisable(false);
            fileMaskTextField.setDisable(true);
        }

    }

    @FXML
    private void handleFileContentSearchAction(ActionEvent event) {
        final RadioButton currentButton = (RadioButton) event.getSource();
        if (currentButton.isSelected()) {
            fileContentSearchTextField.setDisable(false);
        } else {
            fileContentSearchTextField.setDisable(true);
        }

    }


    @FXML
    private void handleChooseSearchPathAction(MouseEvent event) {
        File searchPath = directoryChooser.showDialog(appConfig.getPrimaryStage());
        if (searchPath != null) {
            searchPathTextField.setText(searchPath.getAbsolutePath());
            searchButton.setDisable(false);
        }
    }

    @FXML
    private void handleMouseClickedTreeItemAction(MouseEvent event) {
        final TreeItem<String> selectedItem = firstSearchResultTree.getSelectionModel().getSelectedItem();
        FileTreeUtils.handleSelectedItemAction(selectedItem, fileContentTextArea, bundle);
    }

    @FXML
    private void showMenuAbout(ActionEvent event) {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource(AppConfig.MENU_ABOUT_FXML_PATH));
            stage.setTitle(bundle.getString("menu.about"));
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

    public void setupListeners(Stage primaryStage) {
        primaryStage.setOnCloseRequest((closeEvent) -> executor.shutdown());
    }


    private void startSearchTask() {
        Runnable searchTask = () -> {
            TreeView<String> searchResultsView = new TreeView<>();
            fileContentTextArea.setText("");
            String searchText = fileContentSearchTextField.getText().trim();
            String searchPath = searchPathTextField.getText();
            File searchCatalog = new File(searchPath);

            TreeItem<String> rootItem = new TreeItem<>(searchPath, new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
            rootItem.setExpanded(true);


            if (searchPath.isEmpty() && !searchCatalog.exists()) {
                DialogWindows.showInformationAlert("Путь указан неверно !");
                return;
            }

            if (enableFileContentRadioButton.isSelected() && searchText.isEmpty()) {
                DialogWindows.showInformationAlert("Не задан текст поиска !");
                return;
            }


            if (enableFileMaskRadioButton.isSelected() && fileMaskTextField.getText().trim().isEmpty()) {
                DialogWindows.showInformationAlert("Не задана маска названия файла!");
                return;
            }

            if (resultsTabPane.getTabs().size() < 4 && firstSearchResultTree.getRoot() != null) {
                performResultsView(searchResultsView);
            }

            if (enableFileMaskRadioButton.isSelected()) {
                FileTreeUtils.buildFilesMaskedTree(rootItem, fileMaskTextField.getText(), searchText);
            } else if (enableFileContentRadioButton.isSelected()) {
                FileTreeUtils.buildFilesWithContentTree(rootItem, fileExtensionTextField.getText(), searchText);
            } else {
                FileTreeUtils.buildFilesWithExtensionsTree(rootItem, fileExtensionTextField.getText());
            }

            //First search and others generated Tabs.
            if (resultsTabPane.getTabs().size() == 1) {
                Platform.runLater(() -> firstSearchResultTree.setRoot(rootItem));
            } else {
                Platform.runLater(() -> searchResultsView.setRoot(rootItem));
            }

        };
        executor.execute(searchTask);
    }


    private void performResultsView(TreeView<String> searchResultsView) {

        TextArea fileContent = new TextArea();
        searchResultsView.addEventHandler(MouseEvent.MOUSE_CLICKED, clickEvent -> {
            final TreeItem<String> selectedItem = searchResultsView.getSelectionModel().getSelectedItem();
            FileTreeUtils.handleSelectedItemAction(selectedItem, fileContent, bundle);
        });

        final Tab newTab = TabPaneUtils.addTab(resultsTabPane, "Поиск #" + (resultsTabPane.getTabs().size() + 1));
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.32d);
        splitPane.getItems().addAll(searchResultsView, fileContent);
        Platform.runLater(() -> {
            newTab.setContent(splitPane);
            resultsTabPane.getSelectionModel().select(newTab);
        });
    }


}

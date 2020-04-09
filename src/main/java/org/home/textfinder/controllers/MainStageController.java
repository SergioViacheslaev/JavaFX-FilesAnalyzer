package org.home.textfinder.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import lombok.SneakyThrows;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.home.textfinder.api.Observable;
import org.home.textfinder.api.Observer;
import org.home.textfinder.config.AppConfig;
import org.home.textfinder.utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.home.textfinder.utils.DialogWindows.showInformationAlert;

@Getter
@Setter
public class MainStageController implements Observable {
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final List<Observer> observers = new ArrayList<>();
    private AppConfig appConfig;
    private ResourceBundle bundle;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private String currentFilePath;


    @FXML
    private Button searchButton;
    @FXML
    private TextField searchPathTextField;
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
    private Button readBackButton;
    @FXML
    private Button readForwardButton;
    @FXML
    private CheckBox oneTabModeCheckBox;


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
        oneTabModeCheckBox.setSelected(true);
        oneTabModeCheckBox.setDisable(true);
        oneTabModeCheckBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final CheckBox checkBox = (CheckBox) event.getSource();
                if (checkBox.isSelected()) {
                    final int tabsCount = resultsTabPane.getTabs().size();
                    if (tabsCount > 1) {
                        resultsTabPane.getTabs().remove(1, tabsCount);
                    }
                }
            }
        });
        performResultsView(new TreeView<>());


        //todo: Temp for testing
        searchPathTextField.setText("D:\\Downloads\\HDFS_2");
        searchButton.setDisable(false);


        readForwardButton.setOnAction(new EventHandler<ActionEvent>() {
            @SneakyThrows
            @Override
            public void handle(ActionEvent event) {
                final Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
                final SplitPane splitPane = (SplitPane) selectedTab.getContent();
                splitPane.getItems().remove(1);
                StyleClassedTextArea textArea = new StyleClassedTextArea();
                textArea.appendText(FileUtils.getNextPageContent(currentFilePath));

                splitPane.getItems().add(textArea);

            }
        });


        searchButton.setOnAction(event -> startSearchTask());


    }


    @FXML
    private void handleFileMaskSetAction(ActionEvent event) {
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
    private void handleFileContentSetAction(ActionEvent event) {
        final RadioButton currentButton = (RadioButton) event.getSource();
        if (currentButton.isSelected()) {
            fileContentSearchTextField.setDisable(false);
        } else {
            fileContentSearchTextField.setDisable(true);
        }

    }


    @FXML
    private void handleSearchPathSetAction(MouseEvent event) {
        File searchPath = directoryChooser.showDialog(appConfig.getPrimaryStage());
        if (searchPath != null) {
            searchPathTextField.setText(searchPath.getAbsolutePath());
            searchButton.setDisable(false);
        }
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
        primaryStage.setOnCloseRequest((closeEvent) -> executorService.shutdown());
    }


    private void startSearchTask() {
        Runnable searchTask = () -> {
            TreeView<String> searchResultsView = new TreeView<>();
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

            if ((resultsTabPane.getTabs().size() >= 1 && resultsTabPane.getTabs().size() <= 4) && !oneTabModeCheckBox.isDisabled()) {
                performResultsView(searchResultsView);
            }


            if (enableFileMaskRadioButton.isSelected()) {
                FileTreeUtils.buildFilesMaskedTree(rootItem, fileMaskTextField.getText(), searchText);
            } else if (enableFileContentRadioButton.isSelected()) {
                FileTreeUtils.buildFilesWithContentTree(rootItem, fileExtensionTextField.getText(), searchText);
            } else {
                FileTreeUtils.buildFilesWithExtensionsTree(rootItem, fileExtensionTextField.getText());
            }

            if (oneTabModeCheckBox.isSelected() && resultsTabPane.getTabs().size() == 1) {
                final Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
                final SplitPane splitPane = (SplitPane) selectedTab.getContent();
                TreeView<String> firstSearchFilesView = (TreeView<String>) splitPane.getItems().get(0);
                oneTabModeCheckBox.setDisable(false);
                Platform.runLater(() -> firstSearchFilesView.setRoot(rootItem));
            } else {
                Platform.runLater(() -> searchResultsView.setRoot(rootItem));
            }

        };
        executorService.execute(searchTask);

    }


    private void performResultsView(TreeView<String> searchResultsView) {

        CodeArea fileContentArea = new CodeArea();
        fileContentArea.setVisible(false);
        fileContentArea.setParagraphGraphicFactory(LineNumberFactory.get(fileContentArea));
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(fileContentArea);
        Tab newTab = TabPaneUtils.addTab(resultsTabPane);


        searchResultsView.addEventHandler(MouseEvent.MOUSE_CLICKED, clickEvent -> {
            final TreeItem<String> selectedFilepath = searchResultsView.getSelectionModel().getSelectedItem();
            if (selectedFilepath != null) {

                final Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
                final SplitPane splitPane = (SplitPane) selectedTab.getContent();
                final ObservableList<Node> items = splitPane.getItems();
                splitPane.getItems().remove(1);
                CodeArea codeArea = new CodeArea();
                codeArea.setParagraphGraphicFactory(LineNumberFactory.get(fileContentArea));
                VirtualizedScrollPane<CodeArea> virtualizedScrollPane = new VirtualizedScrollPane<>(codeArea);
                splitPane.getItems().add(virtualizedScrollPane);

                currentFilePath = selectedFilepath.getValue();
                showSelectedFile(selectedFilepath, codeArea, bundle);
            }
        });


        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.32d);
        splitPane.getItems().addAll(searchResultsView, scrollPane);
        Platform.runLater(() -> {
            newTab.setContent(splitPane);
            resultsTabPane.getSelectionModel().select(newTab);
        });
    }

    private void showSelectedFile(TreeItem<String> selectedItem, CodeArea fileContentTextArea, ResourceBundle bundle) {
        Runnable readFileTask = () -> {
            if (selectedItem != null) {

                final String filePath = selectedItem.getValue();

                if (Files.isRegularFile(Paths.get(filePath))) {
                    try {
                        String fileContent = FileUtils.getFileContent(filePath);

                        if (!fileContent.isEmpty()) {
                            Platform.runLater(() -> {
                                fileContentTextArea.replaceText(fileContent);
                                fileContentTextArea.setVisible(true);
                            });

                        } else {
                            showInformationAlert(bundle.getString("alert.FileEmpty"));
                            Platform.runLater(() -> fileContentTextArea.setVisible(false));
                        }

                    } catch (IOException e) {
                        showInformationAlert("Не могу прочитать этот файл !");
                    }
                }
            }
        };

        executorService.execute(readFileTask);

    }


}

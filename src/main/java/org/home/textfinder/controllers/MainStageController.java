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
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
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
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.home.textfinder.utils.DialogWindows.showInformationAlert;

@Getter
@Setter
public class MainStageController implements Observable {
    public static final double DIVIDER_POSITION = 0.24;
    public static final int CODE_AREA_INDEX = 1;
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final List<Observer> observers = new ArrayList<>();
    private AppConfig appConfig;
    private ResourceBundle bundle;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private String currentFilePath;

    @FXML
    private AnchorPane controlPanel;
    @FXML
    private Text fileStatusText;
    @FXML
    private Text pageStatusText;
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
    @FXML
    private CheckBox largeFileModeCheckBox;


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


        largeFileModeCheckBox.setOnAction(event -> {
            final CheckBox largeFile = (CheckBox) event.getSource();
            if (largeFile.isSelected()) {

                DialogWindows.showInformationAlert("В данном режиме: файл любого размера загружается полностью в ОЗУ, отслеживайте наличие свободной памяти и параметры запуска JVM: -Xms -Xmx");
                resultsTabPane.getTabs().remove(1, resultsTabPane.getTabs().size());
                oneTabModeCheckBox.setSelected(true);
                oneTabModeCheckBox.setDisable(true);

                largeFile.setOnAction(action -> {
                    if (largeFile.isSelected()) {
                        resultsTabPane.getTabs().remove(1, resultsTabPane.getTabs().size());
                        oneTabModeCheckBox.setSelected(true);
                        oneTabModeCheckBox.setDisable(true);
                    } else {
                        oneTabModeCheckBox.setDisable(false);
                    }
                });
            }
        });


        oneTabModeCheckBox.setOnAction(event -> {
            final CheckBox oneTab = (CheckBox) event.getSource();
            if (oneTab.isSelected()) {
                final int tabsCount = resultsTabPane.getTabs().size();
                if (tabsCount > 1) {
                    resultsTabPane.getTabs().remove(1, tabsCount);
                }
            }
        });

        performResultsViewAndListeners(new TreeView<>());


        //todo: Temp for testing
        searchPathTextField.setText("D:\\Downloads\\HDFS_2");
        searchButton.setDisable(false);


        readForwardButton.setOnAction(event -> {
            final Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
            CodeArea codeArea = performFileViewArea(selectedTab);
            Map<Boolean, String> fileContent = FileUtils.getNextPageContent(currentFilePath);
            if (!fileContent.isEmpty()) {
                pageStatusText.setText(StatusMessages.getFilePagesStatus());
                boolean hasMorePages = fileContent.keySet().iterator().next();
                codeArea.appendText(fileContent.get(hasMorePages));
                if (!hasMorePages) {
                    readForwardButton.setDisable(true);
                }
                readBackButton.setDisable(false);
            }
        });
        readForwardButton.setTooltip(new Tooltip("Загрузить следующие 50 MB"));

        readBackButton.setOnAction(event -> {
            final Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
            CodeArea codeArea = performFileViewArea(selectedTab);
            Map<Boolean, String> fileContent = FileUtils.getPreviousPageContent(currentFilePath);
            if (!fileContent.isEmpty()) {
                pageStatusText.setText(StatusMessages.getFilePagesStatus());
                boolean hasPreviousPage = fileContent.keySet().iterator().next();
                codeArea.appendText(fileContent.get(hasPreviousPage));
                if (!hasPreviousPage) {
                    readBackButton.setDisable(true);
                }
                readForwardButton.setDisable(false);
            }
        });
        readBackButton.setTooltip(new Tooltip("Загрузить предыдущие 50 MB"));


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

            if (!oneTabModeCheckBox.isSelected()) {
                performResultsViewAndListeners(searchResultsView);
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


    private void performResultsViewAndListeners(TreeView<String> searchResultsView) {
        CodeArea fileContentArea = new CodeArea();
        fileContentArea.setVisible(false);
        fileContentArea.setParagraphGraphicFactory(LineNumberFactory.get(fileContentArea));
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(fileContentArea);
        Tab newTab = TabPaneUtils.addTab(resultsTabPane);

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(searchResultsView, scrollPane);
        splitPane.setDividerPositions(DIVIDER_POSITION);

        Platform.runLater(() -> {
            newTab.setContent(splitPane);
            resultsTabPane.getSelectionModel().select(newTab);
        });

        searchResultsView.addEventHandler(MouseEvent.MOUSE_CLICKED, clickEvent -> {
            final TreeItem<String> selectedFilepath = searchResultsView.getSelectionModel().getSelectedItem();
            if (selectedFilepath != null) {
                currentFilePath = selectedFilepath.getValue();

                final Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
                CodeArea codeArea = performFileViewArea(selectedTab);
                showSelectedFile(selectedFilepath, codeArea, bundle);
            }
        });


    }

    private void showSelectedFile(TreeItem<String> selectedItem, CodeArea
            fileContentTextArea, ResourceBundle bundle) {
        if (selectedItem != null) {
            final String filePath = selectedItem.getValue();
            if (Files.isRegularFile(Paths.get(filePath))) {
                try {
                    long fileSize = Files.size(Paths.get(filePath));
                    fileStatusText.setText(StatusMessages.getFileSizeStatus(fileSize));


                    if (largeFileModeCheckBox.isSelected()) {
                        Platform.runLater(() -> {
                            fileContentTextArea.replaceText(FileUtils.getLargeFileContent(filePath));
                        });
                        return;
                    }

                    if (fileSize > FileUtils.FIZE_SIZE_LIMIT) {

                        String filePageContent= FileUtils.getFirstPageContent(filePath);
                        pageStatusText.setText(StatusMessages.getFilePagesStatus());
                        if (!filePageContent.isEmpty()) {
                            readForwardButton.setDisable(false);
                            readBackButton.setDisable(true);
                            Platform.runLater(() -> {
                                fileContentTextArea.replaceText(filePageContent);
                                fileContentTextArea.setVisible(true);
                            });
                        }
                    } else {
                        readForwardButton.setDisable(true);
                        readBackButton.setDisable(true);
                        String fileFullContent = FileUtils.getFileContent(filePath);
                        if (!fileFullContent.isEmpty()) {
                            Platform.runLater(() -> {
                                fileContentTextArea.replaceText(fileFullContent);
                                fileContentTextArea.setVisible(true);
                            });
                        } else {
                            showInformationAlert(bundle.getString("alert.FileEmpty"));
                            Platform.runLater(() -> fileContentTextArea.setVisible(false));
                        }
                    }

                } catch (IOException e) {
                    showInformationAlert("Не могу прочитать этот файл !");
                }
            }
        }

    }

    private CodeArea performFileViewArea(Tab selectedTab) {
        final SplitPane currentPane = (SplitPane) selectedTab.getContent();
        currentPane.getItems().remove(CODE_AREA_INDEX);
        CodeArea codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        currentPane.getItems().add(scrollPane);
        currentPane.setDividerPositions(DIVIDER_POSITION);

        scrollPane.setDisable(false);
        return codeArea;
    }


}

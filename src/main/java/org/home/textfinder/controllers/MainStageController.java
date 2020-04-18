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
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.home.textfinder.api.Observable;
import org.home.textfinder.api.Observer;
import org.home.textfinder.config.AppConfig;
import org.home.textfinder.model.SearchedTextData;
import org.home.textfinder.utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.home.textfinder.utils.DialogWindows.showInformationAlert;

@Getter
@Setter
public class MainStageController implements Observable {
    public static final double DIVIDER_POSITION = 0.24;
    public static final int CODE_AREA_INDEX = 1;
    private final WeakHashMap<Tab, SearchedTextData> tabsSearchTextMap = new WeakHashMap<>();
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final List<Observer> observers = new ArrayList<>();
    private AppConfig appConfig;
    private ResourceBundle bundle;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private String currentFilePath;

    @FXML
    private AnchorPane controlPanel;
    @FXML
    private TabPane resultsTabPane;
    @FXML
    private Text fileStatusText;
    @FXML
    private Text pageStatusText;
    @FXML
    private Button searchButton;
    @FXML
    private Button searchTextButton;
    @FXML
    private Button exitPageReadingModeButton;
    @FXML
    private Button readBackButton;
    @FXML
    private Button readForwardButton;
    @FXML
    private Button showAllTextOccurrencesButton;
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
    private CheckBox oneTabModeCheckBox;
    @FXML
    private CheckBox largeFileModeCheckBox;


    @FXML
    private void initialize() {
        bundle = StatusMessages.getBundle();
//        searchPathTextField.setText("D:\\Downloads\\HDFS_2\\test");
//        searchButton.setDisable(false);

        setupControlsListeners();
        performResultsView(new TreeView<>());
        setupControlsTips();

    }

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
    private void handleFileMaskSetAction(ActionEvent event) {
        final RadioButton currentButton = (RadioButton) event.getSource();
        if (currentButton.isSelected()) {
            fileExtensionTextField.setDisable(true);
            fileMaskTextField.setDisable(false);
            searchPathTextField.setDisable(false);
        } else {
            fileExtensionTextField.setDisable(false);
            fileMaskTextField.setDisable(true);
        }

    }

    @FXML
    private void handleFileContentSetAction(ActionEvent event) {
        final RadioButton currentButton = (RadioButton) event.getSource();
        if (currentButton.isSelected()) {
            fileContentSearchTextField.setDisable(false);
            searchTextButton.setDisable(false);
            showAllTextOccurrencesButton.setDisable(false);
        } else {
            fileContentSearchTextField.setDisable(true);
            searchTextButton.setDisable(true);
            showAllTextOccurrencesButton.setDisable(true);
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

    public void setupStageListeners(Stage primaryStage) {
        primaryStage.setOnCloseRequest((closeEvent) -> {
            executorService.shutdownNow();
        });
    }


    private void startSearchTask() {
        Runnable searchTask = () -> {
            final Tab currentTab;
            final String tabTitle;
            TreeView<String> searchResultsView = new TreeView<>();
            String searchText = fileContentSearchTextField.getText().trim();
            String searchPath = searchPathTextField.getText();
            File searchCatalog = new File(searchPath);
            TreeItem<String> rootItem = new TreeItem<>(searchPath, new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
            rootItem.setExpanded(true);

            if (searchPath.isEmpty() || !searchCatalog.exists()) {
                DialogWindows.showInformationAlert(bundle.getString("alert.badPath"));
                return;
            }
            if (enableFileContentRadioButton.isSelected() && searchText.isEmpty()) {
                DialogWindows.showInformationAlert(bundle.getString("alert.noSearchedText"));
                return;
            }
            if (enableFileMaskRadioButton.isSelected() && fileMaskTextField.getText().trim().isEmpty()) {
                DialogWindows.showInformationAlert(bundle.getString("alert.noFileMask"));
                return;
            }

            if (!oneTabModeCheckBox.isSelected()) {
                currentTab = performResultsView(searchResultsView);
                tabTitle = currentTab.getText();
                Platform.runLater(() -> currentTab.setText(bundle.getString("tab.Search")));
            } else {
                currentTab = resultsTabPane.getSelectionModel().getSelectedItem();
                currentTab.setDisable(true);
                tabTitle = currentTab.getText();
                Platform.runLater(() -> currentTab.setText(bundle.getString("tab.Search")));
            }

            if (enableFileMaskRadioButton.isSelected() && !enableFileContentRadioButton.isSelected()) {
                FileTreeUtils.buildFilesMaskedTree(rootItem, fileMaskTextField.getText());
            } else if (enableFileMaskRadioButton.isSelected() && enableFileContentRadioButton.isSelected()) {
                FileTreeUtils.buildFilesMaskedContentTree(rootItem, fileMaskTextField.getText(), searchText);
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
                Platform.runLater(() -> {
                    firstSearchFilesView.setRoot(rootItem);
                    selectedTab.setText(tabTitle);
                    currentTab.setDisable(false);
                });
            } else {
                Platform.runLater(() -> {
                    searchResultsView.setRoot(rootItem);
                    currentTab.setText(tabTitle);
                    currentTab.setDisable(false);
                });
            }
        };
        executorService.execute(searchTask);

    }


    private Tab performResultsView(TreeView<String> searchResultsView) {
        CodeArea fileContentArea = new CodeArea();
        fileContentArea.setVisible(false);
        fileContentArea.setParagraphGraphicFactory(LineNumberFactory.get(fileContentArea));
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(fileContentArea);
        Tab newTab = TabPaneUtils.addTab(resultsTabPane);
        newTab.setDisable(true);
        tabsSearchTextMap.put(newTab, new SearchedTextData());

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


        return newTab;
    }

    private void showSelectedFile(TreeItem<String> selectedItem, CodeArea
            fileContentTextArea, ResourceBundle bundle) {
        if (selectedItem != null) {
            final String filePath = selectedItem.getValue();
            if (Files.isRegularFile(Paths.get(filePath))) {
                try {
                    long fileSize = Files.size(Paths.get(filePath));
                    fileStatusText.setText(StatusMessages.getFileSizeStatus(fileSize));
                    pageStatusText.setText("");
                    if (largeFileModeCheckBox.isSelected()) {
                        Platform.runLater(() -> {
                            fileContentTextArea.replaceText(FileUtils.getLargeFileContent(filePath));
                            pageStatusText.setText(bundle.getString("file.fullyLoaded"));
                        });
                        return;
                    }
                    if (fileSize > FileUtils.FIZE_SIZE_LIMIT) {
                        if (!oneTabModeCheckBox.isSelected()) {
                            DialogWindows.showInformationAlert(bundle.getString("alert.pagesOnlyOneTabMode"));
                            return;
                        }
                        String filePageContent = FileUtils.getFirstPageContent(filePath);
                        pageStatusText.setText(StatusMessages.getFilePagesStatus());
                        if (!filePageContent.isEmpty()) {
                            oneTabModeCheckBox.setDisable(true);
                            largeFileModeCheckBox.setDisable(true);
                            exitPageReadingModeButton.setDisable(false);
                            readForwardButton.setDisable(false);
                            readBackButton.setDisable(true);
                            searchButton.setDisable(true);
                            searchPathTextField.setDisable(true);
                            enableFileMaskRadioButton.setDisable(true);
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
                    showInformationAlert(bundle.getString("alert.cantReadFile"));
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


    private void setupControlsListeners() {
        searchTextButton.setOnAction(event -> {
            String searchText = fileContentSearchTextField.getText();
            if (!searchText.isEmpty()) {
                final Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
                final SplitPane currentPane = (SplitPane) selectedTab.getContent();
                final VirtualizedScrollPane<CodeArea> scrollPane = (VirtualizedScrollPane<CodeArea>) currentPane.getItems().get(1);
                final CodeArea fileContentArea = scrollPane.getContent();

                SearchedTextData searchedTextData = tabsSearchTextMap.get(selectedTab);
                String text = searchedTextData.getText();
                if (!searchText.equals(text)) {
                    searchedTextData.setText(searchText);
                    searchedTextData.setPosition(0);
                }

                int caretPosition = StringUtils.indexOfIgnoreCase(fileContentArea.getText(), searchText, searchedTextData.getPosition());
                if (caretPosition > 0) {
                    fileContentArea.showCaretProperty();
                    searchedTextData.setPosition(caretPosition + searchText.length());
                    fileContentArea.moveTo(caretPosition + searchText.length());
                    fileContentArea.requestFollowCaret();
                    fileContentArea.setStyle(caretPosition, caretPosition + searchText.length(), Collections.singletonList(AppConfig.FOUND_TEXT_STYLE));

                    tabsSearchTextMap.put(selectedTab, searchedTextData);
                } else {
                    fileContentArea.showCaretProperty();
                    DialogWindows.showInformationAlert(bundle.getString("alert.noMatches"));
                    searchedTextData.setPosition(0);
                    tabsSearchTextMap.put(selectedTab, searchedTextData);
                    fileContentArea.moveTo(0);
                    fileContentArea.requestFollowCaret();
                }
            }

        });


        exitPageReadingModeButton.setOnAction(event -> {
            oneTabModeCheckBox.setDisable(false);
            largeFileModeCheckBox.setDisable(false);
            readForwardButton.setDisable(true);
            readBackButton.setDisable(true);
            exitPageReadingModeButton.setDisable(true);
            searchButton.setDisable(false);
            searchPathTextField.setDisable(false);
            enableFileMaskRadioButton.setDisable(false);
        });


        largeFileModeCheckBox.setOnAction(event -> {
            final CheckBox largeFile = (CheckBox) event.getSource();
            if (largeFile.isSelected()) {
                DialogWindows.showInformationAlert(bundle.getString("alert.largeFileModeOn"));
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

        readForwardButton.setOnAction(event -> {
            final Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
            tabsSearchTextMap.get(selectedTab).setPosition(0);
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

        readBackButton.setOnAction(event -> {
            final Tab selectedTab = resultsTabPane.getSelectionModel().getSelectedItem();
            tabsSearchTextMap.get(selectedTab).setPosition(0);
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
        searchButton.setOnAction(event -> startSearchTask());

        showAllTextOccurrencesButton.setOnAction(event -> {
            if (currentFilePath != null) {
                try {
                    Stage stage = new Stage();
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(AppConfig.ALL_OCCURENCES_WINDOW_FXML_PATH));
                    Parent root = fxmlLoader.load();
                    stage.setTitle(bundle.getString("title.foundOccurrences"));
                    stage.setMinHeight(600);
                    stage.setMinWidth(800);
                    stage.setResizable(false);
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.NONE);
                    stage.show();

                    final TextOccurrencesMenuController controller = fxmlLoader.getController();
                    CodeArea foundTextArea = controller.getTextOccurrencesArea();
                    foundTextArea.appendText(bundle.getString("tab.Search"));
                    foundTextArea.setDisable(true);

                    showAllTextOccurrencesTask(foundTextArea, stage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void showAllTextOccurrencesTask(CodeArea foundTextArea, Stage stage) {
        final String searchedText = fileContentSearchTextField.getText();
        if (searchedText.trim().isEmpty()) {
            Platform.runLater(stage::close);
            DialogWindows.showInformationAlert(bundle.getString("alert.noSearchedText"));
            return;
        }
        Runnable findTextOccurrencesTask = () -> {
            File file = new File(currentFilePath);
            String allFoundOccurrences = FileUtils.getAllTextOccurrences(file, searchedText);
            if (allFoundOccurrences.isEmpty()) {
                Platform.runLater(() -> {
                    foundTextArea.clear();
                    stage.close();
                });
                DialogWindows.showInformationAlert(bundle.getString("alert.noTextFound"));
                return;
            }

            Platform.runLater(() -> {
                foundTextArea.replaceText(allFoundOccurrences);
                foundTextArea.setDisable(false);
            });
        };

        executorService.execute(findTextOccurrencesTask);
    }

    private void setupControlsTips() {
        readBackButton.setTooltip(new Tooltip(bundle.getString("tip.readBack")));
        readForwardButton.setTooltip(new Tooltip(bundle.getString("tip.readMore")));
        exitPageReadingModeButton.setTooltip(new Tooltip(bundle.getString("tip.exitReadingMode")));
        searchTextButton.setTooltip(new Tooltip(bundle.getString("tip.findText")));
        showAllTextOccurrencesButton.setTooltip(new Tooltip(bundle.getString("tip.showAllOccurrences")));
    }

}

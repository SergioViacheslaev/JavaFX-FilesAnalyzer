package org.home.filesanalyzer.controllers;

/**
 * @author Sergei Viacheslaev
 */

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.Setter;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.home.filesanalyzer.config.AppConfig;

import java.util.ResourceBundle;

@Getter
public class TextOccurrencesMenuController {
    private final CodeArea textOccurrencesArea = new CodeArea();

    @FXML
    @Setter
    private ResourceBundle bundle;

    @FXML
    private AnchorPane pane;

    @FXML
    void initialize() {
        bundle = AppConfig.getBundle();
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(textOccurrencesArea);
        textOccurrencesArea.setParagraphGraphicFactory(LineNumberFactory.get(textOccurrencesArea));

        textOccurrencesArea.setContextMenu(setupContextMenu());

        AnchorPane.setBottomAnchor(scrollPane, 0d);
        AnchorPane.setTopAnchor(scrollPane, 0d);
        AnchorPane.setLeftAnchor(scrollPane, 0d);
        AnchorPane.setRightAnchor(scrollPane, 0d);
        pane.getChildren().add(scrollPane);

    }


    private ContextMenu setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuSelectAll = new MenuItem(bundle.getString("menu.selectAll"));
        MenuItem menuCopy = new MenuItem(bundle.getString("menu.copy"));
        MenuItem menuPaste = new MenuItem(bundle.getString("menu.paste"));

        menuSelectAll.setOnAction(event -> textOccurrencesArea.selectAll());

        menuCopy.setOnAction(event -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(textOccurrencesArea.getSelectedText());
            clipboard.setContent(content);
        });

        menuPaste.setOnAction(event -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            textOccurrencesArea.insertText(textOccurrencesArea.getCaretPosition(), clipboard.getString());
        });


        contextMenu.getItems().add(menuSelectAll);
        contextMenu.getItems().add(menuCopy);
        contextMenu.getItems().add(menuPaste);

        return contextMenu;
    }


}

package org.home.textfinder.controllers;

/**
 * @author Sergei Viacheslaev
 */

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.net.URL;
import java.util.ResourceBundle;

@Getter
public class ShowAllOccurrencesController {
    private final CodeArea textOccurrencesArea = new CodeArea();

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane pane;

    @FXML
    void initialize() {
        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(textOccurrencesArea);
        textOccurrencesArea.setParagraphGraphicFactory(LineNumberFactory.get(textOccurrencesArea));
        AnchorPane.setBottomAnchor(scrollPane, 0d);
        AnchorPane.setTopAnchor(scrollPane, 0d);
        AnchorPane.setLeftAnchor(scrollPane, 0d);
        AnchorPane.setRightAnchor(scrollPane, 0d);

        pane.getChildren().add(scrollPane);

    }


}

package org.home.textfinder.utils;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;


public class TabPaneUtils {
    public static Tab addTab(TabPane tabPane) {
        Tab newTab;
        if (tabPane.getTabs().size() == 0) {
            newTab = new Tab(String.format(StatusMessages.getTabName(), tabPane.getTabs().size() + 1));
            newTab.setClosable(false);
        } else if (tabPane.getTabs().size() == 4) {
            Platform.runLater(() -> tabPane.getTabs().remove(3));
            newTab = new Tab(String.format(StatusMessages.getTabName(),4));
            newTab.setClosable(true);
        } else {
            newTab = new Tab(String.format(StatusMessages.getTabName(), tabPane.getTabs().size() + 1));
            newTab.setClosable(true);
        }

        Platform.runLater(() -> tabPane.getTabs().add(newTab));
        return newTab;
    }
}

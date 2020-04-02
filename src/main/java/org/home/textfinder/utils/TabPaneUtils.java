package org.home.textfinder.utils;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author Sergei Viacheslaev
 */
public class TabPaneUtils {
    public static Tab addTab(TabPane tabPane, String tabName) {
        Tab newTab = new Tab(tabName);
        Platform.runLater(() -> tabPane.getTabs().add(newTab));
        return newTab;
    }
}

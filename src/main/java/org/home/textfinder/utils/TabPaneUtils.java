package org.home.textfinder.utils;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author Sergei Viacheslaev
 */
public class TabPaneUtils {
    public static void addTab(TabPane tabPane, String tabName) {
        int numTabs = tabPane.getTabs().size();
        Tab tab = new Tab(tabName);
        tabPane.getTabs().add(tab);
    }
}

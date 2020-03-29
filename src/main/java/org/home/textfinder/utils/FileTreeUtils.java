package org.home.textfinder.utils;

import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Sergei Viacheslaev
 */
public class FileTreeUtils {


    @SneakyThrows
    public static void buildFilesWithExtensionsTree(TreeItem<String> rootItem, String fileExtension) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(rootItem.getValue()))) {
            for (Path path : directoryStream) {


                if (Files.isRegularFile(path) && (path.toString().endsWith(fileExtension))) {
                    rootItem.getChildren().add(new TreeItem<>(path.toString()));
                }


                if (Files.isDirectory(path)) {
                    TreeItem<String> directoryItem = new TreeItem<>(path.toString(), new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
                    directoryItem.setExpanded(true);
                    rootItem.getChildren().add(directoryItem);
                    buildFilesWithExtensionsTree(directoryItem, fileExtension);
                    removeEmptyTreeItem(rootItem);

                }
            }
        }
    }


    @SneakyThrows
    public static void buildFilesMaskedTree(TreeItem<String> rootItem, String fileMask) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(rootItem.getValue()))) {
            for (Path path : directoryStream) {
                TreeItem<String> treeItem = new TreeItem<>(path.toString());
                treeItem.setGraphic(new ImageView());
                treeItem.setExpanded(true);

                if (Files.isDirectory(path)) {
                    rootItem.getChildren().add(treeItem);
                    buildFilesWithExtensionsTree(treeItem, fileMask);
                    removeEmptyTreeItem(rootItem);

                } else if (StringUtils.containsIgnoreCase(path.toString(), fileMask)) {
                    rootItem.getChildren().add(treeItem);
                }
            }
        }
    }


    private static void removeEmptyTreeItem(TreeItem<String> rootItem) {
        int lastTreeItemIndex = rootItem.getChildren().size() - 1;
        if (rootItem.getChildren().get(lastTreeItemIndex).getChildren().size() == 0) {
            rootItem.getChildren().remove(lastTreeItemIndex);
        }
    }
}

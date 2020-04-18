package org.home.textfinder.utils;

import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Util class for building search results in the TreeView.
 *
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
                    TreeItem<String> directoryItem = addTreeItem(rootItem, path.toString(), new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
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

                if (Files.isRegularFile(path) && StringUtils.containsIgnoreCase(path.getFileName().toString(), fileMask)) {
                    rootItem.getChildren().add(new TreeItem<>(path.toString()));
                }

                if (Files.isDirectory(path)) {
                    TreeItem<String> directoryItem = addTreeItem(rootItem, path.toString(), new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
                    buildFilesMaskedTree(directoryItem, fileMask);
                    removeEmptyTreeItem(rootItem);
                }
            }
        }
    }


    @SneakyThrows
    public static void buildFilesMaskedContentTree(TreeItem<String> rootItem, String fileMask, String text) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(rootItem.getValue()))) {
            for (Path path : directoryStream) {

                if (Files.isRegularFile(path) && StringUtils.containsIgnoreCase(path.getFileName().toString(), fileMask)
                        && FileUtils.checkFileContainsText(new File(path.toString()), text)) {
                    rootItem.getChildren().add(new TreeItem<>(path.toString()));
                }

                if (Files.isDirectory(path)) {
                    TreeItem<String> directoryItem = addTreeItem(rootItem, path.toString(), new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
                    buildFilesMaskedContentTree(directoryItem, fileMask, text);
                    removeEmptyTreeItem(rootItem);
                }
            }
        }
    }

    @SneakyThrows
    public static void buildFilesWithContentTree(TreeItem<String> rootItem, String fileExtension, String text) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(rootItem.getValue()))) {
            for (Path path : directoryStream) {

                if (Files.isRegularFile(path)
                        && (path.toString().endsWith(fileExtension))
                        && FileUtils.checkFileContainsText(new File(path.toString()), text)) {
                    rootItem.getChildren().add(new TreeItem<>(path.toString()));
                }


                if (Files.isDirectory(path)) {
                    TreeItem<String> directoryItem = addTreeItem(rootItem, path.toString(), new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
                    buildFilesWithContentTree(directoryItem, fileExtension, text);
                    removeEmptyTreeItem(rootItem);
                }
            }
        }
    }


    private static TreeItem<String> addTreeItem(TreeItem<String> rootItem, String filePath, ImageView image) {
        TreeItem<String> addedItem = new TreeItem<>(filePath, image);
        addedItem.setExpanded(true);
        rootItem.getChildren().add(addedItem);
        return addedItem;
    }


    private static void removeEmptyTreeItem(TreeItem<String> rootItem) {
        int lastTreeItemIndex = rootItem.getChildren().size() - 1;
        if (rootItem.getChildren().get(lastTreeItemIndex).getChildren().size() == 0) {
            rootItem.getChildren().remove(lastTreeItemIndex);
        }
    }


}

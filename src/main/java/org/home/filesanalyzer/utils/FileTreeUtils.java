package org.home.filesanalyzer.utils;

import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

/**
 * Util class for building search results in the TreeView.
 *
 * @author Sergei Viacheslaev
 */
public class FileTreeUtils {

    public static void buildFilesWithExtensionsTree(TreeItem<String> rootItem, String fileExtension, List<String> accessDeniedFiles) {

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(rootItem.getValue()))) {
            for (Path path : directoryStream) {

                if (Files.isRegularFile(path) && (path.toString().endsWith(fileExtension))) {
                    rootItem.getChildren().add(new TreeItem<>(path.toString()));
                }


                if (Files.isDirectory(path)) {
                    TreeItem<String> directoryItem = addTreeItem(rootItem, path.toString(), new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
                    buildFilesWithExtensionsTree(directoryItem, fileExtension, accessDeniedFiles);
                    removeEmptyTreeItem(rootItem);
                }
            }
        } catch (FileNotFoundException | AccessDeniedException e) {
            accessDeniedFiles.add(rootItem.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void buildFilesMaskedTree(TreeItem<String> rootItem, String fileMask, List<String> accessDeniedFiles) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(rootItem.getValue()))) {
            for (Path path : directoryStream) {

                if (Files.isRegularFile(path) && StringUtils.containsIgnoreCase(path.getFileName().toString(), fileMask)) {
                    rootItem.getChildren().add(new TreeItem<>(path.toString()));
                }

                if (Files.isDirectory(path)) {
                    TreeItem<String> directoryItem = addTreeItem(rootItem, path.toString(), new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
                    buildFilesMaskedTree(directoryItem, fileMask, accessDeniedFiles);
                    removeEmptyTreeItem(rootItem);
                }
            }
        } catch (FileNotFoundException | AccessDeniedException e) {
            accessDeniedFiles.add(rootItem.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void buildFilesMaskedContentTree(TreeItem<String> rootItem, String fileMask, String text, List<String> accessDeniedFiles) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(rootItem.getValue()))) {
            for (Path path : directoryStream) {

                if (Files.isRegularFile(path) && StringUtils.containsIgnoreCase(path.getFileName().toString(), fileMask)
                        && FileUtils.checkFileContainsText(new File(path.toString()), text)) {
                    rootItem.getChildren().add(new TreeItem<>(path.toString()));
                }

                if (Files.isDirectory(path)) {
                    TreeItem<String> directoryItem = addTreeItem(rootItem, path.toString(), new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
                    buildFilesMaskedContentTree(directoryItem, fileMask, text, accessDeniedFiles);
                    removeEmptyTreeItem(rootItem);
                }
            }
        } catch (FileNotFoundException | AccessDeniedException e) {
            accessDeniedFiles.add(rootItem.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buildFilesWithContentTree(TreeItem<String> rootItem, String fileExtension, String text, List<String> accessDeniedFiles) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(rootItem.getValue()))) {
            for (Path path : directoryStream) {

                if (Files.isRegularFile(path)
                        && (path.toString().endsWith(fileExtension))
                        && FileUtils.checkFileContainsText(new File(path.toString()), text)) {
                    rootItem.getChildren().add(new TreeItem<>(path.toString()));
                }


                if (Files.isDirectory(path)) {
                    TreeItem<String> directoryItem = addTreeItem(rootItem, path.toString(), new ImageView(Icons.DIRECTORY_EXPANDED.getImage()));
                    buildFilesWithContentTree(directoryItem, fileExtension, text, accessDeniedFiles);
                    removeEmptyTreeItem(rootItem);
                }
            }
        } catch (FileNotFoundException | AccessDeniedException e) {
            accessDeniedFiles.add(rootItem.getValue());
        } catch (IOException e) {
            e.printStackTrace();
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

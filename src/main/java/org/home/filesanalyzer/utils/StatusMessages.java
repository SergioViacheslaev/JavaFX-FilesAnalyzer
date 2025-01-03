package org.home.filesanalyzer.utils;


import java.util.ResourceBundle;

public class StatusMessages {
    private static ResourceBundle bundle;

    public static void setBundle(ResourceBundle bundle) {
        StatusMessages.bundle = bundle;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static String getFileSizeStatus(long fileSize) {
        if (fileSize < FileUtils.ONE_KB) {
            return String.format("%s %d B", bundle.getString("fileSize"), fileSize);
        }

        if (fileSize < FileUtils.ONE_MB) {
            return String.format("%s %d KB", bundle.getString("fileSize"), fileSize / FileUtils.ONE_KB);
        } else {
            return String.format("%s %d MB", bundle.getString("fileSize"), fileSize / FileUtils.ONE_MB);
        }
    }

    public static String getFilePagesStatus() {
        return String.format(bundle.getString("filePage"), FileUtils.currentPageNumber, FileUtils.totalPagesCount);
    }

    public static String getTabName() {
        return bundle.getString("tabName");
    }
}

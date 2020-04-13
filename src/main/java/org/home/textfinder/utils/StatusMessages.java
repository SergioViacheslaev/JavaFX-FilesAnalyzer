package org.home.textfinder.utils;

/**
 * @author Sergei Viacheslaev
 */
public class StatusMessages {
    public static String getFileSizeStatus(long fileSize) {
        if (fileSize < FileUtils.ONE_KB) {
            return String.format("Размер файла: %d B", fileSize);
        }

        if (fileSize < FileUtils.ONE_MB) {
            return String.format("Размер файла: %d KB", fileSize / FileUtils.ONE_KB);
        } else {
            return String.format("Размер файла: %d MB", fileSize / FileUtils.ONE_MB);
        }
    }

    public static String getFilePagesStatus() {
         return String.format("Страница %d из %d", FileUtils.currentPageNumber, FileUtils.totalPagesCount);
    }
}

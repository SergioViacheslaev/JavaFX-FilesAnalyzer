package org.home.textfinder.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**Gets file content, pages, check files has searched text.
 *
 * @author Sergei Viacheslaev
 */
public class FileUtils {
    public static final long ONE_MB = 1048576;
    public static final long ONE_KB = 1024;
    public static final long FIZE_SIZE_LIMIT = 100 * ONE_MB;
    public static final int FILE_PAGE_LIMIT = 50 * 1024 * 1024;
    public static int lastPageSize = 0;
    public static long previousPagePointer = 0;
    public static long nextPagePointer = 0;
    public static long totalPagesCount = 0;
    public static int currentPageNumber = 1;

    public static String getFileContent(String filePath) throws IOException {
        final List<String> contentStrings = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        if (!contentStrings.isEmpty()) {
            return String.join("\n", contentStrings);
        } else {
            return "";
        }
    }


    @SneakyThrows
    public static String getLargeFileContent(String filepath) {
        byte[] buffer = Files.readAllBytes(Paths.get(filepath));
        if (buffer.length > 0) {
            return new String(buffer, StandardCharsets.UTF_8);
        }
        return "";
    }


    @SneakyThrows
    public static Map<Boolean, String> getPreviousPageContent(String filePath) {
        boolean hasPreviousPage = true;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            char[] buffer = new char[FILE_PAGE_LIMIT];

            if (br.skip(previousPagePointer) == 0) {
                hasPreviousPage = false;
            }

            int readBytes = br.read(buffer);
            nextPagePointer = previousPagePointer + readBytes;
            previousPagePointer = previousPagePointer - (lastPageSize + readBytes);
            if (previousPagePointer < 0) {
                previousPagePointer = 0;
            }
            lastPageSize = readBytes;

            if (readBytes > 0) {
                currentPageNumber--;
                return Collections.singletonMap(hasPreviousPage, new String(buffer));
            }
        }

        return Collections.emptyMap();
    }


    @SneakyThrows
    public static Map<Boolean, String> getNextPageContent(String filePath) {
        boolean hasNextPage = true;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            char[] buffer = new char[FILE_PAGE_LIMIT];

            previousPagePointer = nextPagePointer - lastPageSize;

            br.skip(nextPagePointer);

            int bytesRead = br.read(buffer);
            nextPagePointer += bytesRead;
            lastPageSize = bytesRead;

            if (!br.ready()) {
                hasNextPage = false;
            }
            if (bytesRead > 0) {
                currentPageNumber++;
                return Collections.singletonMap(hasNextPage, new String(buffer));
            }
        }

        return Collections.emptyMap();

    }

    @SneakyThrows
    public static String getFirstPageContent(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            char[] buffer = new char[FILE_PAGE_LIMIT];

            int bytesRead = br.read(buffer);
            nextPagePointer = bytesRead;
            lastPageSize = bytesRead;
            previousPagePointer = 0;
            currentPageNumber = 1;
            totalPagesCount = countTotalFilePages(filePath);

            if (bytesRead > 0) {
                return new String(buffer);
            }
        }

        return "";

    }


    @SneakyThrows
    public static boolean checkFileContainsText(File file, String searchedText) {
        boolean isContentFound = false;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine() && !isContentFound) {
                String contentLine = scanner.nextLine();
                if (StringUtils.containsIgnoreCase(contentLine,searchedText)) {
                    isContentFound = true;
                }
            }
        }
        return isContentFound;
    }

    @SneakyThrows
    private static long countTotalFilePages(String filePath) {
        long fileSize = Files.size(Paths.get(filePath));
        long pagesCount;

        if (fileSize % FileUtils.FILE_PAGE_LIMIT == 0) {
            pagesCount = fileSize / FileUtils.FILE_PAGE_LIMIT;
        } else {
            pagesCount = (fileSize / FileUtils.FILE_PAGE_LIMIT) + 1;
        }

        return pagesCount;
    }

}


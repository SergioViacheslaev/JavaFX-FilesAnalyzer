package org.home.textfinder.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * @author Sergei Viacheslaev
 */
public class FileUtils {

    public static String getFileContent(String filePath) throws IOException {
        final List<String> contentStrings = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        if (!contentStrings.isEmpty()) {
            return String.join("\n", contentStrings);
        } else {
            return "";
        }
    }


    public static boolean checkFileContainsText(File file, String searchedText) {
        boolean isContentFound = false;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String contentLine = scanner.nextLine();

                if (contentLine.contains(searchedText)) {
                    isContentFound = true;
                }
            }
        } finally {
            return isContentFound;
        }
    }

}


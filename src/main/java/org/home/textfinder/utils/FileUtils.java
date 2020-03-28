package org.home.textfinder.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

}


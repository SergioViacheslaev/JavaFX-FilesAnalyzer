package org.home.textfinder.utils;

import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Sergei Viacheslaev
 */
public class FileUtils {
    private static final long fileSizeLimit = 104857600L; //100 MB
    //    private static final long fileSizeLimit = 52_428_800L; //100 MB
    public static long skipBytes = 0L;

    public static String getFileContent(String filePath) throws IOException {

        if (Files.size(Paths.get(filePath)) > fileSizeLimit) {
            DialogWindows.showInformationAlert("Файл больше 100 МБ !");
            return "";
        } else {
//            File file = new File(filePath);
//            return org.apache.commons.io.FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            final List<String> contentStrings = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
            if (!contentStrings.isEmpty()) {
                return String.join("\n", contentStrings);
            } else {
                return "";
            }
        }

    }

 /*   public static String getFileContent(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder((int) Files.size(Paths.get(filePath)));
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath,StandardCharsets.UTF_8))) {
            char[] buffer = new char[1024 * 1024];
            bufferedReader.skip()


            int bytesRead;
            while ((bytesRead = bufferedReader.read(buffer)) != -1)
            {
                    sb.append(buffer);
            }

        }

        return sb.toString();

    }*/


    public static Map<String, Long> getPreviousPageContent(String filePath, long previousPageOffset) throws IOException {
        FileTreeUtils.previousPageOffset = previousPageOffset - (8 * 1024 * 1024);
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {

            raf.seek(previousPageOffset);

            byte[] buffer = new byte[8 * 1024 * 1024];
            raf.read(buffer);

            FileTreeUtils.nextPageOffset = raf.getFilePointer();
            return Collections.singletonMap(new String(buffer, StandardCharsets.UTF_8), raf.getFilePointer());
        }

    }
/*
    public static Map<String, Long> getNextPageContent(String filePath, long nextPageOffset, StyleClassedTextArea textArea) throws IOException {

        FileTreeUtils.previousPageOffset = nextPageOffset - (2097152);


        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        ) {
            StringBuilder sb = new StringBuilder(2097152);

            raf.seek(nextPageOffset);

            while (sb.length() <= 2097152) {
                sb.append(String.format("%s%n", raf.readLine()));
            }

            textArea.replaceText(sb.toString());

            FileTreeUtils.nextPageOffset = raf.getFilePointer();


            return Collections.emptyMap();
//            return Collections.singletonMap(new String(buffer, StandardCharsets.UTF_8), raf.getFilePointer());
        }


    }*/


    public static String getNextPageContent(String filePath) throws IOException {
//        StringBuilder sb = new StringBuilder((int) Files.size(Paths.get(filePath)));


        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            char[] buffer = new char[8 * 1024 * 1024];
            br.skip(skipBytes);

            int bytesRead;
            bytesRead = br.read(buffer);


            if (bytesRead > 0) {
                skipBytes += bytesRead;
                return new String(buffer);
            }


        }

        return "";

    }




  /*  public static String getFileContent(String filePath) throws IOException {
        try (RandomAccessFile reader = new RandomAccessFile(filePath, "r");
            FileChannel channel = reader.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());

            channel.read(buffer);
            String fileContent = new String(buffer.array(), StandardCharsets.UTF_8);

            buffer.clear();

            return fileContent;
        }
    }*/

    @SneakyThrows
    public static boolean checkFileContainsText(File file, String searchedText) {
        boolean isContentFound = false;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine() && !isContentFound) {
                String contentLine = scanner.nextLine();

                if (contentLine.contains(searchedText)) {
                    isContentFound = true;
                }
            }
        }

        return isContentFound;
    }

}


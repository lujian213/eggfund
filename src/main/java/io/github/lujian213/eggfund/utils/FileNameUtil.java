package io.github.lujian213.eggfund.utils;

public class FileNameUtil {

    private FileNameUtil() {
    }

    public static boolean isValidFileName(String fileName) {
        return fileName.matches("^[a-zA-Z0-9._-]+$");
    }

    public static String makeValidFileName(String input) {
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
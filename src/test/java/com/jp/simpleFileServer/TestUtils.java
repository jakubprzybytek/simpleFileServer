package com.jp.simpleFileServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for testing.
 */
public class TestUtils {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

    public static File FIRST_FILE = new File("./src/test/resources/first_test_file.txt");

    public static File SECOND_FILE = new File("./src/test/resources/second_test_file.txt");

    public static String randomFileName() {
        return String.format("testFile-%d.txt", System.currentTimeMillis());
    }

    public static byte[] readBytes(File file) {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getNowString() {
        return sdf.format(new Date());
    }
}

package com.adioss.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static void removeOnShutdown(Path tempDirectory) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            deleteDirectory(tempDirectory.toFile());
        }));
    }
}

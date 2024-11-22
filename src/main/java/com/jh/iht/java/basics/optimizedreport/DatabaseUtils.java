package com.jh.iht.java.basics.optimizedreport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DatabaseUtils {

    // This function backs up the current browser history to a new file
    public static void createBackupFromLatestHistory(String dbPath) {
        try {
            File originalHistory = new File(dbPath);
            if (originalHistory.exists()) {
                Path backupPath = Paths.get(dbPath + "Backup");
                Files.copy(originalHistory.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Backup created: " + backupPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

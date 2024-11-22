package com.jh.iht.java.basics.comments;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * This utility class contains methods for interacting with browser history databases.
 * Specifically, it provides functionality to create backups of browser history files.
 */
public class DatabaseUtils {

    /**
     * Creates a backup of the current browser history file by copying it to a new location with a "Backup" suffix.
     * If the original file exists, the backup is created; otherwise, no action is taken.
     * 
     * @param dbPath The path to the browser history database file.
     */
    public static void createBackupFromLatestHistory(String dbPath) {
        try {
            // Create a File object from the provided database path
            File originalHistory = new File(dbPath);
            
            // Check if the original history file exists
            if (originalHistory.exists()) {
                // Create the backup path by appending "Backup" to the original file name
                Path backupPath = Paths.get(dbPath + "Backup");
                
                // Copy the original file to the backup path, replacing any existing backup file
                Files.copy(originalHistory.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Print a confirmation message after successful backup creation
                System.out.println("Backup created: " + backupPath);
            }
        } catch (IOException e) {
            // Print any IO exceptions that occur during the backup process
            e.printStackTrace();
        }
    }
}

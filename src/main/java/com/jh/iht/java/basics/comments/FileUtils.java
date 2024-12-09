package com.jh.iht.java.basics.comments;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * This utility class provides methods for file operations such as ensuring a directory exists
 * and writing a JSON report to a file.
 */
public class FileUtils {

    /**
     * Ensures that the specified directory exists. If the directory does not exist,
     * it attempts to create it. If the directory creation is successful, a confirmation
     * message is printed.
     *
     * @param path The path to the directory to check or create.
     */
    public static void ensureDirectoryExists(String path) {
        // Create a File object for the given directory path
        File directory = new File(path);
        
        // Check if the directory does not exist and attempt to create it
        if (!directory.exists() && directory.mkdirs()) {
            // If directory creation is successful, print the directory path
            System.out.println("Directory created: " + directory.getPath());
        }
    }

    /**
     * Writes a JSON report to a file at the specified path.
     * The JSON object is pretty-printed with an indentation of 4 spaces.
     *
     * @param filePath  The path where the JSON file will be saved.
     * @param jsonReport The JSON object to write to the file.
     */
    public static void writeJsonToFile(String filePath, JSONObject jsonReport) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the pretty-printed JSON content to the file
            writer.write(jsonReport.toString(4));
        } catch (IOException e) {
            // Print any exceptions that occur during the file writing process
            e.printStackTrace();
        }
    }
    public static void writeCsvToFile(String filePath, List<String[]> data) {
        System.out.println("Writing data into CSV file...");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String[] row : data) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

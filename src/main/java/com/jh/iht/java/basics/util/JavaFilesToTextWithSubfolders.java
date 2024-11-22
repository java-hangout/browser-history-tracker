package com.jh.iht.java.basics.util;

import java.io.*;

public class JavaFilesToTextWithSubfolders {

    public static void main(String[] args) {
        // Folder containing .java files (can include subfolders)
        String folderPath = "D:\\workspace\\HistoryTracker\\src\\main\\java\\com\\jh\\iht\\java\\basics\\comments";  // Change this to the folder path containing .java files
        
        // Output file where the content of all .java files will be written
        String outputFile = "D:\\workspace\\HistoryTracker\\src\\main\\resources\\output\\BrowserVisitedSitesTimeTracker_comments.txt";  // Change this to your desired output file
        
        try {
            writeJavaFilesFromSubfoldersToSingleFile(folderPath, outputFile);
            System.out.println("Java files have been successfully written to " + outputFile);
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    public static void writeJavaFilesFromSubfoldersToSingleFile(String folderPath, String outputFile) throws IOException {
        // Create a BufferedWriter to write to the output file
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        // Start recursion to process files in the directory and subdirectories
        processDirectory(new File(folderPath), writer);

        // Close the BufferedWriter to finalize writing to the output file
        writer.close();
    }

    // Recursively processes all .java files in the directory and subdirectories
    private static void processDirectory(File folder, BufferedWriter writer) throws IOException {
        // List all files and subdirectories in the current folder
        File[] files = folder.listFiles();
        
        if (files == null) {
            System.out.println("Error accessing the folder: " + folder.getPath());
            return;
        }

        // Process each file/subdirectory
        for (File file : files) {
            if (file.isDirectory()) {
                // If it's a directory, recurse into it
                processDirectory(file, writer);
            } else if (file.isFile() && file.getName().endsWith(".java")) {
                // If it's a .java file, process it
                processJavaFile(file, writer);
            }
        }
    }

    // Process a single .java file
    private static void processJavaFile(File javaFile, BufferedWriter writer) throws IOException {
        BufferedReader reader = null;
        try {
            // Create a BufferedReader to read the content of the .java file
            reader = new BufferedReader(new FileReader(javaFile));
            String line;

            // Write the file name as a header (optional)
            writer.write("### Contents of " + javaFile.getPath() + " ###");
            writer.newLine();
            
            // Write the content of the .java file into the output file
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            writer.newLine(); // Adds an extra newline between files
        } catch (IOException e) {
            System.out.println("Error reading file: " + javaFile.getPath());
            throw e; // Rethrow the exception to stop further execution
        } finally {
            // Close the BufferedReader after processing the current file
            if (reader != null) {
                reader.close();
            }
        }
    }
}

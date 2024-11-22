package com.jh.iht.java.basics.optimizedreport;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {

    public static void ensureDirectoryExists(String path) {
        File directory = new File(path);
        if (!directory.exists() && directory.mkdirs()) {
            System.out.println("Directory created: " + directory.getPath());
        }
    }

    public static void writeJsonToFile(String filePath, JSONObject jsonReport) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(jsonReport.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

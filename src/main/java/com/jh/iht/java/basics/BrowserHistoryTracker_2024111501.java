package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.io.*;

public class BrowserHistoryTracker_2024111501 {

    public static void main(String[] args) {
        // Path to Chrome's History SQLite database file
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";
        String csvFilePath = "D:\\workspace\\HistoryTracker\\src\\main\\resources\\template\\browser_history02.csv";  // Output CSV file path

        // Setup CSV Writer
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            // Write the header to the CSV file
            writer.write("Title,URL,Visit Time,Start Time");
            writer.newLine();

            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {

                // SQL query to fetch actual URL instead of ID
                String sql = "SELECT v.id, u.url, u.title, v.visit_time " +
                        "FROM visits v " +
                        "JOIN urls u ON v.url = u.id " +
                        "ORDER BY v.visit_time";

                try (PreparedStatement pstmt = conn.prepareStatement(sql);
                     ResultSet rs = pstmt.executeQuery()) {

                    // Date format for displaying the visit time
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                    boolean dataFound = false;

                    while (rs.next()) {
                        dataFound = true;
                        long visitTime = rs.getLong("visit_time");
                        String url = rs.getString("url");  // Get the actual URL here
                        String title = rs.getString("title");

                        // Convert Chrome's timestamp (microseconds since 1601) to Unix timestamp (milliseconds since 1970)
                        long timestampMillis = (visitTime - 11644473600000000L) / 1000; // Convert to milliseconds

                        // Convert Unix timestamps to human-readable dates
                        java.util.Date visitDate = new java.util.Date(timestampMillis);
                        String visitDateFormatted = dateFormatter.format(visitDate);
                        String visitTimeFormatted = timeFormatter.format(visitDate);

                        // Write the visit data to the CSV file
                        writer.write(String.format("\"%s\",\"%s\",\"%s %s\",\"%s\"\n",
                                title, url, visitDateFormatted, visitTimeFormatted, visitTimeFormatted));
                    }

                    // If no data was found, print a message
                    if (!dataFound) {
                        System.out.println("No visits found in the database.");
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

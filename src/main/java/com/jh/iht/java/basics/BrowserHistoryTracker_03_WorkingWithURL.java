package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;
import java.util.*;

public class BrowserHistoryTracker_03_WorkingWithURL {

    public static void main(String[] args) {
        // Path to Chrome's History SQLite database file
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";

        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {

                // SQL query to fetch actual URL instead of ID
                String sql = "SELECT v.id, u.url, u.title, v.visit_time " +
                        "FROM visits v " +
                        "JOIN urls u ON v.url = u.id " +
                        "ORDER BY v.visit_time " +
                        "LIMIT 10";  // Retrieve the first 10 records for testing

                try (PreparedStatement pstmt = conn.prepareStatement(sql);
                     ResultSet rs = pstmt.executeQuery()) {

                    // Date format for displaying the visit time
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                    // Variables to track session data
                    boolean dataFound = false;

                    while (rs.next()) {
                        dataFound = true;
                        long visitTime = rs.getLong("visit_time");
                        String url = rs.getString("url");  // Get the actual URL here
                        String title = rs.getString("title");

                        // Print raw visit_time for debugging
                        System.out.println("Raw visit_time (microseconds since 1601): " + visitTime);

                        // Convert Chrome's timestamp (microseconds since 1601) to Unix timestamp (milliseconds since 1970)
                        long timestampMillis = (visitTime - 11644473600000000L) / 1000; // Convert to milliseconds

                        // Convert Unix timestamps to human-readable dates
                        java.util.Date visitDate = new java.util.Date(timestampMillis);
                        String visitDateFormatted = dateFormatter.format(visitDate);
                        String visitTimeFormatted = timeFormatter.format(visitDate);

                        // Print the visit details with the actual URL
                        System.out.println("Visited: " + title + " (" + url + ")");
                        System.out.println("  Visit Time: " + visitDateFormatted + " " + visitTimeFormatted);
                        System.out.println("---------------------------------------------------");
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

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

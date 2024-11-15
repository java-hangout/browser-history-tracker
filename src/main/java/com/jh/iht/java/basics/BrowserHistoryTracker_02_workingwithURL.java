package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

public class BrowserHistoryTracker_02_workingwithURL {

    public static void main(String[] args) {

        // Path to Chrome's History SQLite database file
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {

                // SQL query to get all visits (no date range restriction)
                String sql = "SELECT v.id, v.url, u.title, u.url AS full_url, v.visit_time FROM visits v " +
                        "JOIN urls u ON v.url = u.id ORDER BY v.visit_time";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    try (ResultSet rs = pstmt.executeQuery()) {

                        // Date format to display visit times
                        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                        // Variables for session tracking
                        long previousVisitTime = -1;
                        String previousUrl = "";
                        long sessionStartTime = -1;
                        long sessionEndTime = -1;
                        long sessionDuration = 0;

                        // Check if the query returns any results
                        boolean dataFound = false;

                        while (rs.next()) {
                            dataFound = true;
                            String url = rs.getString("full_url");  // Fetch the actual URL
                            String title = rs.getString("title");
                            long visitTime = rs.getLong("visit_time");

                            // Print raw visit_time for debugging
                            System.out.println("Raw visit_time (microseconds since 1601): " + visitTime);

                            // Convert Chrome's timestamp (microseconds since 1601) to Unix timestamp (milliseconds since 1970)
                            long timestampMillis = (visitTime - 11644473600000000L) / 1000;  // Convert to milliseconds

                            // Convert Unix timestamp to human-readable date
                            Date visitDate = new Date(timestampMillis);
                            String visitDateFormatted = dateFormatter.format(visitDate);
                            String visitTimeFormatted = timeFormatter.format(visitDate);

                            // Print the current visit info
                            System.out.println("Visited: " + title + " (" + url + ") at " + visitDateFormatted + " " + visitTimeFormatted);

                            // Save the current visit as the previous visit
                            previousVisitTime = timestampMillis;
                            previousUrl = url;
                        }

                        // If no data was found, print a message
                        if (!dataFound) {
                            System.out.println("No visits found.");
                        }

                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

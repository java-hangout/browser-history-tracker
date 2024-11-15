package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.Date;

public class BrowserHistoryTrackerwithNumberOfDays {

    public static void main(String[] args) {

        // Get the current date and calculate the time range (e.g., Last 2 days for debugging)
        LocalDate currentDate = LocalDate.now();
        System.out.println("currentDate: " + currentDate);

        // Narrow the date range for debugging purposes
        LocalDate startDate = currentDate.minusDays(2); // Last 2 days, adjust as needed
        System.out.println("startDate: " + startDate);

        ZonedDateTime startOfRange = startDate.atStartOfDay(ZoneId.of("UTC"));
        ZonedDateTime endOfRange = currentDate.atStartOfDay(ZoneId.of("UTC")).plusDays(1).minusNanos(1); // End of today
        System.out.println("startOfRange: " + startOfRange);
        System.out.println("endOfRange: " + endOfRange);

        // Convert to Chrome's timestamp format (microseconds since Jan 1, 1601)
        long startTimestamp = startOfRange.toInstant().toEpochMilli() * 10000 + 11644473600000000L;
        long endTimestamp = endOfRange.toInstant().toEpochMilli() * 10000 + 11644473600000000L;

        // Print the timestamp values for debugging
        System.out.println("Start Timestamp (microseconds since 1601): " + startTimestamp);
        System.out.println("End Timestamp (microseconds since 1601): " + endTimestamp);

        // Path to Chrome's History SQLite database file
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {

                // Test Query 1: Check if data exists in the last 2 days
                String sql = "SELECT v.id, v.url, u.title, v.visit_time FROM visits v " +
                        "JOIN urls u ON v.url = u.id WHERE v.visit_time >= ? AND v.visit_time < ? " +
                        "ORDER BY v.visit_time";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, startTimestamp);
                    pstmt.setLong(2, endTimestamp);

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
                            String url = rs.getString("url");
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
                            System.out.println("No visits found in the given time range.");
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

package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.Date;

public class BrowserHistoryTracker_09 {

    public static void main(String[] args) {

        // Get the current date and calculate the time range (e.g., Last 7 days)
        LocalDate currentDate = LocalDate.now();

        // Calculate the start of the range (e.g., last 7 days)
        LocalDate startDate = currentDate.minusDays(10000); // Last 7 days
        ZonedDateTime startOfRange = startDate.atStartOfDay(ZoneId.of("UTC"));
        ZonedDateTime endOfRange = currentDate.atStartOfDay(ZoneId.of("UTC")).plusDays(1).minusNanos(1); // End of today

        // Convert to Chrome's timestamp format (microseconds since Jan 1, 1601)
        long startTimestamp = startOfRange.toInstant().toEpochMilli() * 10000 + 11644473600000000L;
        long endTimestamp = endOfRange.toInstant().toEpochMilli() * 10000 + 11644473600000000L;

        // Path to Chrome's History SQLite database file
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                
                // SQL query to get the visits between the calculated time range
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

                        while (rs.next()) {
                            String url = rs.getString("url");
                            String title = rs.getString("title");
                            long visitTime = rs.getLong("visit_time");

                            // Convert Chrome's timestamp to milliseconds
                            long timestampMillis = (visitTime - 11644473600000000L) / 10;

                            // Format the visit time
                            String visitDate = dateFormatter.format(new Date(timestampMillis));
                            String visitTimeFormatted = timeFormatter.format(new Date(timestampMillis));

                            // Calculate the session time
                            if (previousVisitTime != -1) {
                                long timeDifference = timestampMillis - previousVisitTime;

                                // If the time difference is greater than 10 minutes (600,000 milliseconds), it's a new session
                                if (timeDifference > 600000) {
                                    // End of the previous session, print session details
                                    if (sessionStartTime != -1) {
                                        String sessionStartFormatted = timeFormatter.format(new Date(sessionStartTime));
                                        String sessionEndFormatted = timeFormatter.format(new Date(sessionEndTime));
                                        long durationSeconds = sessionDuration / 1000;
                                        long minutes = durationSeconds / 60;
                                        long seconds = durationSeconds % 60;

                                        System.out.println("Session on " + previousUrl + ": ");
                                        System.out.println("  Start Time: " + sessionStartFormatted);
                                        System.out.println("  End Time: " + sessionEndFormatted);
                                        System.out.println("  Duration: " + minutes + " minutes " + seconds + " seconds");
                                    }
                                    // Start a new session
                                    sessionStartTime = timestampMillis;
                                    sessionEndTime = timestampMillis;
                                    sessionDuration = 0;
                                }
                                sessionDuration += timeDifference;
                                sessionEndTime = timestampMillis;
                            } else {
                                // This is the first visit in the database, start a session
                                sessionStartTime = timestampMillis;
                                sessionEndTime = timestampMillis;
                                sessionDuration = 0;
                            }

                            // Print the current visit info
                            System.out.println("Visited: " + title + " (" + url + ") at " + visitTimeFormatted);

                            // Save the current visit as the previous visit
                            previousVisitTime = timestampMillis;
                            previousUrl = url;
                        }

                        // Print the last session duration
                        if (sessionStartTime != -1) {
                            String sessionStartFormatted = timeFormatter.format(new Date(sessionStartTime));
                            String sessionEndFormatted = timeFormatter.format(new Date(sessionEndTime));
                            long durationSeconds = sessionDuration / 1000;
                            long minutes = durationSeconds / 60;
                            long seconds = durationSeconds % 60;

                            System.out.println("Session on " + previousUrl + ": ");
                            System.out.println("  Start Time: " + sessionStartFormatted);
                            System.out.println("  End Time: " + sessionEndFormatted);
                            System.out.println("  Duration: " + minutes + " minutes " + seconds + " seconds");
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

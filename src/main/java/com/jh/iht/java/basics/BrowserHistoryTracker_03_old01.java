package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;

public class BrowserHistoryTracker_03_old01 {

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
                        "ORDER BY v.visit_time";

                try (PreparedStatement pstmt = conn.prepareStatement(sql);
                     ResultSet rs = pstmt.executeQuery()) {

                    // Date format for displaying the visit time
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                    // Variables to track session data
                    long previousVisitTime = -1;
                    String previousUrl = "";
                    long sessionStartTime = -1;
                    long sessionEndTime = -1;
                    long sessionDuration = 0;

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

                        // Print raw visit_time for debugging
                        System.out.println("Visited: " + title + " (" + url + ")");
                        System.out.println("  Visit Time: " + visitDateFormatted + " " + visitTimeFormatted);

                        // Calculate session duration
                        if (previousVisitTime != -1) {
                            long timeDifference = timestampMillis - previousVisitTime;

                            // If the time difference is greater than 10 minutes (600,000 milliseconds), it's a new session
                            if (timeDifference > 600000) {
                                // End of the previous session, print session details
                                if (sessionStartTime != -1) {
                                    // Calculate session duration in seconds
                                    long durationSeconds = sessionDuration / 1000;
                                    long minutes = durationSeconds / 60;
                                    long seconds = durationSeconds % 60;

                                    // Print session details
                                    System.out.println("Session on " + previousUrl + ": ");
                                    System.out.println("  Start Time: " + timeFormatter.format(new java.util.Date(sessionStartTime)));
                                    System.out.println("  End Time: " + timeFormatter.format(new java.util.Date(sessionEndTime)));
                                    System.out.println("  Duration: " + minutes + " minutes " + seconds + " seconds");
                                }

                                // Start a new session
                                sessionStartTime = timestampMillis;
                                sessionEndTime = timestampMillis;
                                sessionDuration = 0;
                            }

                            // Add time difference to the session duration
                            sessionDuration += timeDifference;
                            sessionEndTime = timestampMillis;
                        } else {
                            // This is the first visit in the database, start a session
                            sessionStartTime = timestampMillis;
                            sessionEndTime = timestampMillis;
                            sessionDuration = 0;
                        }

                        // Save the current visit as the previous visit
                        previousVisitTime = timestampMillis;
                        previousUrl = url;
                    }

                    // Print the last session duration if available
                    if (sessionStartTime != -1) {
                        long durationSeconds = sessionDuration / 1000;
                        long minutes = durationSeconds / 60;
                        long seconds = durationSeconds % 60;

                        System.out.println("Session on " + previousUrl + ": ");
                        System.out.println("  Start Time: " + timeFormatter.format(new java.util.Date(sessionStartTime)));
                        System.out.println("  End Time: " + timeFormatter.format(new java.util.Date(sessionEndTime)));
                        System.out.println("  Duration: " + minutes + " minutes " + seconds + " seconds");
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

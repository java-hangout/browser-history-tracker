package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

public class BrowserHistoryTracker_working {

    public static void main(String[] args) {
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History"; // Path to Chrome's History SQLite database file

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                // Query Chrome's visits and urls
                String sql = "SELECT v.id, v.url, u.title, v.visit_time FROM visits v " +
                             "JOIN urls u ON v.url = u.id ORDER BY v.visit_time";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

                    // Date formatter for display
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                    // Variables for session tracking
                    long previousVisitTime = -1; // To track the previous visit time
                    String previousUrl = ""; // To track the previous URL
                    long sessionStartTime = -1; // Start time of a session
                    long sessionEndTime = -1; // End time of the session
                    long sessionDuration = 0; // Duration of the session in milliseconds

                    while (rs.next()) {
                        String url = rs.getString("url");
                        String title = rs.getString("title");
                        long visitTime = rs.getLong("visit_time");

                        // Convert Chrome's timestamp to milliseconds
                        long timestampMillis = (visitTime - 11644473600000000L) / 10;

                        // Format the date and time
                        String visitDate = dateFormatter.format(new Date(timestampMillis));
                        String visitTimeFormatted = timeFormatter.format(new Date(timestampMillis));

                        // Calculate the session time
                        if (previousVisitTime != -1) {
                            long timeDifference = timestampMillis - previousVisitTime;

                            // If the time difference is greater than 10 minutes (600,000 milliseconds), it's a new session
                            if (timeDifference > 600000) {
                                // End of the previous session, print session details
                                if (sessionStartTime != -1) {
                                    // Convert start and end times to human-readable format
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
                            sessionEndTime = timestampMillis; // Update the end time of the current session
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

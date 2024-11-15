package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.Date;
import java.util.Scanner;

public class BrowserHistoryTracker_withStartandEnd {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for the start and end dates in yyyy-MM-dd format
        System.out.print("Enter start date (yyyy-MM-dd): ");
        String startDateInput = scanner.nextLine().trim();
        
        System.out.print("Enter end date (yyyy-MM-dd): ");
        String endDateInput = scanner.nextLine().trim();

        // Validate both date formats
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        dateFormatter.setLenient(false);
        try {
            // Parse both the start and end dates
            dateFormatter.parse(startDateInput); 
            dateFormatter.parse(endDateInput);
        } catch (ParseException e) {
            System.out.println("Invalid date format. Please use yyyy-MM-dd format.");
            return; // Exit if the date is invalid
        }

        // Path to Chrome's History SQLite database file
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                
                // Convert the start and end dates to timestamps in milliseconds
                LocalDate startDate = LocalDate.parse(startDateInput);
                LocalDate endDate = LocalDate.parse(endDateInput);
                
                ZonedDateTime startOfDay = startDate.atStartOfDay(ZoneId.of("UTC"));
                ZonedDateTime endOfDay = endDate.plusDays(1).atStartOfDay(ZoneId.of("UTC")).minusNanos(1); // End of day for the end date

                // Convert to Chrome's timestamp format (microseconds since 1601-01-01 UTC)
                long startTimestamp = startOfDay.toInstant().toEpochMilli() * 10000 + 11644473600000000L;
                long endTimestamp = endOfDay.toInstant().toEpochMilli() * 10000 + 11644473600000000L;

                // SQL query to get the visits between the specified dates
                String sql = "SELECT v.id, v.url, u.title, v.visit_time FROM visits v " +
                             "JOIN urls u ON v.url = u.id WHERE v.visit_time >= ? AND v.visit_time < ? " +
                             "ORDER BY v.visit_time";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, startTimestamp);
                    pstmt.setLong(2, endTimestamp);

                    try (ResultSet rs = pstmt.executeQuery()) {

                        // Define time formatter for displaying visit times
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
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

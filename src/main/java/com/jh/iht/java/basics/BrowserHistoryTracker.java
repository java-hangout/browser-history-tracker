package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;
import java.util.Scanner;

public class BrowserHistoryTracker {

    public static void main(String[] args) {
        // Path to Chrome's History SQLite database file
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History"; 

        // Create a scanner object to accept user input
        Scanner scanner = new Scanner(System.in);

        // Ask the user for a date (in the format YYYY-MM-DD)
        System.out.print("Enter the date (YYYY-MM-DD): ");
        String inputDate = scanner.nextLine();

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                // Query Chrome history for the given date
                String sql = "SELECT url, title, last_visit_time FROM urls WHERE last_visit_time >= ? AND last_visit_time < ? ORDER BY last_visit_time";
                
                // Prepare the date filter based on user input
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
                Date date = dateFormatter.parse(inputDate);
                
                // Start of the day (00:00:00) in milliseconds
                long startOfDayMillis = date.getTime();
                // End of the day (23:59:59) in milliseconds
                long endOfDayMillis = startOfDayMillis + 86400000L; // 24 hours in milliseconds
                
                // Convert to Chrome's timestamp format (microseconds since January 1, 1601 UTC)
                long startOfDayChrome = (startOfDayMillis + 11644473600000L) * 10;
                long endOfDayChrome = (endOfDayMillis + 11644473600000L) * 10;

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    // Set the date range for the query (start and end of the day in Chrome timestamp format)
                    stmt.setLong(1, startOfDayChrome);
                    stmt.setLong(2, endOfDayChrome);

                    try (ResultSet rs = stmt.executeQuery()) {
                        boolean historyFound = false;
                        while (rs.next()) {
                            String url = rs.getString("url");
                            String title = rs.getString("title");
                            long lastVisitTime = rs.getLong("last_visit_time");

                            // Convert Chrome's timestamp (microseconds since January 1, 1601 UTC) to milliseconds
                            long timestampMillis = (lastVisitTime - 11644473600000000L) / 10;

                            // Get the formatted visit time
                            String visitTime = timeFormatter.format(new Date(timestampMillis));

                            System.out.println("Visited: " + title + " (" + url + ") at " + visitTime);
                            historyFound = true;
                        }

                        if (!historyFound) {
                            System.out.println("No browsing history found for the date: " + inputDate);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException | ParseException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}

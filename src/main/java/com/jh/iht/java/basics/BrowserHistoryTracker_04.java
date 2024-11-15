package com.jh.iht.java.basics;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class BrowserHistoryTracker_04 {

    public static void main(String[] args) throws ClassNotFoundException {
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History"; // Path to Chrome's History SQLite database file
        Class.forName("org.sqlite.JDBC");

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            // Query Chrome history
            String sql = "SELECT url, title, last_visit_time FROM urls ORDER BY last_visit_time";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

                // SimpleDateFormat for custom date format
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                while (rs.next()) {
                    String url = rs.getString("url");
                    String title = rs.getString("title");
                    long lastVisitTime = rs.getLong("last_visit_time");

                    // Debugging: Print the raw timestamp to understand the issue
                    System.out.println("Raw timestamp (microseconds since 1601): " + lastVisitTime);

                    // Adjust the timestamp by subtracting the constant (microseconds between 1601 and 1970) and convert to milliseconds
                    long timestampMillis = (lastVisitTime - 11644473600000000L) / 10;

                    // Debugging: Print the calculated timestamp in milliseconds
                    System.out.println("Calculated timestamp (milliseconds since Unix epoch): " + timestampMillis);

                    // Ensure that the calculated timestamp is within a reasonable range
                    if (timestampMillis < 0) {
                        System.out.println("Calculated timestamp seems invalid. Skipping this entry.");
                        continue;
                    }

                    // Convert the timestamp to Instant and then to Date
                    Instant instant = Instant.ofEpochMilli(timestampMillis);
                    Date date = Date.from(instant);

                    // Format the Date to a human-readable format using SimpleDateFormat
                    String formattedDate = dateFormatter.format(date);

                    // Output the results
                    System.out.println("Visited: " + title + " (" + url + ")");
                    System.out.println("Last Visit: " + formattedDate);
                    System.out.println("------");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

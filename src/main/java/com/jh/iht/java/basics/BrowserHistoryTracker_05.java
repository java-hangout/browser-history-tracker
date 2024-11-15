package com.jh.iht.java.basics;

import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date;

public class BrowserHistoryTracker_05 {

    public static void main(String[] args) {
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History"; // Path to Chrome's History SQLite database file

        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
                // Query Chrome history
                String sql = "SELECT url, title, last_visit_time FROM urls ORDER BY last_visit_time";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    
                    // Map to store the history grouped by day
                    Map<String, List<String>> dayWiseHistory = new TreeMap<>();
                    
                    // Date format to group by day
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                    while (rs.next()) {
                        String url = rs.getString("url");
                        String title = rs.getString("title");
                        long lastVisitTime = rs.getLong("last_visit_time");

                        // Convert Chrome's timestamp (microseconds since January 1, 1601 UTC) to milliseconds
                        long timestampMillis = (lastVisitTime - 11644473600000000L) / 10;

                        // Get the formatted date for day-wise grouping
                        String visitDate = dateFormatter.format(new Date(timestampMillis));
                        String visitTime = timeFormatter.format(new Date(timestampMillis));

                        // Group by day
                        String visitInfo = "Visited: " + title + " (" + url + ") at " + visitTime;
                        dayWiseHistory.computeIfAbsent(visitDate, k -> new ArrayList<>()).add(visitInfo);
                    }

                    // Print day-wise history
                    for (Map.Entry<String, List<String>> entry : dayWiseHistory.entrySet()) {
                        System.out.println("Date: " + entry.getKey());
                        for (String historyEntry : entry.getValue()) {
                            System.out.println("  " + historyEntry);
                        }
                        System.out.println("------");
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

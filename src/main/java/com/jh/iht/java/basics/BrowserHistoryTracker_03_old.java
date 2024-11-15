package com.jh.iht.java.basics;

import java.sql.*;

public class BrowserHistoryTracker_03_old {

    public static void main(String[] args) throws ClassNotFoundException {
        String dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History"; // Path to Chrome's History SQLite database file
        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            // Query Chrome history
            String sql = "SELECT url, title, last_visit_time FROM urls ORDER BY last_visit_time ";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String url = rs.getString("url");
                    String title = rs.getString("title");
                    long lastVisitTime = rs.getLong("last_visit_time");

                    // Convert Chrome's timestamp (microseconds since January 1, 1601 UTC) to milliseconds
                    long timestampMillis = (lastVisitTime - 11644473600000000L) / 10;

                    System.out.println("Visited: " + title + " (" + url + ")");
                    System.out.println("Last Visit: " + new java.util.Date(timestampMillis));
                    System.out.println("------");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

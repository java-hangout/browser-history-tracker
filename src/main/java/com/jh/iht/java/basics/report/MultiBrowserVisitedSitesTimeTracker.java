package com.jh.iht.java.basics.report;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class MultiBrowserVisitedSitesTimeTracker {

    public static void main(String[] args) {

//        String browser = getBrowserName();
        String browser = "chrome"; // Change this based on the browser you want to track
        generateVisitedSitesTimeTrackerRecord(browser);

    }

    private static void generateVisitedSitesTimeTrackerRecord(String browser) {
        String dbPath = getBrowserDatabasePath(browser);
        String csvFilePath = "D:\\workspace\\HistoryTracker\\src\\main\\resources\\template\\browser_history_report_" + browser + ".csv";  // Output CSV file path

        // Setup CSV Writer
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            // Write the header to the CSV file
            writer.write("Title,URL,Visited Date and Time,Total Time Spent (s),Total Time Spent (m)");
            writer.newLine();

            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Establish connection
            // Browser-specific SQL query
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath); PreparedStatement pstmt = conn.prepareStatement(getBrowserSQLQuery(browser));
                 ResultSet rs = pstmt.executeQuery()) {
                // Date format for displaying the visit time
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

                boolean dataFound = false;

                while (rs.next()) {
                    dataFound = true;
                    long visitTime = rs.getLong("visit_time");
                    String url = rs.getString("url");  // Get the actual URL here
                    String title = rs.getString("title");
                    long visitDuration = rs.getLong("visit_duration");  // Visit duration in microseconds

                    // Convert Chrome's timestamp (microseconds since 1601) to Unix timestamp (milliseconds since 1970)
                    long timestampMillis = (visitTime - 11644473600000000L) / 1000; // Convert to milliseconds

                    // Convert Unix timestamps to human-readable dates
                    java.util.Date visitDate = new java.util.Date(timestampMillis);
                    String visitDateFormatted = dateFormatter.format(visitDate);
                    String visitTimeFormatted = timeFormatter.format(visitDate);

                    // Convert visit duration from microseconds to seconds
                    double totalTimeSpent = visitDuration / 1_000_000.0;  // Convert microseconds to seconds
                    // Convert visit duration from seconds to minutes
                    double totalTimeSpentInMinutes = totalTimeSpent / 60;  // Convert seconds to minutes

                    // Write the visit data to the CSV file with the total time spent
                    writer.write(String.format("\"%s\",\"%s\",\"%s %s\",%.2f,%.2f\n",
                            title, url, visitDateFormatted, visitTimeFormatted, totalTimeSpent, totalTimeSpentInMinutes));
                }

                // If no data was found, print a message
                if (!dataFound) {
                    System.out.println("No visits found in the database.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static String getBrowserName() {
        // Choose the browser: "chrome", "firefox", or "edge"
        Scanner scanner = new Scanner(System.in);
        // Prompt the user for the browser name
        System.out.print("Enter Browser Name: ");
        return scanner.nextLine().trim();
    }
    // Get the database path based on browser type
    private static String getBrowserDatabasePath(String browser) {
        String dbPath = "";

        switch (browser.toLowerCase()) {
            case "chrome":
                dbPath = "C:\\Users\\HP\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";
                break;
            case "firefox":
                dbPath = "C:\\Users\\HP\\AppData\\Roaming\\Mozilla\\Firefox\\Profiles\\<profile_name>\\places.sqlite";
                break;
            case "edge":
                dbPath = "C:\\Users\\HP\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\History";
                break;
            default:
                System.out.println("Unsupported browser. Please use chrome, firefox, or edge.");
                break;
        }
        return dbPath;
    }

    // Get the SQL query based on the browser type
    private static String getBrowserSQLQuery(String browser) {
        String sql = "";

        switch (browser.toLowerCase()) {
            case "chrome":
            case "edge":
                // For Chrome and Edge, the query is similar
                sql = "SELECT v.id, u.url, u.title, v.visit_time, v.visit_duration " +
                        "FROM visits v " +
                        "JOIN urls u ON v.url = u.id " +
                        "ORDER BY v.visit_time";
                break;
            case "firefox":
                // Firefox uses different tables (moz_places, moz_historyvisits)
                sql = "SELECT p.url, p.title, v.visit_date, v.visit_duration " +
                        "FROM moz_places p " +
                        "JOIN moz_historyvisits v ON p.id = v.place_id " +
                        "ORDER BY v.visit_date";
                break;
            default:
                System.out.println("Unsupported browser. Please use chrome, firefox, or edge.");
                break;
        }
        return sql;
    }
}

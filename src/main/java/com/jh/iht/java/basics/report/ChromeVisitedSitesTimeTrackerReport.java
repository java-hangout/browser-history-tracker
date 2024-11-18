
package com.jh.iht.java.basics.report;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class ChromeVisitedSitesTimeTrackerReport {

    public static void main(String[] args) {
        generateVisitedSitesTimeTrackerRecord();
    }

    private static void generateVisitedSitesTimeTrackerRecord() {
        // Load properties from the config.properties file
        Properties properties = loadProperties();

        if (properties == null) {
            System.out.println("Could not load properties file. Exiting.");
            return;
        }

        // Get the dbPath and csvFilePath from the properties file
        String dbPath = properties.getProperty("dbPath");
        String csvFilePath = properties.getProperty("csvFilePath");

        // Setup CSV Writer
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath))) {
            // Write the header to the CSV file
            writer.write("Title,URL,Visited Date and Time,Total Time Spent (s),Total Time Spent (m)");
            writer.newLine();

            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            // Establish connection
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath); PreparedStatement pstmt = conn.prepareStatement(getChromeSQLiteQuery());
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
                    // Convert visit duration from seconds minutes
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

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = ChromeVisitedSitesTimeTracker.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return null;
            }
            // Load the properties file
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return properties;
    }

    private static String getChromeSQLiteQuery() {
        // SQL query to fetch actual URL instead of ID, including the visit duration
        return "SELECT v.id, u.url, u.title, v.visit_time, v.visit_duration " +
                "FROM visits v " +
                "JOIN urls u ON v.url = u.id " +
                "ORDER BY v.visit_time";
    }
}
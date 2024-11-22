package com.jh.iht.java.basics.comments;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains utility methods for interacting with the user's browser history.
 * It identifies the installed browsers, retrieves visited site data from each browser's history,
 * and formats the data for further processing or reporting.
 */
public class BrowserUtils {

    /**
     * Checks for the installed browsers (Chrome, Edge, Firefox) and adds them to the list.
     * 
     * @return List of installed browser names.
     */
    public static List<String> getInstalledBrowsers() {
        List<String> browsers = new ArrayList<>();
        addBrowserIfExists(browsers, "chrome", "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        addBrowserIfExists(browsers, "edge", "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
        addBrowserIfExists(browsers, "firefox", "C:\\Program Files\\Mozilla Firefox\\firefox.exe");
        return browsers;
    }

    /**
     * Helper method to check if a browser's executable exists on the system. 
     * If it exists, the browser name is added to the list.
     * 
     * @param browsers List of browsers to add to.
     * @param browserName The name of the browser to check.
     * @param path The path where the browser executable is expected to be.
     */
    public static void addBrowserIfExists(List<String> browsers, String browserName, String path) {
        if (new File(path).exists()) {
            browsers.add(browserName);
        }
    }

    /**
     * Retrieves the list of visited sites from the browser history for a given browser and user.
     * It connects to the browser's SQLite database and queries the history.
     * 
     * @param browserName The name of the browser (e.g., Chrome, Firefox, Edge).
     * @param userName The username for which we want to retrieve the visited sites.
     * @return A JSONArray containing the visited sites data for the given browser.
     */
    public static JSONArray getVisitedSitesForBrowser(String browserName, String userName) {
        JSONArray visitedSitesArray = new JSONArray();
        String dbPath = getBrowserHistoryPath(browserName, userName);

        // If the browser history database path exists, continue processing
        if (dbPath != null) {
            DatabaseUtils.createBackupFromLatestHistory(dbPath);  // Backup the latest browser history
            dbPath += "Backup";  // Use the backup file for querying history
            String query = getBrowserSQLiteQuery(browserName);

            // If a valid query is found for the browser, proceed with querying the database
            if (query != null) {
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                     PreparedStatement pstmt = conn.prepareStatement(query);
                     ResultSet rs = pstmt.executeQuery()) {

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    // Process each result from the query and create a JSON object for each visited site
                    while (rs.next()) {
                        JSONObject visitedSite = new JSONObject();
                        long visitTime = rs.getLong("visit_time");
                        String url = rs.getString("url");
                        String title = rs.getString("title");
                        long visitDuration = rs.getLong("visit_duration");

                        // Convert visit time from raw format to a readable timestamp
                        long timestampMillis = (visitTime - 11644473600000000L) / 1000;
                        String visitedDateTime = dateFormatter.format(new java.util.Date(timestampMillis));
                        double totalTimeSpentInMinutes = (visitDuration / 1_000_000.0) / 60;

                        // Populate the JSON object with visit details
                        visitedSite.put("title", title);
                        visitedSite.put("url", url);
                        visitedSite.put("visitedDateAndTime", visitedDateTime);
                        visitedSite.put("totalTimeSpentInSeconds", visitDuration / 1_000_000.0);
                        visitedSite.put("totalTimeSpentInMinutes", totalTimeSpentInMinutes);

                        visitedSitesArray.put(visitedSite);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();  // Handle database errors
                }
            }
        }
        return visitedSitesArray;
    }

    /**
     * Gets the path to the browser's history database based on the browser type and the user's home directory.
     * 
     * @param browserName The name of the browser (e.g., Chrome, Firefox, Edge).
     * @param userName The username to get the history for.
     * @return The path to the browser's history database, or null if not found.
     */
    private static String getBrowserHistoryPath(String browserName, String userName) {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        // Return the appropriate history path based on the browser and operating system
        switch (browserName.toLowerCase()) {
            case "chrome":
                return getBrowserHistoryPathForChrome(userHome, os);
            case "edge":
                return getBrowserHistoryPathForEdge(userHome, os);
            case "firefox":
                return getBrowserHistoryPathForFirefox(userHome, os);
            default:
                return null;  // Return null if the browser is unsupported
        }
    }

    /**
     * Gets the path to the Chrome browser history based on the operating system.
     * 
     * @param userHome The user's home directory.
     * @param os The operating system name (e.g., Windows, Mac, Linux).
     * @return The path to the Chrome history database, or null if not found.
     */
    private static String getBrowserHistoryPathForChrome(String userHome, String os) {
        if (os.contains("win")) {
            return userHome + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/Google/Chrome/Default/History";
        } else if (os.contains("nix") || os.contains("nux")) {
            return userHome + "/.config/google-chrome/Default/History";
        }
        return null;
    }

    /**
     * Gets the path to the Microsoft Edge browser history based on the operating system.
     * 
     * @param userHome The user's home directory.
     * @param os The operating system name (e.g., Windows, Mac, Linux).
     * @return The path to the Edge history database, or null if not found.
     */
    private static String getBrowserHistoryPathForEdge(String userHome, String os) {
        if (os.contains("win")) {
            return userHome + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\History";
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/Microsoft Edge/Default/History";
        } else if (os.contains("nix") || os.contains("nux")) {
            return userHome + "/.config/microsoft-edge/Default/History";
        }
        return null;
    }

    /**
     * Gets the path to the Firefox browser history based on the operating system.
     * 
     * @param userHome The user's home directory.
     * @param os The operating system name (e.g., Windows, Mac, Linux).
     * @return The path to the Firefox history database, or null if not found.
     */
    private static String getBrowserHistoryPathForFirefox(String userHome, String os) {
        if (os.contains("win")) {
            return userHome + "\\AppData\\Roaming\\Mozilla\\Firefox\\Profiles\\default\\places.sqlite";
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/Firefox/Profiles/default/places.sqlite";
        } else if (os.contains("nix") || os.contains("nux")) {
            return userHome + "/.mozilla/firefox/default/places.sqlite";
        }
        return null;
    }

    /**
     * Retrieves the appropriate SQL query to fetch the visited sites data from the browser's history database.
     * 
     * @param browserName The name of the browser (e.g., Chrome, Firefox, Edge).
     * @return The SQL query to retrieve the visited sites, or null if the browser is unsupported.
     */
    private static String getBrowserSQLiteQuery(String browserName) {
        if (browserName.equalsIgnoreCase("chrome") || browserName.equalsIgnoreCase("edge")) {
            return "SELECT v.id, u.url, u.title, v.visit_time, v.visit_duration " +
                    "FROM visits v " +
                    "JOIN urls u ON v.url = u.id " +
                    "WHERE DATE(datetime(v.visit_time / 1000000 - 11644473600, 'unixepoch')) = DATE('now') " +
                    "ORDER BY v.visit_time";
        }
        // For Firefox, we assume places.sqlite has 'moz_places' table
        if (browserName.equalsIgnoreCase("firefox")) {
            return "SELECT moz_places.url, moz_places.title, moz_historyvisits.visit_date " +
                    "FROM moz_places " +
                    "JOIN moz_historyvisits ON moz_places.id = moz_historyvisits.place_id " +
                    "WHERE DATE(datetime(moz_historyvisits.visit_date / 1000, 'unixepoch')) = DATE('now')";
        }
        return null;  // Return null if the browser does not match known types
    }
}

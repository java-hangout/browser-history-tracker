package com.jh.iht.java.basics.optimizedreport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BrowserUtils {

    // This function checks for installed browsers and adds them to the list
    public static List<String> getInstalledBrowsers() {
        List<String> browsers = new ArrayList<>();
        addBrowserIfExists(browsers, "chrome", "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        addBrowserIfExists(browsers, "edge", "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
        addBrowserIfExists(browsers, "firefox", "C:\\Program Files\\Mozilla Firefox\\firefox.exe");
        return browsers;
    }

    // This helper function adds browser to the list if the corresponding executable exists
    public static void addBrowserIfExists(List<String> browsers, String browserName, String path) {
        if (new File(path).exists()) {
            browsers.add(browserName);
        }
    }

    // This function generates the visited sites data for each browser
    public static JSONArray getVisitedSitesForBrowser(String browserName, String userName) {
        JSONArray visitedSitesArray = new JSONArray();
        String dbPath = getBrowserHistoryPath(browserName, userName);

        if (dbPath != null) {
            DatabaseUtils.createBackupFromLatestHistory(dbPath);
            dbPath += "Backup";
            String query = getBrowserSQLiteQuery(browserName);

            if (query != null) {
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
                     PreparedStatement pstmt = conn.prepareStatement(query);
                     ResultSet rs = pstmt.executeQuery()) {

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    while (rs.next()) {
                        JSONObject visitedSite = new JSONObject();
                        long visitTime = rs.getLong("visit_time");
                        String url = rs.getString("url");
                        String title = rs.getString("title");
                        long visitDuration = rs.getLong("visit_duration");

                        long timestampMillis = (visitTime - 11644473600000000L) / 1000; // Convert to timestamp
                        String visitedDateTime = dateFormatter.format(new java.util.Date(timestampMillis));
                        double totalTimeSpentInMinutes = (visitDuration / 1_000_000.0) / 60;

                        visitedSite.put("title", title);
                        visitedSite.put("url", url);
                        visitedSite.put("visitedDateAndTime", visitedDateTime);
                        visitedSite.put("totalTimeSpentInSeconds", visitDuration / 1_000_000.0);
                        visitedSite.put("totalTimeSpentInMinutes", totalTimeSpentInMinutes);

                        visitedSitesArray.put(visitedSite);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return visitedSitesArray;
    }

    // Get browser history path based on the browser name
    private static String getBrowserHistoryPath(String browserName, String userName) {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        switch (browserName.toLowerCase()) {
            case "chrome":
                return getBrowserHistoryPathForChrome(userHome, os);
            case "edge":
                return getBrowserHistoryPathForEdge(userHome, os);
            case "firefox":
                return getBrowserHistoryPathForFirefox(userHome, os);
            default:
                return null;
        }
    }

    // Get path for Chrome browser history
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

    // Get path for Edge browser history
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

    // Get path for Firefox browser history
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

    // Query to retrieve visit details from browser history SQLite DB
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
        return null;
    }
}

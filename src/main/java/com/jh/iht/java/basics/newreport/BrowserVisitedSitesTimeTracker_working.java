package com.jh.iht.java.basics.newreport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BrowserVisitedSitesTimeTracker_working {

    public static void main(String[] args) {
        List<String> loggedInUsers = getLoggedInUsers();
        if (loggedInUsers != null && !loggedInUsers.isEmpty()) {
            for (String userName : loggedInUsers) {
                System.out.println("userName : "+userName);
                generateVisitedSitesTimeTrackerRecord(userName);
            }
        }
    }

    private static List<String> getLoggedInUsers() {
        List<String> loggedInUsers = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        try {
            // For Windows: Use PowerShell to get logged-in users
            if (os.contains("win")) {
                String command = "powershell.exe Get-WmiObject -Class Win32_ComputerSystem | Select-Object -ExpandProperty UserName";

                Process process = Runtime.getRuntime().exec(command);

                // Start a thread to handle the output
                Thread outputThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Filter out empty or console login entries
                            if (line != null && !line.trim().isEmpty() && !line.trim().equalsIgnoreCase("Console")) {
                                loggedInUsers.add(line.trim());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Start the output handling thread
                outputThread.start();

                // Also handle the error stream
                Thread errorThread = new Thread(() -> {
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            System.err.println("Error: " + errorLine); // Log errors if any
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // Start the error handling thread
                errorThread.start();

                // Wait for the process to finish with a timeout
                boolean finished = process.waitFor(10, TimeUnit.SECONDS); // Wait for 10 seconds max

                if (!finished) {
                    System.err.println("Process timed out!");
                    process.destroy(); // Destroy the process if it times out
                }

                // Wait for both threads to finish reading the output
                outputThread.join();
                errorThread.join();
            }
            // For Linux/macOS: Use the "who" command to get logged-in users
            else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                Process process = Runtime.getRuntime().exec("who");

                // Handle output and errors as before
                Thread outputThread = new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String user = line.split("\\s+")[0]; // Extract the username from the output
                            if (!loggedInUsers.contains(user)) {
                                loggedInUsers.add(user);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                outputThread.start();

                Thread errorThread = new Thread(() -> {
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            System.err.println("Error: " + errorLine); // Log errors if any
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                errorThread.start();

                boolean finished = process.waitFor(10, TimeUnit.SECONDS); // Wait for 10 seconds max

                if (!finished) {
                    System.err.println("Process timed out!");
                    process.destroy(); // Destroy the process if it times out
                }

                outputThread.join();
                errorThread.join();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return loggedInUsers;
    }

    private static void generateVisitedSitesTimeTrackerRecord(String userName) {
        String onlyUserName = userName.contains("\\") ? userName.split("\\\\")[1] : userName;
        System.out.println("userName 111 : "+userName);
        System.out.println("onlyUserName : "+onlyUserName);
        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String folderCurrentDate = new SimpleDateFormat("ddMMMyyyy").format(new Date());
        String path = "D:\\workspace\\HistoryTracker\\src\\main\\resources\\template\\"+folderCurrentDate+"\\"; // Change path as needed
        System.out.println("path : "+path);
        System.out.println("userName : "+userName);
        String jsonFileName = path + onlyUserName + "_" + timestamp + ".json";
        System.out.println("jsonFileName : "+jsonFileName);

        // Ensure that the directory exists before writing the file
        File file = new File(jsonFileName);
        File directory = file.getParentFile();  // Get the directory part of the file path

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            boolean dirsCreated = directory.mkdirs();  // Create the directory and any necessary parent directories
            if (dirsCreated) {
                System.out.println("Directory created: " + directory.getPath());
            } else {
                System.err.println("Failed to create directory: " + directory.getPath());
            }
        }
        // Create the root JSON object with ordered fields
        JSONObject jsonReport = new JSONObject();
        jsonReport.put("userName", userName);
        jsonReport.put("date", currentDate);

        // Detect installed browsers dynamically
        List<String> installedBrowsers = getInstalledBrowsers();

        // Prepare the "browsers" array to be added to the JSON
        JSONArray browsersArray = new JSONArray();

        // Iterate through each installed browser and get visited sites
        for (String browser : installedBrowsers) {
            JSONObject browserData = new JSONObject();
            browserData.put("browserName", browser);
            JSONArray visitedSitesArray = getVisitedSitesForBrowser(browser, userName);
            browserData.put("visitedSites", visitedSitesArray);

            // Add browser data to browsers array
            browsersArray.put(browserData);
        }

        // Add the browsers array to the report
        jsonReport.put("browsers", browsersArray);

        // Write the JSON report to file with pretty formatting
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFileName))) {
            writer.write(jsonReport.toString(4)); // Pretty print with indentation of 4 spaces
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getInstalledBrowsers() {
        // Logic to detect installed browsers dynamically
        List<String> browsers = new ArrayList<>();
        File chromePath = new File("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        if (chromePath.exists()) browsers.add("chrome");

        File edgePath = new File("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
        if (edgePath.exists()) browsers.add("edge");

        // Add other browsers like Firefox, Opera, etc., if needed
        return browsers;
    }

    private static JSONArray getVisitedSitesForBrowser(String browserName, String userName) {
        JSONArray visitedSitesArray = new JSONArray();
        String dbPath = "";
        String query = "";

        // Select database and query based on browser name
        if ("chrome".equalsIgnoreCase(browserName)) {
            dbPath = getChromeHistoryPath(userName);
            query = getChromeSQLiteQuery();
        } else if ("edge".equalsIgnoreCase(browserName)) {
            dbPath = getEdgeHistoryPath(userName);
            query = getEdgeSQLiteQuery();
        }

        if (dbPath == null || query == null) {
            return visitedSitesArray; // Return an empty array if paths or queries are null
        }

        try {
            Class.forName("org.sqlite.JDBC");
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

                    long timestampMillis = (visitTime - 11644473600000000L) / 1000; // Convert to milliseconds
                    java.util.Date visitDate = new java.util.Date(timestampMillis);
                    String visitedDateTime = dateFormatter.format(visitDate);

                    double totalTimeSpent = visitDuration / 1_000_000.0;
                    double totalTimeSpentInMinutes = totalTimeSpent / 60;

                    // Ensure fields are ordered correctly as specified
                    visitedSite.put("title", title);
                    visitedSite.put("url", url);
                    visitedSite.put("visitedDateAndTime", visitedDateTime);
                    visitedSite.put("totalTimeSpentInSeconds", totalTimeSpent);
                    visitedSite.put("totalTimeSpentInMinutes", totalTimeSpentInMinutes);

                    // Add visited site to the visitedSitesArray
                    visitedSitesArray.put(visitedSite);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return visitedSitesArray;
    }

    private static String getChromeSQLiteQuery() {
        // We don't need to format the date here anymore; the query will do that
        return "SELECT v.id, u.url, u.title, v.visit_time, v.visit_duration " +
                "FROM visits v " +
                "JOIN urls u ON v.url = u.id " +
                "WHERE DATE(datetime(v.visit_time / 1000000 - 11644473600, 'unixepoch')) = DATE('now') " +
                "ORDER BY v.visit_time";
    }

    private static String getEdgeSQLiteQuery() {
        // Same update here; use the system's current date (local time) in the query
        return "SELECT v.id, u.url, u.title, v.visit_time, v.visit_duration " +
                "FROM visits v " +
                "JOIN urls u ON v.url = u.id " +
                "WHERE DATE(datetime(v.visit_time / 1000000 - 11644473600, 'unixepoch')) = DATE('now') " +
                "ORDER BY v.visit_time";
    }


    /*private static String getChromeSQLiteQuery() {
        // Get today's date in YYYY-MM-DD format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        return "SELECT v.id, u.url, u.title, v.visit_time, v.visit_duration " +
                "FROM visits v " +
                "JOIN urls u ON v.url = u.id " +
                "WHERE strftime('%Y-%m-%d', datetime(v.visit_time / 1000000 - 11644473600, 'unixepoch')) = '" + todayDate + "' " +
                "ORDER BY v.visit_time";
    }

    private static String getEdgeSQLiteQuery() {
        // Get today's date in YYYY-MM-DD format
        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        return "SELECT v.id, u.url, u.title, v.visit_time, v.visit_duration " +
                "FROM visits v " +
                "JOIN urls u ON v.url = u.id " +
                "WHERE strftime('%Y-%m-%d', datetime(v.visit_time / 1000000 - 11644473600, 'unixepoch')) = '" + todayDate + "' " +
                "ORDER BY v.visit_time";
    }*/

    // Get Chrome's History path dynamically based on the system user and OS
    private static String getChromeHistoryPath(String userName) {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();  // Get the operating system

        String chromeHistoryPath = "";
        if (os.contains("win")) {
            chromeHistoryPath = userHome + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";
            System.out.println("chromeHistoryPath : "+chromeHistoryPath);
        } else if (os.contains("mac")) {
            chromeHistoryPath = userHome + "/Library/Application Support/Google/Chrome/Default/History";
        } else if (os.contains("nix") || os.contains("nux")) {
            chromeHistoryPath = userHome + "/.config/google-chrome/Default/History";
        }

        return chromeHistoryPath;
    }

    // Get Edge's History path dynamically based on the system user and OS
    private static String getEdgeHistoryPath(String userName) {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();  // Get the operating system

        String edgeHistoryPath = "";
        if (os.contains("win")) {
            edgeHistoryPath = userHome + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\History";
            System.out.println("edgeHistoryPath : "+edgeHistoryPath);
        } else if (os.contains("mac")) {
            edgeHistoryPath = userHome + "/Library/Application Support/Microsoft Edge/Default/History";
        } else if (os.contains("nix") || os.contains("nux")) {
            edgeHistoryPath = userHome + "/.config/microsoft-edge/Default/History";
        }

        return edgeHistoryPath;
    }
}

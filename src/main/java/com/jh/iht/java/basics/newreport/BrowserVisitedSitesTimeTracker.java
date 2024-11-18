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

/**
 * @author Veeresh N
 * @version 1.0
 */
public class BrowserVisitedSitesTimeTracker {

    private static final String BASE_PATH = "D:\\workspace\\HistoryTracker\\src\\main\\resources\\template\\";

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
            if (os.contains("win")) {
                getLoggedInUsersWindows(loggedInUsers);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                getLoggedInUsersUnix(loggedInUsers);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return loggedInUsers;
    }

    private static void getLoggedInUsersWindows(List<String> loggedInUsers) throws IOException, InterruptedException {
        String command = "powershell.exe Get-WmiObject -Class Win32_ComputerSystem | Select-Object -ExpandProperty UserName";
        Process process = Runtime.getRuntime().exec(command);

        Thread outputThread = new Thread(() -> processOutput(loggedInUsers, process.getInputStream()));
        Thread errorThread = new Thread(() -> processError(process.getErrorStream()));

        outputThread.start();
        errorThread.start();

        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            System.err.println("Process timed out!");
            process.destroy();
        }

        outputThread.join();
        errorThread.join();
    }

    private static void getLoggedInUsersUnix(List<String> loggedInUsers) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("who");

        Thread outputThread = new Thread(() -> processOutput(loggedInUsers, process.getInputStream()));
        Thread errorThread = new Thread(() -> processError(process.getErrorStream()));

        outputThread.start();
        errorThread.start();

        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            System.err.println("Process timed out!");
            process.destroy();
        }

        outputThread.join();
        errorThread.join();
    }

    private static void processOutput(List<String> loggedInUsers, InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //String user = (line.contains("\\") ? line.split("\\\\")[1] : line).trim();
                String user = line.trim();
                if (!user.isEmpty() && !user.equalsIgnoreCase("Console") && !loggedInUsers.contains(user)) {
                    loggedInUsers.add(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processError(InputStream errorStream) {
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("Error: " + errorLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateVisitedSitesTimeTrackerRecord(String userName) {
        String onlyUserName = userName.contains("\\") ? userName.split("\\\\")[1] : userName;
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String folderCurrentDate = new SimpleDateFormat("ddMMMyyyy").format(new Date());
        String path = BASE_PATH + folderCurrentDate + "\\"; 
        String jsonFileName = path + onlyUserName + "_" + timestamp + ".json";

        ensureDirectoryExists(path);

        JSONObject jsonReport = createJsonReport(userName);

        // Detect installed browsers dynamically
        List<String> installedBrowsers = getInstalledBrowsers();
        JSONArray browsersArray = new JSONArray();

        for (String browser : installedBrowsers) {
            JSONObject browserData = new JSONObject();
            browserData.put("browserName", browser);
            JSONArray visitedSitesArray = getVisitedSitesForBrowser(browser, userName);
            browserData.put("visitedSites", visitedSitesArray);
            browsersArray.put(browserData);
        }

        jsonReport.put("browsers", browsersArray);

        writeJsonToFile(jsonFileName, jsonReport);
    }

    private static void ensureDirectoryExists(String path) {
        File directory = new File(path);
        if (!directory.exists() && directory.mkdirs()) {
            System.out.println("Directory created: " + directory.getPath());
        }
    }

    private static JSONObject createJsonReport(String userName) {
        JSONObject jsonReport = new JSONObject();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        jsonReport.put("userName", userName);
        jsonReport.put("date", currentDate);
        return jsonReport;
    }

    private static void writeJsonToFile(String filePath, JSONObject jsonReport) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(jsonReport.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getInstalledBrowsers() {
        List<String> browsers = new ArrayList<>();
        addBrowserIfExists(browsers, "chrome", "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        addBrowserIfExists(browsers, "edge", "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
        return browsers;
    }

    private static void addBrowserIfExists(List<String> browsers, String browserName, String path) {
        if (new File(path).exists()) {
            browsers.add(browserName);
        }
    }

    private static JSONArray getVisitedSitesForBrowser(String browserName, String userName) {
        JSONArray visitedSitesArray = new JSONArray();
        String dbPath = getBrowserHistoryPath(browserName, userName);
        String query = getBrowserSQLiteQuery(browserName);

        if (dbPath == null || query == null) {
            return visitedSitesArray;
        }

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

                long timestampMillis = (visitTime - 11644473600000000L) / 1000;
                String visitedDateTime = dateFormatter.format(new Date(timestampMillis));
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

        return visitedSitesArray;
    }

    private static String getBrowserHistoryPath(String browserName, String userName) {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();

        if ("chrome".equalsIgnoreCase(browserName)) {
            return getChromeHistoryPath(userHome, os);
        } else if ("edge".equalsIgnoreCase(browserName)) {
            return getEdgeHistoryPath(userHome, os);
        }
        return null;
    }

    private static String getChromeHistoryPath(String userHome, String os) {
        if (os.contains("win")) {
            return userHome + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\History";
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/Google/Chrome/Default/History";
        } else if (os.contains("nix") || os.contains("nux")) {
            return userHome + "/.config/google-chrome/Default/History";
        }
        return null;
    }

    private static String getEdgeHistoryPath(String userHome, String os) {
        if (os.contains("win")) {
            return userHome + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\History";
        } else if (os.contains("mac")) {
            return userHome + "/Library/Application Support/Microsoft Edge/Default/History";
        } else if (os.contains("nix") || os.contains("nux")) {
            return userHome + "/.config/microsoft-edge/Default/History";
        }
        return null;
    }

    private static String getBrowserSQLiteQuery(String browserName) {
        return "SELECT v.id, u.url, u.title, v.visit_time, v.visit_duration " +
               "FROM visits v " +
               "JOIN urls u ON v.url = u.id " +
               "WHERE DATE(datetime(v.visit_time / 1000000 - 11644473600, 'unixepoch')) = DATE('now') " +
               "ORDER BY v.visit_time";
    }
}
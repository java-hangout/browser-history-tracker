package com.jh.iht.java.basics.comments;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This utility class provides methods to generate JSON reports.
 * It can create a JSON object containing user information and the current date.
 */
public class JsonUtils {

    /**
     * This method creates a JSON report for a given user, including their username and the current date.
     *
     * @param userName The name of the user for whom the report is being created.
     * @return A JSONObject containing the user's name and the current date.
     */
    public static JSONObject createJsonReport(String userName) {
        JSONObject jsonReport = new JSONObject();
        // Get the current date in the format "dd-MM-yyyy"
        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        jsonReport.put("userName", userName);
        jsonReport.put("date", currentDate);
        return jsonReport;
    }
}

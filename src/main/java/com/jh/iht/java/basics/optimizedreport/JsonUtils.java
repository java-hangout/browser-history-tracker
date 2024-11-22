package com.jh.iht.java.basics.optimizedreport;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonUtils {

    public static JSONObject createJsonReport(String userName) {
        JSONObject jsonReport = new JSONObject();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        jsonReport.put("userName", userName);
        jsonReport.put("date", currentDate);
        return jsonReport;
    }
}

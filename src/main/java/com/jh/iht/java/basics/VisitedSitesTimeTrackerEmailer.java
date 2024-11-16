package com.jh.iht.java.basics;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Scanner;

public class VisitedSitesTimeTrackerEmailer {

    public static void main(String[] args) {
//        String browser = getBrowserName();
        String browser = "chrome"; // Change this based on the browser you want to track
//        String browser = "edge";
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
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);PreparedStatement pstmt = conn.prepareStatement(getBrowserSQLQuery(browser));
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
        // Send the email with the generated CSV file
        sendEmailWithAttachment(csvFilePath, browser);
    }

    private static String getBrowserName() {
        // Choose the browser: "chrome", "firefox", or "edge"
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for the browser name
        System.out.print("Enter Browser Name: ");
        return scanner.nextLine().trim();
    }

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

    private static void sendEmailWithAttachment(String filePath, String browser) {
        System.out.println("filePath  ---> " + filePath);
        // Check if the file exists and has content
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            System.out.println("Error: The attachment file is either missing or empty.");
            return;
        }
        // Set up the mail server properties
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.port", "587");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.auth", "true");

        // Authenticate the sender's email
        String username = "xxxxxxx@gmail.com";  // Your Gmail address
        String password = "xxxxxxx";  // Your Gmail password

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create the email content
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("contacts.veeresh@gmail.com"));
            message.setSubject(capitalizeFirstLetter(browser) + " Visited Sites Time Tracker Report");

            // Create the message body part
            BodyPart messageBodyPart = new MimeBodyPart();
            //messageBodyPart.setText("Please find the attached visited Sites Time tracker report.");
            Address[] addresses = message.getRecipients(Message.RecipientType.TO);
            String email = String.valueOf(addresses[0]);
            int index = email.indexOf('@');
            String modifiedEmail = (index != -1) ? email.substring(0, index) : email;

            // Create the plain text email body with simulated bold text
            String textBody = "Attached is Visited Sites Time Tracker Report for your reference. "
                    + "This report contains detailed information about the websites visited and the time spent on them in the specified browsers. "
                    + "It includes the following key data points:\n"
                    + "\tTitle: The title of the visited website\n"
                    + "\tURL: The URL of the website\n"
                    + "\tVisited Date and Time: The date and time the site was visited\n"
                    + "\tTotal Time Spent (in seconds and minutes): Duration spent on the website\n"
                    + "Please feel free to review the data and let me know if you have any questions or require further details. "
                    + "Any specific adjustments or additional information, do not hesitate to reach out.\n"
                    + "Thank you, and I look forward to your feedback.\n\n"
                    + "Best regards,\n"
                    + "IT Service Desk";
            messageBodyPart.setText("Dear " + modifiedEmail + ",\n\n" + textBody);

            // Create the attachment part
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(filePath);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName("browser_history_report_" + browser + ".csv");

            // Combine the body and the attachment
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            // Set the content of the message
            message.setContent(multipart);

            // Send the email
            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Method to capitalize the first letter of a string
    private static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;  // Return the same if the input is null or empty
        }
        // Capitalize first letter and append the rest of the string in lowercase
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

}

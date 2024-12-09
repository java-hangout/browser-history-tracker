package com.jh.iht.java.basics.optimizedreport;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

/**
 * @author Veeresh N
 * @version 1.0
 */
public class GmailUtils {
    public static void sendGmailWithAttachment(String filePath, String userName) {
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
        String senderGmailId = "contacts.veereshn@gmail.com";  // Your Gmail address
        String senderGmailPW = "**********";  // Your Gmail password

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderGmailId, senderGmailPW);
            }
        });

        try {
            // Create the email content
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderGmailId));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("contacts.veeresh@gmail.com"));
            message.setSubject(capitalizeFirstLetter(userName) + " Visited Sites Time Tracker Report");

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
            attachmentBodyPart.setFileName("browser_history_report_" + userName + ".csv");

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

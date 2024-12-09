package com.jh.iht.java.basics.comments;

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
 * This class contains utility methods for sending emails via Outlook (Office 365 SMTP)
 * with an attachment, such as sending browser history reports.
 * 
 * @version 1.0
 * @author Veeresh N
 */
public class OutlookUtils {

    /**
     * Sends an email with the specified attachment to a recipient via Outlook's SMTP server.
     * The email includes a customized subject and body, with the attachment being the provided file.
     * 
     * @param filePath The path to the attachment file to be sent in the email.
     * @param userName The user name to be used in the email body and subject.
     */
    public static void sendOutlookWithAttachment(String filePath, String userName) {
        System.out.println("filePath  ---> " + filePath);
        
        // Check if the file exists and has content
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            System.out.println("Error: The attachment file is either missing or empty.");
            return;
        }

        // Set up the mail server properties for Outlook SMTP
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "smtp.office365.com");  //devsmtp.osb.co.uk -- Outlook SMTP server
        properties.setProperty("mail.smtp.port", "587");  // Port number for Outlook SMTP
        properties.setProperty("mail.smtp.starttls.enable", "true");  // Enable STARTTLS
        properties.setProperty("mail.smtp.auth", "true");  // Enable authentication

        // Authenticate the sender's email (Outlook email)
        String senderOutlookId = "your-outlook-email@outlook.com";  // Your Outlook email address
        String senderOutlookPW = "your-outlook-email-password";  // Your Outlook password

        // Create a session with authentication
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderOutlookId, senderOutlookPW);
            }
        });

        try {
            // Create the email content
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderOutlookId));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("recipient-email@domain.com"));  // Recipient's email
            message.setSubject(capitalizeFirstLetter(userName) + " Visited Sites Time Tracker Report");

            // Create the message body part
            BodyPart messageBodyPart = new MimeBodyPart();
            
            // Create the plain text email body
            Address[] addresses = message.getRecipients(Message.RecipientType.TO);
            String email = String.valueOf(addresses[0]);
            int index = email.indexOf('@');
            String senderMailID = (index != -1) ? email.substring(0, index) : email;

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
            messageBodyPart.setText("Dear " + senderMailID + ",\n\n" + textBody);

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

    /**
     * Capitalizes the first letter of the input string and converts the rest of the string to lowercase.
     * 
     * @param input The string to be modified.
     * @return The input string with the first letter capitalized and the rest in lowercase.
     */
    private static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;  // Return the same if the input is null or empty
        }
        // Capitalize first letter and append the rest of the string in lowercase
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}

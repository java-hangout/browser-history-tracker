package com.jh.iht.java.basics.comments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for system-related functions, such as retrieving the list of logged-in users
 * on the machine depending on the operating system (Windows or Unix-based).
 */
public class SystemUtils {

    /**
     * Retrieves the list of currently logged-in users depending on the operating system.
     * It executes appropriate system commands to fetch this information based on whether
     * the system is running Windows or Unix-based (Linux, Mac).
     * 
     * @return A list of logged-in user names.
     */
    public static List<String> getLoggedInUsers() {
        List<String> loggedInUsers = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();  // Get the OS name

        try {
            // Check the OS and call the corresponding method to get logged-in users
            if (os.contains("win")) {
                getLoggedInUsersWindows(loggedInUsers);  // For Windows
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                getLoggedInUsersUnix(loggedInUsers);  // For Unix-like systems (Linux, macOS)
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return loggedInUsers;  // Return the list of logged-in users
    }

    /**
     * Retrieves the list of logged-in users on a Windows machine by executing a PowerShell command.
     * The command queries the system for the current logged-in user.
     * 
     * @param loggedInUsers A list to which the logged-in users will be added.
     * @throws IOException If an I/O error occurs while executing the command.
     * @throws InterruptedException If the process is interrupted while waiting.
     */
    private static void getLoggedInUsersWindows(List<String> loggedInUsers) throws IOException, InterruptedException {
        // Execute PowerShell command to get the logged-in user on Windows
        executeCommand("powershell.exe Get-WmiObject -Class Win32_ComputerSystem | Select-Object -ExpandProperty UserName", loggedInUsers);
    }

    /**
     * Retrieves the list of logged-in users on a Unix-based machine (Linux, macOS) by executing the "who" command.
     * 
     * @param loggedInUsers A list to which the logged-in users will be added.
     * @throws IOException If an I/O error occurs while executing the command.
     * @throws InterruptedException If the process is interrupted while waiting.
     */
    private static void getLoggedInUsersUnix(List<String> loggedInUsers) throws IOException, InterruptedException {
        // Execute "who" command to get the logged-in users on Unix-based systems
        executeCommand("who", loggedInUsers);
    }

    /**
     * Executes the specified system command and processes its output and error streams.
     * The output of the command is processed to extract logged-in users and add them to the list.
     * 
     * @param command The system command to be executed.
     * @param loggedInUsers A list to which the logged-in users will be added.
     * @throws IOException If an I/O error occurs while executing the command.
     * @throws InterruptedException If the process is interrupted while waiting.
     */
    private static void executeCommand(String command, List<String> loggedInUsers) throws IOException, InterruptedException {
        // Execute the system command
        Process process = Runtime.getRuntime().exec(command);

        // Start threads to handle the output and error streams of the process
        Thread outputThread = new Thread(() -> processOutput(loggedInUsers, process.getInputStream()));
        Thread errorThread = new Thread(() -> processError(process.getErrorStream()));

        // Start the threads
        outputThread.start();
        errorThread.start();

        // Wait for the process to complete or timeout after 10 seconds
        if (!process.waitFor(10, TimeUnit.SECONDS)) {
            System.err.println("Process timed out!");
            process.destroy();
        }

        // Wait for both threads to finish
        outputThread.join();
        errorThread.join();
    }

    /**
     * Processes the output stream of the system command to extract logged-in users and add them to the list.
     * 
     * @param loggedInUsers A list to which the logged-in users will be added.
     * @param inputStream The input stream of the system command's output.
     */
    private static void processOutput(List<String> loggedInUsers, InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String user = line.trim();  // Trim any whitespace
                if (!user.isEmpty() && !user.equalsIgnoreCase("Console") && !loggedInUsers.contains(user)) {
                    loggedInUsers.add(user);  // Add the user to the list if it's valid
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the error stream of the system command and prints any error messages to the error stream.
     * 
     * @param errorStream The error stream of the system command.
     */
    private static void processError(InputStream errorStream) {
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("Error: " + errorLine);  // Print any error lines
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

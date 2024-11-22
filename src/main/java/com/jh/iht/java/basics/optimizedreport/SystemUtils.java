package com.jh.iht.java.basics.optimizedreport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SystemUtils {

    public static List<String> getLoggedInUsers() {
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
        executeCommand("powershell.exe Get-WmiObject -Class Win32_ComputerSystem | Select-Object -ExpandProperty UserName", loggedInUsers);
    }

    private static void getLoggedInUsersUnix(List<String> loggedInUsers) throws IOException, InterruptedException {
        executeCommand("who", loggedInUsers);
    }

    private static void executeCommand(String command, List<String> loggedInUsers) throws IOException, InterruptedException {
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

    private static void processOutput(List<String> loggedInUsers, InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
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
}

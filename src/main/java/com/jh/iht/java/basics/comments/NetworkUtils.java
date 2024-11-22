package com.jh.iht.java.basics.comments;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * This class provides utility methods for network-related operations, such as retrieving the system's IP address.
 */
public class NetworkUtils {

    /**
     * Retrieves the system's local IP address. It iterates through all available network interfaces 
     * and returns the first non-loopback IPv4 address found.
     *
     * @return The local IP address of the system as a string, or "IP Not Found" if no valid IP is found.
     */
    public static String getSystemIpAddress() {
        try {
            // Get all network interfaces on the system
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            // Iterate through each network interface
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                // Get the IP addresses associated with the network interface
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                // Iterate through each IP address
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    // Skip loopback addresses (127.0.0.1) and select the first IPv4 address
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                        return inetAddress.getHostAddress();  // Return the system's IP address
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();  // Print the error if there is an issue retrieving network interfaces
        }
        return "IP Not Found";  // Return this if no valid IP address was found
    }
}

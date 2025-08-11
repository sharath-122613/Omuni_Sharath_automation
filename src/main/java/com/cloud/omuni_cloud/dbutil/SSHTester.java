package com.cloud.omuni_cloud.dbutil;

import com.jcraft.jsch.*;

/**
 * Utility class to test SSH connection and port forwarding
 */
public class SSHTester {
    
    public static void main(String[] args) {
        // SSH connection details
        String sshHost = "10.10.10.10";
        int sshPort = 22;
        String sshUsername = "ubuntu";
        String sshPassword = "Omni@123";
        
        // MySQL connection details
        String dbHost = "10.10.10.10";
        int dbPort = 3306;
        
        JSch jsch = new JSch();
        Session session = null;
        
        try {
            // 1. Set up the SSH session
            System.out.println("Setting up SSH session to " + sshHost + ":" + sshPort);
            session = jsch.getSession(sshUsername, sshHost, sshPort);
            session.setPassword(sshPassword);
            
            // Avoid asking for key confirmation
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            
            // 2. Connect to the SSH server
            System.out.println("Connecting to SSH server...");
            session.connect();
            System.out.println("✓ Connected to SSH server");
            
            // 3. Set up port forwarding
            int localPort = 3307; // Local port to forward to
            System.out.println("\nSetting up port forwarding:");
            System.out.println("Local port " + localPort + " -> " + dbHost + ":" + dbPort);
            
            int assignedPort = session.setPortForwardingL(localPort, dbHost, dbPort);
            System.out.println("✓ Port forwarding active on localhost:" + assignedPort);
            
            // 4. Test the connection
            System.out.println("\nSSH tunnel is active. You can now connect to MySQL using:");
            System.out.println("Host: localhost");
            System.out.println("Port: " + localPort);
            System.out.println("Username: optimusrw");
            System.out.println("Password: Optimu$#2024");
            
            // Keep the connection open for testing
            System.out.println("\nSSH tunnel is active. Press Ctrl+C to exit...");
            
            // Keep the program running
            Thread.sleep(Long.MAX_VALUE);
            
        } catch (JSchException e) {
            System.err.println("\nSSH Error: " + e.getMessage());
            if (e.getMessage().contains("Auth fail")) {
                System.err.println("Authentication failed. Please check the username and password.");
            } else if (e.getMessage().contains("Connection refused")) {
                System.err.println("Connection refused. The SSH server might be down or the port might be blocked.");
            } else if (e.getMessage().contains("timeout")) {
                System.err.println("Connection timed out. The server might be down or unreachable.");
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\nError: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. Clean up
            if (session != null && session.isConnected()) {
                System.out.println("\nClosing SSH connection...");
                session.disconnect();
                System.out.println("SSH connection closed.");
            }
        }
    }
}

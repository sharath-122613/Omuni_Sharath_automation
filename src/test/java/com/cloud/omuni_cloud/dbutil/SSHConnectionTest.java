package com.cloud.omuni_cloud.dbutil;

import com.jcraft.jsch.*;

/**
 * Test class to verify SSH tunnel connection
 */
public class SSHConnectionTest {
    // SSH connection details
    private static final String SSH_HOST = "10.10.10.10";
    private static final int SSH_PORT = 22;
    private static final String SSH_USERNAME = "ubuntu";
    private static final String SSH_PASSWORD = "Omni@123";
    
    // MySQL connection details
    private static final String DB_HOST = "10.10.10.10";
    private static final int DB_PORT = 3306;
    
    public static void main(String[] args) {
        JSch jsch = new JSch();
        Session session = null;
        int localPort = 0;
        
        try {
            // 1. Set up the SSH session
            System.out.println("Setting up SSH session to " + SSH_HOST + ":" + SSH_PORT);
            session = jsch.getSession(SSH_USERNAME, SSH_HOST, SSH_PORT);
            session.setPassword(SSH_PASSWORD);
            
            // Avoid asking for key confirmation
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            
            // 2. Connect to the SSH server
            System.out.println("Connecting to SSH server...");
            session.connect();
            System.out.println("✓ Connected to SSH server");
            
            // 3. Set up port forwarding
            localPort = 3307; // Local port to forward to
            System.out.println("\nSetting up port forwarding:");
            System.out.println("Local port " + localPort + " -> " + DB_HOST + ":" + DB_PORT);
            
            int assignedPort = session.setPortForwardingL(localPort, DB_HOST, DB_PORT);
            System.out.println("✓ Port forwarding active on localhost:" + assignedPort);
            
            // 4. Test the connection
            System.out.println("\nSSH tunnel is active. You can now connect to MySQL using:");
            System.out.println("Host: localhost");
            System.out.println("Port: " + localPort);
            System.out.println("Username: optimusrw");
            System.out.println("Password: [your password]");
            
            // Keep the connection open for testing
            System.out.println("\nPress ENTER to close the connection...");
            System.in.read();
            
        } catch (Exception e) {
            System.err.println("\nError: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
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

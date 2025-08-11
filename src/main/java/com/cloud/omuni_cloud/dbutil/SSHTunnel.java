package com.cloud.omuni_cloud.dbutil;

import com.jcraft.jsch.*;
import java.io.IOException;

/**
 * Utility class for creating an SSH tunnel to a MySQL database.
 */
public class SSHTunnel implements AutoCloseable {
    private JSch jsch;
    private Session session;
    private int localPort;
    private boolean isConnected = false;

    // SSH Configuration
    private final String sshHost;
    private final int sshPort;
    private final String sshUsername;
    private final String sshPassword;
    
    // MySQL Configuration
    private final String remoteDbHost;
    private final int remoteDbPort;

    /**
     * Creates a new SSHTunnel instance
     * 
     * @param sshHost SSH hostname
     * @param sshPort SSH port
     * @param sshUsername SSH username
     * @param sshPassword SSH password
     * @param remoteDbHost Remote database host
     * @param remoteDbPort Remote database port
     */
    public SSHTunnel(String sshHost, int sshPort, String sshUsername, String sshPassword,
                    String remoteDbHost, int remoteDbPort) {
        this.sshHost = sshHost;
        this.sshPort = sshPort;
        this.sshUsername = sshUsername;
        this.sshPassword = sshPassword;
        this.remoteDbHost = remoteDbHost;
        this.remoteDbPort = remoteDbPort;
        this.jsch = new JSch();
    }

    /**
     * Establishes an SSH tunnel to the remote database
     * 
     * @return The local port that forwards to the remote database
     * @throws JSchException if there's an error establishing the SSH connection
     */
    public int connect() throws JSchException {
        if (isConnected) {
            return localPort;
        }

        long startTime = System.currentTimeMillis();
        System.out.println("🔧 Setting up SSH tunnel to " + sshHost + ":" + sshPort + "...");
        
        try {
            // Set strict host key checking to no to avoid prompting
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            
            // Add connection timeout
            session = jsch.getSession(sshUsername, sshHost, sshPort);
            session.setPassword(sshPassword);
            session.setConfig(config);
            session.setTimeout(30000); // 30 seconds timeout
            
            // Enable keepalive
            session.setServerAliveInterval(60000); // 60 seconds
            
            System.out.println("🔑 Attempting to authenticate with SSH server...");
            
            // Connect to SSH server
            session.connect(30000); // 30 seconds connection timeout
            
            if (!session.isConnected()) {
                throw new JSchException("Failed to establish SSH connection: Connection timeout");
            }
            
            System.out.println("✅ SSH connection established in " + (System.currentTimeMillis() - startTime) + "ms");


            // Find an available local port
            localPort = findAvailablePort();
            System.out.println("🔌 Setting up port forwarding on local port " + localPort + "...");
            
            // Set up port forwarding with timeout
            int assignedPort = session.setPortForwardingL(localPort, remoteDbHost, remoteDbPort);
            
            isConnected = true;
            System.out.println("✅ SSH tunnel established on port " + assignedPort + " in " + 
                             (System.currentTimeMillis() - startTime) + "ms");
            
            return assignedPort;
            
        } catch (JSchException e) {
            System.err.println("❌ SSH connection failed: " + e.getMessage());
            if (e.getMessage().contains("Auth fail")) {
                System.err.println("   - Please verify your SSH username and password");
            } else if (e.getMessage().contains("Connection timed out")) {
                System.err.println("   - Could not connect to SSH server. Please check:");
                System.err.println("     1. Is the SSH hostname correct?");
                System.err.println("     2. Is the SSH port open in your firewall?");
                System.err.println("     3. Is the SSH server running on the specified port?");
            } else if (e.getMessage().contains("reject HostKey")) {
                System.err.println("   - Host key verification failed. This might be a security concern.");
            }
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error setting up SSH tunnel: " + e.getMessage());
            throw new JSchException("SSH tunnel setup failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Closes the SSH tunnel
     */
    @Override
    public void close() {
        isConnected = false;
        if (session != null && session.isConnected()) {
            System.out.println("🔌 Closing SSH tunnel on port " + localPort + "...");
            try {
                // Remove any port forwarding
                if (localPort > 0) {
                    try {
                        session.delPortForwardingL(localPort);
                        System.out.println("✅ Removed port forwarding for " + localPort);
                    } catch (Exception e) {
                        System.err.println("⚠️ Could not remove port forwarding: " + e.getMessage());
                    }
                }
                
                // Disconnect the session
                session.disconnect();
                System.out.println("✅ SSH session disconnected");
            } catch (Exception e) {
                System.err.println("⚠️ Error while closing SSH tunnel: " + e.getMessage());
            }
        }
    }
    
    /**
     * Finds a free port on localhost
     * 
     * @return A free port number
     */
    private int findAvailablePort() throws JSchException {
        // Try to find an available port, starting from 3307 and going up
        for (int port = 3307; port <= 3407; port++) {
            try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
                System.out.println("🔍 Found available port: " + port);
                return port;
            } catch (IOException e) {
                // Port is in use, try the next one
                continue;
            }
        }
        
        // If we get here, no ports were available
        throw new JSchException("Could not find an available port between 3307-3407");
    }
    
    /**
     * Checks if the SSH tunnel is connected
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected && session != null && session.isConnected();
    }
    
    /**
     * @deprecated Use close() method instead
     */
    @Deprecated
    public void disconnect() {
        close();
    }
    
    @Override
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}

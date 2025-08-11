package com.cloud.omuni_cloud.dbutil.example;

import com.cloud.omuni_cloud.dbutil.DatabaseConnection;
import com.cloud.omuni_cloud.dbutil.DatabaseManager;
import com.cloud.omuni_cloud.dbutil.SSHTunnel;
import com.cloud.omuni_cloud.dbutil.config.DatabaseConfig;
import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;
import com.jcraft.jsch.JSchException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Example class demonstrating how to use the DatabaseConnection class
 * with SSH tunneling to connect to a MySQL database and perform validations.
 */
public class DatabaseExample {
    
    // Configuration names
    private static final String DB_CONFIG_NAME = "example_db";
    
    // SSH Tunnel Configuration
    private static final String SSH_HOST = "bastion-preprod.omuni.com";
    private static final int SSH_PORT = 2040;
    private static final String SSH_USERNAME = "sharath.km";
    private static final String SSH_PASSWORD = "asdfghjkl";
    
    // Database Configuration
    private static final String DB_HOST = "oms-02-new.cnkclpncauoh.us-east-1.rds.amazonaws.com";
    private static final int DB_PORT = 3306;
    private static final String DB_NAME = ""; // Leave empty to connect without default database
    private static final String DB_USER = "optimusrw";
    private static final String DB_PASSWORD = "s4bMPxwHQU";
    
    // Database connection manager
    private static DatabaseManager dbManager;
    
    public static void main(String[] args) {
        SSHTunnel sshTunnel = null;
        
        try {
            // 1. Create and establish SSH tunnel
            System.out.println("Establishing SSH tunnel...");
            sshTunnel = new SSHTunnel(
                SSH_HOST, SSH_PORT, SSH_USERNAME, SSH_PASSWORD,
                DB_HOST, DB_PORT
            );
            
            int localPort = sshTunnel.connect();
            
            // 2. Configure the database connection
            System.out.println("Configuring database connection...");
            
            // Create a database configuration with SSH tunneling using builder pattern
            DbConnectionConfig dbConfig = new DbConnectionConfig(
                "localhost",  // Use localhost due to SSH tunnel
                String.valueOf(localPort),
                DB_NAME,
                DB_USER,
                DB_PASSWORD
            ).withSsh(
                SSH_HOST,
                SSH_PORT,
                SSH_USERNAME,
                SSH_PASSWORD,
                localPort  // Using the same port for SSH local port forwarding
            );
            
            // Register the configuration
            DatabaseConfig.addDatabaseConfig(DB_CONFIG_NAME, dbConfig);
            
            // 4. Now try to connect to a specific database
            System.out.println("\nAttempting to connect to a specific database...");
            
            // Try to connect to a specific database
            String specificDbName = "omni_oms"; // Replace with your database name
            System.out.println("Trying to connect to database: " + specificDbName);
            
            // Update the configuration with the specific database using builder pattern
            DbConnectionConfig specificDbConfig = new DbConnectionConfig(
                "localhost",
                String.valueOf(localPort),
                specificDbName,
                DB_USER,
                DB_PASSWORD
            ).withSsh(
                SSH_HOST,
                SSH_PORT,
                SSH_USERNAME,
                SSH_PASSWORD,
                localPort  // Using the same port for SSH local port forwarding
            );
            
            // Register the specific database configuration
            String specificConfigName = DB_CONFIG_NAME + "_specific";
            DatabaseConfig.addDatabaseConfig(specificConfigName, specificDbConfig);
            
            // Create a new database manager for the specific database
            try (DatabaseManager specificDbManager = new DatabaseManager(specificConfigName);
                 Connection conn = specificDbManager.getConnection()) {
                
                System.out.println("✓ Successfully connected to database: " + specificDbName);
                
                // Example: Query the database
                System.out.println("\nExample query: SELECT DATABASE()");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT DATABASE()")) {
                    if (rs.next()) {
                        System.out.println("Current database: " + rs.getString(1));
                    }
                }
                
                // Now try to list all databases using DatabaseManager
                System.out.println("\nAvailable databases (using DatabaseManager):");
                try (Connection conn2 = dbManager.getConnection();
                     Statement stmt = conn2.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                    while (rs.next()) {
                        System.out.println("- " + rs.getString(1));
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Database error occurred:");
                e.printStackTrace();
            }
            
            // 3. Test the connection
            System.out.println("Testing database connection...");
            try (Connection conn = dbManager.getConnection()) {
                System.out.println("✓ Successfully connected to MySQL server!");
                
                // List available databases
                System.out.println("\nAvailable databases:");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
                    while (rs.next()) {
                        System.out.println("- " + rs.getString(1));
                    }
                }
                
                // List available databases
                System.out.println("\nAttempting to list available databases...");
                try (java.sql.Statement stmt = dbManager.getConnection().createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SHOW DATABASES;")) {
                    
                    System.out.println("\nAvailable databases:");
                    int dbCount = 0;
                    while (rs.next()) {
                        String dbName = rs.getString(1);
                        System.out.println("-" + dbName);
                        dbCount++;
                    }
                    System.out.println("\nFound " + dbCount + " databases.");
                    
                    // If no databases were found, the user might not have permission to list databases
                    if (dbCount == 0) {
                        System.out.println("\nNote: No databases listed. This could mean:");
                        System.out.println("1. The user doesn't have permission to list databases");
                        System.out.println("2. No databases exist on the server");
                        System.out.println("3. The user can only access specific databases");
                    }
                }
                
                // If a specific database was provided, try to connect to it
                if (!DB_NAME.isEmpty()) {
                    System.out.println("\nAttempting to connect to database: " + DB_NAME);
                    try {
                        dbManager.close(); // Close the previous connection
                        dbManager = new DatabaseManager(DB_CONFIG_NAME);
                        System.out.println("✓ Successfully connected to database: " + DB_NAME);
                    } catch (SQLException e) {
                        System.err.println("\nFailed to connect to database '" + DB_NAME + "': " + e.getMessage());
                        System.err.println("Please verify the database name and that the user has access to it.");
                        throw e;
                    }
                }
                
            } catch (SQLException e) {
                if (e.getMessage().contains("Access denied")) {
                    System.err.println("\nAccess denied. Please verify the following:");
                    System.err.println("1. Username and password are correct");
                    System.err.println("2. The user has the correct permissions");
                    System.err.println("3. The user is allowed to connect from your IP address");
                    System.err.println("4. The database name is correct (if specified)");
                } else {
                    System.err.println("\nError: " + e.getMessage());
                }
                throw e;
            }
            
            // 3. Only proceed with table operations if we have a valid database connection
            if (!DB_NAME.isEmpty()) {
                try {
                    System.out.println("\nTesting database operations...");
                    
                    // Example 1: List tables in the database
                    System.out.println("\nAvailable tables in database " + DB_NAME + ":");
                    try (java.sql.Statement stmt = dbManager.getConnection().createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SHOW TABLES;")) {
                        while (rs.next()) {
                            System.out.println("- " + rs.getString(1));
                        }
                    }
                    
                    // Example 2: Execute a simple query
                    System.out.println("\nTesting a simple query...");
                    try (java.sql.Statement stmt = dbManager.getConnection().createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
                        if (rs.next()) {
                            System.out.println("MySQL Version: " + rs.getString(1));
                        }
                    }
                    
                } catch (SQLException e) {
                    System.err.println("\nError executing database operations: " + e.getMessage());
                    System.err.println("This might be due to insufficient permissions or the database might be empty.");
                }
            } else {
                System.out.println("\nNo specific database selected. Use DB_NAME to specify a database for table operations.");
            }
            
        } catch (JSchException e) {
            System.err.println("SSH Tunnel error: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database error occurred:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. Clean up resources
            try {
                if (dbManager != null) {
                    dbManager.close();
                    System.out.println("Database manager closed.");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database manager: " + e.getMessage());
            }
            
            if (sshTunnel != null) {
                sshTunnel.disconnect();
                System.out.println("SSH tunnel disconnected.");
            }
        }
    }
}

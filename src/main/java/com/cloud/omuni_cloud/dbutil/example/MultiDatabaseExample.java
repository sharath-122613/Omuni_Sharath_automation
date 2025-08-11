package com.cloud.omuni_cloud.dbutil.example;

import com.cloud.omuni_cloud.dbutil.DatabaseManager;
import com.cloud.omuni_cloud.dbutil.config.DatabaseConfig;
import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Example class demonstrating how to use the DatabaseManager with multiple database connections.
 */
public class MultiDatabaseExample {
    // Configuration names
    private static final String PRIMARY_DB = "primary";
    private static final String SECONDARY_DB = "secondary";

    public static void main(String[] args) {
        // Initialize database configurations
        initializeDatabaseConfigs();

        // Example 1: Query from primary database
        queryFromPrimaryDatabase();

        // Example 2: Query from secondary database
        queryFromSecondaryDatabase();

        // Example 3: Transaction across multiple databases
        try {
            transferDataBetweenDatabases();
        } catch (Exception e) {
            System.err.println("Error during data transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize database configurations with connection details
     */
    private static void initializeDatabaseConfigs() {
        // Primary database configuration (with SSH)
        DbConnectionConfig primaryConfig = new DbConnectionConfig(
            "10.10.10.10",  // host
            "3306",         // port
            "primary_db",   // database name
            "dbuser1",      // username
            "dbpass1"       // password
        ).withSsh(
            "ssh.example.com",  // SSH host
            22,                 // SSH port
            "sshuser",          // SSH username
            "sshpass",          // SSH password
            3307                // Local port for SSH tunnel
        );

        // Secondary database configuration (direct connection)
        DbConnectionConfig secondaryConfig = new DbConnectionConfig(
            "10.20.20.20",  // host
            "3306",         // port
            "secondary_db", // database name
            "dbuser2",      // username
            "dbpass2"       // password
        );

        // Add configurations to DatabaseConfig
        DatabaseConfig.addDatabaseConfig(PRIMARY_DB, primaryConfig);
        DatabaseConfig.addDatabaseConfig(SECONDARY_DB, secondaryConfig);
    }

    /**
     * Example: Query data from the primary database
     */
    private static void queryFromPrimaryDatabase() {
        System.out.println("\n=== Querying from Primary Database ===");
        
        try (DatabaseManager dbManager = new DatabaseManager(PRIMARY_DB)) {
            // Example query with parameters
            String query = "SELECT id, name, email FROM users WHERE status = ? LIMIT 5";
            List<Map<String, Object>> results = dbManager.executeQuery(query, "active");
            
            // Process and display results
            System.out.println("Active users:");
            for (Map<String, Object> row : results) {
                System.out.printf("ID: %s, Name: %s, Email: %s%n",
                    row.get("id"), row.get("name"), row.get("email"));
            }
            
            // Example: Check if a specific record exists
            boolean exists = dbManager.recordExists("users", 
                Map.of("email", "user@example.com"));
            System.out.println("User exists: " + exists);
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example: Query data from the secondary database
     */
    private static void queryFromSecondaryDatabase() {
        System.out.println("\n=== Querying from Secondary Database ===");
        
        try (DatabaseManager dbManager = new DatabaseManager(SECONDARY_DB)) {
            // Example query
            String query = "SELECT product_id, product_name, price FROM products ORDER BY price DESC LIMIT 3";
            List<Map<String, Object>> results = dbManager.executeQuery(query);
            
            // Process and display results
            System.out.println("Top 3 most expensive products:");
            for (Map<String, Object> row : results) {
                System.out.printf("ID: %s, Name: %s, Price: $%.2f%n",
                    row.get("product_id"), 
                    row.get("product_name"), 
                    Double.parseDouble(row.get("price").toString()));
            }
            
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Example: Transfer data between databases in a transaction
     */
    private static void transferDataBetweenDatabases() throws Exception {
        System.out.println("\n=== Transferring Data Between Databases ===");
        
        try (DatabaseManager sourceDb = new DatabaseManager(PRIMARY_DB);
             DatabaseManager targetDb = new DatabaseManager(SECONDARY_DB)) {
            
            // Start transaction (simplified - in a real app, you'd use proper transaction management)
            System.out.println("Starting data transfer...");
            
            // Example: Get data from source
            String selectQuery = "SELECT id, name, email FROM users WHERE last_login < DATE_SUB(NOW(), INTERVAL 6 MONTH)";
            List<Map<String, Object>> inactiveUsers = sourceDb.executeQuery(selectQuery);
            
            if (inactiveUsers.isEmpty()) {
                System.out.println("No inactive users found to transfer.");
                return;
            }
            
            System.out.println("Found " + inactiveUsers.size() + " inactive users to archive.");
            
            // Example: Insert into target
            String insertQuery = "INSERT INTO archived_users (user_id, username, email, archived_date) VALUES (?, ?, ?, NOW())";
            int count = 0;
            
            for (Map<String, Object> user : inactiveUsers) {
                targetDb.executeUpdate(insertQuery,
                    user.get("id"),
                    user.get("name"),
                    user.get("email")
                );
                count++;
            }
            
            System.out.println("Successfully archived " + count + " users.");
            
        } catch (SQLException e) {
            System.err.println("Error during data transfer: " + e.getMessage());
            throw e;
        }
    }
}

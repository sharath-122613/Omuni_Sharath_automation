package com.cloud.omuni_cloud.dbutil.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class to manage multiple database connection settings
 */
public class DatabaseConfig {
    private static final Map<String, DbConnectionConfig> configs = new HashMap<>();
    
    // Initialize with database configurations from environment variables
    static {
        // Nickfury database configuration with SSH tunneling
        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "3306";
        String dbName = System.getenv("DB_NAME");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        
        String sshHost = System.getenv("SSH_HOST");
        int sshPort = System.getenv("SSH_PORT") != null ? Integer.parseInt(System.getenv("SSH_PORT")) : 22;
        String sshUser = System.getenv("SSH_USER");
        String sshPassword = System.getenv("SSH_PASSWORD");
        int sshLocalPort = System.getenv("SSH_LOCAL_PORT") != null ? 
            Integer.parseInt(System.getenv("SSH_LOCAL_PORT")) : 3307;
            
        if (dbHost != null && dbName != null && dbUser != null && dbPassword != null &&
            sshHost != null && sshUser != null && sshPassword != null) {
                
            addDatabaseConfig("nickfury", new DbConnectionConfig(
                dbHost,      // MySQL hostname
                dbPort,      // MySQL port
                dbName,      // Database name
                dbUser,      // MySQL username
                dbPassword   // MySQL password
            ).withSsh(
                sshHost,     // SSH hostname
                sshPort,     // SSH port
                sshUser,     // SSH username
                sshPassword, // SSH password
                sshLocalPort // Local port for SSH tunnel
            ));
        } else {
            System.err.println("Warning: Database configuration not fully set via environment variables");
        }
    }
    
    /**
     * Add or update a database configuration
     * @param name Unique name for this database configuration
     * @param config Database connection configuration
     */
    public static void addDatabaseConfig(String name, DbConnectionConfig config) {
        configs.put(name, config);
    }
    
    /**
     * Get a database configuration by name
     * @param name Name of the database configuration
     * @return Database connection configuration
     * @throws IllegalArgumentException if the configuration doesn't exist
     */
    public static DbConnectionConfig getDatabaseConfig(String name) {
        if (!configs.containsKey(name)) {
            throw new IllegalArgumentException("No database configuration found for: " + name);
        }
        return configs.get(name);
    }
    
    /**
     * Get all database configurations
     * @return Map of all database configurations
     */
    public static Map<String, DbConnectionConfig> getAllConfigs() {
        return new HashMap<>(configs);
    }
    
    /**
     * Clears all database configurations.
     * This is primarily intended for testing purposes.
     */
    public static void clearDatabaseConfigs() {
        configs.clear();
    }
}

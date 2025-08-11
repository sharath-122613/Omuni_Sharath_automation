package com.cloud.omuni_cloud.dbutil;

import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for managing multiple MySQL database connections with connection pooling and SSH tunneling.
 */
public class DatabaseConnection {
    private static final String JDBC_URL = "jdbc:mysql://%s:%s/%s";
    
    // Connection pool map (connection name -> HikariDataSource)
    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    
    // SSH sessions map (connection name -> Session)
    private final Map<String, Session> sshSessions = new ConcurrentHashMap<>();
    
    // Singleton instance
    private static volatile DatabaseConnection instance;
    
    // Default connection pool settings
    private static final int MAX_POOL_SIZE = 10;
    private static final int MIN_IDLE = 2;
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    private static final long IDLE_TIMEOUT = TimeUnit.MINUTES.toMillis(10);
    private static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30);
    
    static {
        // Explicitly load the MySQL JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Make sure it's in the classpath.", e);
        }
    }
    
    // Private constructor to prevent instantiation
    private DatabaseConnection() {
        // Initialize the connection pool
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeAllConnections));
    }
    
    /**
     * Gets the singleton instance of DatabaseConnection
     * @return DatabaseConnection instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }
    
    /**
     * Gets a database connection for the specified configuration name
     * @param configName Name of the database configuration to use
     * @return A database connection from the pool
     * @throws SQLException if connection fails
     */
    public Connection getConnection(String configName) throws SQLException {
        // Get or create a data source for this configuration
        HikariDataSource dataSource = dataSources.computeIfAbsent(configName, k -> {
            try {
                return createDataSource(k);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create data source for " + k, e);
            }
        });
        
        // Get a connection from the pool
        return dataSource.getConnection();
    }
    
    /**
     * Sets up an SSH tunnel for the database connection
     * @param configName Name of the configuration
     * @param config Database configuration
     * @return The local port the tunnel is using
     * @throws JSchException if SSH tunnel setup fails
     */
    private int setupSshTunnel(String configName, DbConnectionConfig config) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(config.getSshUsername(), config.getSshHost(), config.getSshPort());
        session.setPassword(config.getSshPassword());
        
        // Avoid asking for key confirmation
        java.util.Properties sshConfig = new java.util.Properties();
        sshConfig.put("StrictHostKeyChecking", "no");
        session.setConfig(sshConfig);
        
        // Connect to the SSH server
        session.connect();
        
        // Set up port forwarding
        int localPort = config.getSshLocalPort();
        session.setPortForwardingL(localPort, config.getHost(), Integer.parseInt(config.getPort()));
        
        // Store the session for later cleanup
        sshSessions.put(configName, session);
        
        System.out.println("SSH tunnel established on local port: " + localPort);
        return localPort;
    }
    
    /**
     * Creates a HikariCP data source for the specified configuration
     * @param configName Name of the database configuration
     * @return Configured Hikari data source
     */
    private HikariDataSource createDataSource(String configName) throws Exception {
        // Get the database configuration
        DbConnectionConfig config = com.cloud.omuni_cloud.dbutil.config.DatabaseConfig.getDatabaseConfig(configName);
        return createDataSource(config);
    }
    
    /**
     * Creates a HikariCP data source using the provided configuration
     * @param config The database configuration
     * @return Configured Hikari data source
     * @throws Exception if data source creation fails
     */
    private HikariDataSource createDataSource(DbConnectionConfig config) throws Exception {
        // Create a unique identifier for this connection
        String configId = config.getHost() + "_" + config.getPort() + "_" + config.getDatabase();
            
        // If SSH is required, set up the tunnel first
        if (config.useSsh()) {
            int localPort = setupSshTunnel(configId, config);
            // Create a new config for the tunneled connection
            config = new DbConnectionConfig(
                "localhost", 
                String.valueOf(localPort), 
                config.getDatabase(), 
                config.getUsername(), 
                config.getPassword()
            ).withSsh(
                config.getSshHost(),
                config.getSshPort(),
                config.getSshUsername(),
                config.getSshPassword(),
                localPort
            );
        }
        
        // Configure HikariCP
        HikariConfig hikariConfig = new HikariConfig();
        
        // JDBC URL with timezone settings for MySQL 8+
        String jdbcUrl = String.format(JDBC_URL + "?useSSL=false&serverTimezone=UTC&useLegacyDatetimeCode=false", 
            config.getHost(), 
            config.getPort(), 
            config.getDatabase() != null ? config.getDatabase() : "");
        
        System.out.println("Connecting to database URL: " + jdbcUrl.replace(config.getPassword(), "*****"));
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // Credentials
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        
        // Pool configuration
        hikariConfig.setMaximumPoolSize(MAX_POOL_SIZE);
        hikariConfig.setMinimumIdle(MIN_IDLE);
        hikariConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        hikariConfig.setIdleTimeout(IDLE_TIMEOUT);
        hikariConfig.setMaxLifetime(MAX_LIFETIME);
        
        // Connection properties
        Properties props = new Properties();
        props.setProperty("useSSL", "false");
        props.setProperty("autoReconnect", "true");
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("serverTimezone", "UTC");
        props.setProperty("verifyServerCertificate", "false");
        props.setProperty("allowMultiQueries", "true");
        props.setProperty("useLocalSessionState", "true");
        props.setProperty("useLocalTransactionState", "true");
        props.setProperty("useServerPrepStmts", "true");
        props.setProperty("cachePrepStmts", "true");
        props.setProperty("prepStmtCacheSize", "250");
        props.setProperty("prepStmtCacheSqlLimit", "2048");
        
        hikariConfig.setDataSourceProperties(props);
        
        // Connection test query
        hikariConfig.setConnectionTestQuery("SELECT 1");
        
        // Set pool name for better monitoring
        String poolName = "HikariPool-" + config.getHost() + "-" + 
            (config.getDatabase() != null ? config.getDatabase() : "default") + 
            "-" + System.currentTimeMillis();
        hikariConfig.setPoolName(poolName);
        
        // Log pool creation
        System.out.println("🔌 Creating connection pool " + poolName + 
                         " for database: " + config.getDatabase() + 
                         " at " + config.getHost() + ":" + config.getPort());
        
        try {
            return new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            System.err.println("Failed to create Hikari data source: " + e.getMessage());
            throw new SQLException("Failed to create connection pool: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a direct connection using the provided parameters
     * @param host Database host
     * @param port Database port
     * @param database Database name (can be empty for server connection)
     * @param username Database username
     * @param password Database password
     * @return A new database connection
     * @throws SQLException if connection fails
     * @deprecated Use getConnection(String) with DatabaseConfig instead
     */
    @Deprecated
    public Connection connect(String host, String port, String database, String username, String password) 
            throws SQLException {
        
        // Create a temporary configuration name
        String tempConfigName = "temp_" + System.currentTimeMillis();
        
        try {
            // Create a temporary configuration
            DbConnectionConfig config = new DbConnectionConfig(host, port, database, username, password);
            
            // Create a data source for this connection
            HikariDataSource dataSource = createDataSource(config);
            dataSources.put(tempConfigName, dataSource);
            
            // Get and return a connection
            return dataSource.getConnection();
            
        } catch (Exception e) {
            // Clean up if there was an error
            close(tempConfigName);
            throw new SQLException("Failed to create direct connection: " + e.getMessage(), e);
        }
    }
    
    /**
     * Closes the database connection for the specified configuration
     * @param configName Name of the database configuration
     * @throws SQLException if an error occurs while closing the connection
     */
    public void close(String configName) throws SQLException {
        if (configName == null) {
            throw new IllegalArgumentException("Configuration name cannot be null");
        }
        
        try {
            // Close data source if it exists
            if (dataSources.containsKey(configName)) {
                HikariDataSource dataSource = dataSources.get(configName);
                if (dataSource != null && !dataSource.isClosed()) {
                    try {
                        dataSource.close();
                        System.out.println(" Closed connection pool for " + configName);
                    } catch (Exception e) {
                        System.err.println("Error closing data source " + configName + ": " + e.getMessage());
                    }
                }
                dataSources.remove(configName);
            }
            
            // Close SSH session if it exists
            if (sshSessions.containsKey(configName)) {
                Session session = sshSessions.get(configName);
                if (session != null && session.isConnected()) {
                    try {
                        session.disconnect();
                        System.out.println(" Closed SSH tunnel for " + configName);
                    } catch (Exception e) {
                        System.err.println("Error disconnecting SSH session " + configName + ": " + e.getMessage());
                    }
                }
                sshSessions.remove(configName);
            }
        } catch (Exception e) {
            throw new SQLException("Error closing connection for " + configName, e);
        }
    }
    
    /**
     * Closes all database connections and SSH sessions
     */
    public void closeAllConnections() {
        // Close all data sources
        for (Map.Entry<String, HikariDataSource> entry : dataSources.entrySet()) {
            try {
                if (entry.getValue() != null && !entry.getValue().isClosed()) {
                    entry.getValue().close();
                    System.out.println(" Closed connection pool for " + entry.getKey());
                }
            } catch (Exception e) {
                System.err.println(" Error closing data source for " + entry.getKey() + ": " + e.getMessage());
            }
        }
        dataSources.clear();
        
        // Close all SSH sessions
        for (Map.Entry<String, Session> entry : sshSessions.entrySet()) {
            if (entry.getValue() != null && entry.getValue().isConnected()) {
                entry.getValue().disconnect();
                System.out.println(" Closed SSH tunnel for " + entry.getKey());
            }
        }
        sshSessions.clear();
        
        System.out.println("All database connections and SSH sessions have been closed.");
    }
    
    /**
     * Validates if a value exists in the specified table and column
     * @param configName Name of the database configuration to use
     * @param tableName Name of the table to query
     * @param columnName Name of the column to check
     * @param value Value to validate
     * @return true if value exists, false otherwise
     * @throws SQLException if query execution fails
     * @throws IllegalStateException if not connected to database
     */
    public boolean validateValueExists(String configName, String tableName, String columnName, String value) 
            throws SQLException, IllegalStateException {
        
        if (tableName == null || columnName == null) {
            throw new IllegalArgumentException("Table name and column name must not be null");
        }
        
        String sql = String.format("SELECT 1 FROM %s WHERE %s = ? LIMIT 1",
            sanitizeIdentifier(tableName),
            sanitizeIdentifier(columnName));
            
        try (Connection conn = getConnection(configName);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new SQLException("Error validating value existence: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sanitizes an SQL identifier (table name, column name) to prevent SQL injection
     * @param identifier The identifier to sanitize
     * @return The sanitized identifier
     */
    private String sanitizeIdentifier(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier cannot be null");
        }
        // Only allow alphanumeric characters and underscores
        if (!identifier.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid identifier: " + identifier);
        }
        return identifier;
    }
    
    /**
     * Validates if a row exists based on multiple conditions
     * @param configName Name of the database configuration to use
     * @param tableName Name of the table to query
     * @param conditions Array of condition strings (e.g., ["column1 = ?", "column2 > ?"])
     * @param params Array of parameter values corresponding to the conditions
     * @return true if row exists, false otherwise
     * @throws SQLException if query execution fails
     * @throws IllegalStateException if not connected to database
     * @throws IllegalArgumentException if parameters are invalid
     */
    public boolean validateRowExists(String configName, String tableName, String[] conditions, Object[] params) 
            throws SQLException, IllegalStateException {
        
        if (configName == null || configName.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration name cannot be null or empty");
        }
        
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        
        if (conditions == null || conditions.length == 0) {
            throw new IllegalArgumentException("At least one condition must be provided");
        }
        
        if (params == null || params.length != conditions.length) {
            throw new IllegalArgumentException("Number of parameters must match number of conditions");
        }
        
        // Sanitize all conditions to prevent SQL injection
        for (String condition : conditions) {
            if (condition == null || condition.trim().isEmpty()) {
                throw new IllegalArgumentException("Condition cannot be null or empty");
            }
            // Basic check for SQL injection attempts in conditions
            if (condition.toUpperCase().contains("DELETE ") || 
                condition.toUpperCase().contains("UPDATE ") ||
                condition.toUpperCase().contains("INSERT ") ||
                condition.toUpperCase().contains("DROP ") ||
                condition.toUpperCase().contains("--")) {
                throw new IllegalArgumentException("Invalid condition: " + condition);
            }
        }
        
        // Build the SQL query
        StringBuilder sql = new StringBuilder("SELECT 1 FROM ")
            .append(sanitizeIdentifier(tableName))
            .append(" WHERE ")
            .append(String.join(" AND ", conditions))
            .append(" LIMIT 1");
            
        try (Connection conn = getConnection(configName);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Set parameters with proper type handling
            for (int i = 0; i < params.length; i++) {
                if (params[i] == null) {
                    stmt.setNull(i + 1, Types.VARCHAR);
                } else if (params[i] instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) params[i]);
                } else if (params[i] instanceof Long) {
                    stmt.setLong(i + 1, (Long) params[i]);
                } else if (params[i] instanceof Boolean) {
                    stmt.setBoolean(i + 1, (Boolean) params[i]);
                } else if (params[i] instanceof java.util.Date) {
                    stmt.setTimestamp(i + 1, new java.sql.Timestamp(((java.util.Date) params[i]).getTime()));
                } else if (params[i] instanceof java.sql.Date) {
                    stmt.setDate(i + 1, (java.sql.Date) params[i]);
                } else if (params[i] instanceof String) {
                    stmt.setString(i + 1, (String) params[i]);
                } else {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new SQLException("Error validating row existence: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the data sources map for testing purposes
     * @return the data sources map
     */
    Map<String, HikariDataSource> getDataSourcesForTesting() {
        return new HashMap<>(dataSources);
    }
    
    /**
     * Gets the SSH sessions map for testing purposes
     * @return the SSH sessions map
     */
    Map<String, Session> getSshSessionsForTesting() {
        return new HashMap<>(sshSessions);
    }
}

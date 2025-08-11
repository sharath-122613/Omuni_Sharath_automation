package com.cloud.omuni_cloud.dbutil;

import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class to simplify common database operations using DatabaseConnection.
 * Provides a higher-level interface for executing queries and updates.
 */
public class DatabaseManager implements AutoCloseable {
    private static final DatabaseConnection dbConnection = DatabaseConnection.getInstance();
    private final String configName;
    private boolean connectionClosed = false;

    /**
     * Creates a new DatabaseManager for the specified configuration
     * @param configName Name of the database configuration to use
     */
    public DatabaseManager(String configName) {
        this.configName = configName;
    }

    /**
     * Executes a SELECT query and returns the results as a list of maps
     * @param query The SQL query to execute
     * @param params Optional query parameters
     * @return List of maps representing the result set rows
     * @throws SQLException if a database access error occurs
     */
    public List<Map<String, Object>> executeQuery(String query, Object... params) throws SQLException {
        if (connectionClosed) {
            throw new SQLException("DatabaseManager has been closed. Create a new instance to execute queries.");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection(configName);
            stmt = conn.prepareStatement(query);
            
            // Set parameters if any
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            
            // Execute query and process results
            rs = stmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
            
            return results;
            
        } catch (SQLException e) {
            throw new SQLException("Error executing query: " + e.getMessage(), e);
        } finally {
            // Close resources in reverse order
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            // Note: Connection is managed by the connection pool, so we don't close it here
        }
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE statement
     * @param query The SQL statement to execute
     * @param params Optional statement parameters
     * @return The number of rows affected
     * @throws SQLException if a database access error occurs
     */
    public int executeUpdate(String query, Object... params) throws SQLException {
        if (connectionClosed) {
            throw new SQLException("DatabaseManager has been closed. Create a new instance to execute updates.");
        }
        
        try (Connection conn = dbConnection.getConnection(configName);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            // Set parameters if any
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new SQLException("Error executing update: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a record exists in the specified table matching the given conditions
     * @param tableName Name of the table to check
     * @param conditions Column-value pairs for the WHERE clause
     * @return true if a matching record exists, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean recordExists(String tableName, Map<String, Object> conditions) throws SQLException {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        
        if (conditions == null || conditions.isEmpty()) {
            throw new IllegalArgumentException("At least one condition must be provided");
        }
        
        StringBuilder query = new StringBuilder("SELECT 1 FROM " + tableName + " WHERE ");
        List<Object> params = new ArrayList<>();
        
        int i = 0;
        for (Map.Entry<String, Object> entry : conditions.entrySet()) {
            if (i++ > 0) {
                query.append(" AND ");
            }
            query.append(entry.getKey()).append(" = ?");
            params.add(entry.getValue());
        }
        query.append(" LIMIT 1");
        
        List<Map<String, Object>> results = executeQuery(query.toString(), params.toArray());
        return !results.isEmpty();
    }

    /**
     * Gets a single value from the database
     * @param query The SQL query to execute
     * @param params Optional query parameters
     * @return The value of the first column of the first row, or null if no results
     * @throws SQLException if a database access error occurs
     */
    public Object getSingleValue(String query, Object... params) throws SQLException {
        List<Map<String, Object>> results = executeQuery(query, params);
        if (results.isEmpty() || results.get(0).isEmpty()) {
            return null;
        }
        return results.get(0).values().iterator().next();
    }

    /**
     * Closes the database connection for this manager
     * @throws SQLException if an error occurs while closing the connection
     */
    @Override
    public void close() throws SQLException {
        if (!connectionClosed) {
            try {
                // Note: We don't close the connection here as it's managed by the connection pool
                connectionClosed = true;
            } catch (Exception e) {
                throw new SQLException("Error closing database manager", e);
            }
        }
    }
    
    /**
     * Gets the underlying database connection
     * @return the database connection
     * @throws SQLException if a database access error occurs
     * @throws ClassNotFoundException if the database driver is not found
     */
    public Connection getConnection() throws SQLException {
        if (connectionClosed) {
            throw new SQLException("DatabaseManager has been closed. Create a new instance to get a connection.");
        }
        try {
            return dbConnection.getConnection(configName);
        } catch (Exception e) {
            throw new SQLException("Failed to get database connection", e);
        }
    }
    
    /**
     * Verifies a sale call in Chandler database for Bata orders
     * @param orderId The order ID to verify
     * @return The value from the 4th column (index 3) of the matching record
     * @throws SQLException if a database access error occurs
     */
    public String verifySaleCallInChandlerDBforBataOrders(String orderId) throws SQLException {
        try {
            // Using hardcoded column name 'orderNo' as specified
            String query = "SELECT * FROM nickfury.sale_orders WHERE orderNo = ? ORDER BY id DESC";
            List<Map<String, Object>> results = executeQuery(query, orderId);
            
            if (results.isEmpty()) {
                return "[WARNING] No sale order found with orderNo: " + orderId;
            }
            
            // Get the first result and format the output
            Map<String, Object> result = results.get(0);
            return String.format("Sale order found - ID: %s, Status: %s, Total: %s", 
                result.get("id"), 
                result.get("status") != null ? result.get("status") : "N/A",
                result.getOrDefault("total", result.get("amount") != null ? result.get("amount") : "N/A"));
                
        } catch (SQLException e) {
            return "[ERROR] Failed to verify sale order: " + e.getMessage().split("\n")[0];
        }
    }
    
    /**
     * Verifies a booking call in Chandler database for Bata orders
     * @param orderId The order ID to verify
     * @return The value from the 8th column (index 7) of the matching record
     * @throws SQLException if a database access error occurs
     */
    public String verifyBookingCallInChandlerDBforBataOrders(String orderId) throws SQLException {
        try {
            // Using hardcoded column name 'orderId' as specified
            String query = "SELECT * FROM nickfury.store_orders WHERE orderId = ? ORDER BY id DESC";
            List<Map<String, Object>> results = executeQuery(query, orderId);
            
            if (results.isEmpty()) {
                return "[WARNING] No store order found with orderId: " + orderId;
            }
            
            // Get the first result and format the output
            Map<String, Object> result = results.get(0);
            return String.format("Store order found - ID: %s, Status: %s", 
                result.get("id"), 
                result.get("status") != null ? result.get("status") : "N/A");
                
        } catch (SQLException e) {
            return "[ERROR] Failed to verify store order: " + e.getMessage().split("\n")[0];
        }
    }
}

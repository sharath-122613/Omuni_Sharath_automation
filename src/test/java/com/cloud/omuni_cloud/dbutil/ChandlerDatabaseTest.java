package com.cloud.omuni_cloud.dbutil;

import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify Chandler database connection and basic operations.
 */
public class ChandlerDatabaseTest {
    
    private static DatabaseConnection dbConnection;
    private static final String CONFIG_NAME = "chandler";
    
    @BeforeAll
    public static void setup() {
        // Initialize the database connection
        dbConnection = DatabaseConnection.getInstance();
    }
    
    @Test
    public void testDatabaseConnection() {
        try (Connection conn = dbConnection.getConnection(CONFIG_NAME);
             Statement stmt = conn.createStatement()) {
            
            // Test if connection is valid
            assertFalse(conn.isClosed(), "Connection should be open");
            
            // Test a simple query
            try (ResultSet rs = stmt.executeQuery("SELECT 1")) {
                assertTrue(rs.next(), "Should have at least one row");
                assertEquals(1, rs.getInt(1), "Should return 1");
            }
            
            System.out.println("✅ Successfully connected to Chandler database");
            
        } catch (Exception e) {
            fail("Failed to connect to Chandler database: " + e.getMessage());
        }
    }
    
    @Test
    public void testListTables() {
        try (DatabaseManager dbManager = new DatabaseManager(CONFIG_NAME)) {
            // Get list of tables in the database
            List<Map<String, Object>> tables = dbManager.executeQuery(
                "SHOW TABLES"
            );
            
            assertNotNull(tables, "Should return list of tables");
            
            // Print tables
            System.out.println("\n📋 Tables in Chandler database:");
            for (Map<String, Object> table : tables) {
                System.out.println("- " + table.values().iterator().next());
            }
            
        } catch (Exception e) {
            fail("Failed to list tables: " + e.getMessage());
        }
    }
    
    @Test
    public void testSampleQuery() {
        try (DatabaseManager dbManager = new DatabaseManager(CONFIG_NAME)) {
            // Example: Get count of records in a common table (adjust table name as needed)
            List<Map<String, Object>> results = dbManager.executeQuery(
                "SELECT 'users' as table_name, COUNT(*) as count FROM users"
            );
            
            if (!results.isEmpty()) {
                Map<String, Object> row = results.get(0);
                System.out.println("\n📊 Sample query results:");
                System.out.println("Table: " + row.get("table_name"));
                System.out.println("Record count: " + row.get("count"));
            } else {
                System.out.println("No results returned from sample query");
            }
            
        } catch (Exception e) {
            System.err.println("Sample query failed (this might be expected if table doesn't exist): " + e.getMessage());
        }
    }
    
    @AfterAll
    public static void cleanup() {
        try {
            // Close all connections when done
            dbConnection.close(CONFIG_NAME);
            System.out.println("\n✅ Chandler database tests completed");
        } catch (SQLException e) {
            System.err.println("Error cleaning up connections: " + e.getMessage());
        }
    }
}

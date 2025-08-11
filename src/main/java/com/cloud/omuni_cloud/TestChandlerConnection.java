package com.cloud.omuni_cloud;

import com.cloud.omuni_cloud.dbutil.DatabaseConnection;
import com.cloud.omuni_cloud.dbutil.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Simple test class to verify Chandler database connection
 */
public class TestChandlerConnection {
    private static final String CONFIG_NAME = "nickfury";
    
    public static void main(String[] args) {
        System.out.println("🔌 Testing Chandler database connection...");
        
        try {
            // 1. Test basic connection
            testBasicConnection();
            
            // 2. List tables
            listTables();
            
            // 3. Run a sample query
            runSampleQuery();
            
            System.out.println("\n✅ All tests completed successfully!");
            
        } catch (Exception e) {
            System.err.println("\n❌ Error during database test: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up
            try {
                DatabaseConnection.getInstance().close(CONFIG_NAME);
            } catch (Exception e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    private static void testBasicConnection() throws Exception {
        System.out.println("\n1. Testing basic connection...");
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection(CONFIG_NAME);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("✅ Successfully connected to Chandler database");
            
            // Test a simple query
            try (ResultSet rs = stmt.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    System.out.println("✅ Basic query test passed");
                } else {
                    System.out.println("❌ Basic query test failed - no results");
                }
            }
        }
    }
    
    private static void listTables() {
        System.out.println("\n2. Listing database tables...");
        
        try (DatabaseManager dbManager = new DatabaseManager(CONFIG_NAME)) {
            List<Map<String, Object>> tables = dbManager.executeQuery(
                "SHOW TABLES"
            );
            
            System.out.println("✅ Found " + tables.size() + " tables");
            
            if (!tables.isEmpty()) {
                System.out.println("\n📋 Tables in Chandler database:");
                for (int i = 0; i < Math.min(10, tables.size()); i++) {
                    System.out.println("- " + tables.get(i).values().iterator().next());
                }
                if (tables.size() > 10) {
                    System.out.println("... and " + (tables.size() - 10) + " more");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error listing tables: " + e.getMessage());
        }
    }
    
    private static void runSampleQuery() {
        System.out.println("\n3. Running sample queries...");
        
        try (DatabaseManager dbManager = new DatabaseManager(CONFIG_NAME)) {
            // Try to get database version
            List<Map<String, Object>> version = dbManager.executeQuery(
                "SELECT VERSION() as version"
            );
            if (!version.isEmpty()) {
                System.out.println("✅ Database version: " + version.get(0).get("version"));
            }
            
            // Try to get user count (if users table exists)
            try {
                List<Map<String, Object>> userCount = dbManager.executeQuery(
                    "SELECT COUNT(*) as count FROM users"
                );
                if (!userCount.isEmpty()) {
                    System.out.println("✅ Total users: " + userCount.get(0).get("count"));
                }
            } catch (Exception e) {
                System.out.println("ℹ️ Could not query users table: " + e.getMessage().split("\n")[0]);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error running sample queries: " + e.getMessage());
        }
    }
}

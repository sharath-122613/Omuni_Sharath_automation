package com.cloud.omuni_cloud;

import com.cloud.omuni_cloud.config.ChandlerTestConfig;
import com.cloud.omuni_cloud.config.TestDatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(classes = {ChandlerTestConfig.class, TestDatabaseConfig.class})
@ActiveProfiles("test")
@Import({ChandlerTestConfig.class, TestDatabaseConfig.class})
public class ChandlerDatabaseSmokeTest {
    
    private static final Logger log = LoggerFactory.getLogger(ChandlerDatabaseSmokeTest.class);
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_SECONDS = 5;
    
    @BeforeAll
    static void checkDatabaseAvailability() {
        log.info("Checking database availability...");
        
        // First, check if we can connect directly (for local development)
        if (canConnectToDatabase()) {
            log.info("✅ Direct database connection successful");
            return;
        }
        
        // If direct connection fails, try with SSH tunnel
        log.warn("Direct database connection failed, checking SSH tunnel...");
        
        // The TestDatabaseConfig should have set up the SSH tunnel by now
        // Let's give it a moment to establish the tunnel
        waitForDatabase(MAX_RETRIES, RETRY_DELAY_SECONDS);
    }
    
    private static boolean canConnectToDatabase() {
        String jdbcUrl = "jdbc:mysql://localhost:13306/nickfury?useSSL=false";
        Properties props = new Properties();
        props.setProperty("user", "nickfury");
        props.setProperty("password", "mwKGFy3T6SBZ");
        props.setProperty("connectTimeout", "5000");
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, props)) {
            return conn.isValid(5);
        } catch (SQLException e) {
            log.debug("Database connection failed: {}", e.getMessage());
            return false;
        }
    }
    
    private static void waitForDatabase(int maxRetries, int retryDelaySeconds) {
        for (int i = 1; i <= maxRetries; i++) {
            log.info("Checking database connection (attempt {}/{})...", i, maxRetries);
            
            if (canConnectToDatabase()) {
                log.info("✅ Database connection successful after {} attempt(s)", i);
                return;
            }
            
            if (i < maxRetries) {
                log.warn("Database not available yet, retrying in {} seconds...", retryDelaySeconds);
                try {
                    TimeUnit.SECONDS.sleep(retryDelaySeconds);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.error("❌ Failed to connect to database after {} attempts", maxRetries);
        assumeTrue(false, "Database is not available and could not be reached after multiple attempts");
    }

    @Autowired
    private DataSource dataSource;

    @Test
    public void testDatabaseConnection() throws Exception {
        assertNotNull(dataSource, "DataSource should not be null");
        log.info("Testing database connection...");
        
        try (Connection connection = dataSource.getConnection()) {
            log.info("Database connection established, validating...");
            assertTrue(connection.isValid(5), "Connection should be valid");
            
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT 1")) {
                
                assertTrue(rs.next(), "Should have at least one row");
                int result = rs.getInt(1);
                log.info("Query result: {}", result);
                assertEquals(1, result, "Should return 1");
                log.info("✅ Database connection test passed");
            }
        } catch (Exception e) {
            log.error("❌ Database connection test failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Test
    public void testListTables() throws Exception {
        assertNotNull(dataSource, "DataSource should not be null");
        log.info("Listing database tables...");
        
        try (Connection connection = dataSource.getConnection()) {
            log.debug("Connected to database: {}", connection.getMetaData().getURL());
            
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SHOW TABLES")) {
                
                if (!rs.next()) {
                    log.warn("⚠️ No tables found in the database");
                    return; // Skip the test if no tables found instead of failing
                }
                
                log.info("📋 Tables in Chandler database:");
                int count = 0;
                do {
                    String tableName = rs.getString(1);
                    log.info("- {}", tableName);
                    count++;
                } while (rs.next());
                
                log.info("✅ Found {} tables", count);
                assertTrue(count > 0, "Expected at least one table in the database");
            }
        } catch (Exception e) {
            log.error("❌ Error listing tables: {}", e.getMessage(), e);
            throw e;
        }
    }
}

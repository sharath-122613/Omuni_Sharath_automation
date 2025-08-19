package com.cloud.omuni_cloud.dbutil;

import com.cloud.omuni_cloud.dbutil.config.DatabaseConfig;
import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DatabaseConnection class using TestContainers.
 * These tests require Docker to be running.
 */
@Testcontainers
public class DatabaseConnectionTest {
    private static final String TEST_DB = "testdb";
    private static final String TEST_USER = "testuser";
    private static final String TEST_PASSWORD = "testpass";

    // Create a network for our containers to communicate
    private static final Network network = Network.newNetwork();

    // MySQL container for testing
    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName(TEST_DB)
            .withUsername(TEST_USER)
            .withPassword(TEST_PASSWORD)
            .withNetwork(network)
            .withNetworkAliases("mysql")
            .waitingFor(Wait.forListeningPort());

    // We'll skip SSH container for now as it's not essential for basic database testing
    // and can be complex to set up in CI environments

    @BeforeAll
    static void setup() {
        // Start the MySQL container
        mysqlContainer.start();

        // Initialize test data
        initializeTestData();

        // Configure the database connection for testing
        DbConnectionConfig config = new DbConnectionConfig(
                mysqlContainer.getHost(),
                String.valueOf(mysqlContainer.getMappedPort(3306)),
                TEST_DB,
                TEST_USER,
                TEST_PASSWORD
        );

        // Add the test configuration
        DatabaseConfig.addDatabaseConfig("test", config);
    }

    @AfterAll
    static void teardown() {
        // Clean up the container
        if (mysqlContainer != null) {
            mysqlContainer.stop();
        }
        
        // Clear the database configuration
        DatabaseConfig.clearDatabaseConfigs();
    }

    private static void initializeTestData() {
        try (Connection connection = DriverManager.getConnection(
                mysqlContainer.getJdbcUrl(),
                mysqlContainer.getUsername(),
                mysqlContainer.getPassword());
             Statement stmt = connection.createStatement()) {

            // Create test table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) NOT NULL, " +
                "status VARCHAR(20) DEFAULT 'active'" +
                ")"
            );

            // Insert test data
            stmt.execute(
                "INSERT INTO users (username, email, status) VALUES " +
                "('user1', 'user1@example.com', 'active'), " +
                "('user2', 'user2@example.com', 'inactive'), " +
                "('user3', 'user3@example.com', 'active')"
            );

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize test data", e);
        }
    }

    @Test
    void testGetConnection() throws SQLException, ClassNotFoundException {
        // Arrange
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        // Act
        try (Connection connection = dbConnection.getConnection("test")) {
            // Assert
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");
            
            // Verify we can execute a query
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                assertTrue(rs.next(), "Should have at least one row");
                assertTrue(rs.getInt(1) > 0, "Should have some test data");
            }
        }
    }

    @Test
    void testConnectionPooling() throws SQLException, ClassNotFoundException {
        // Arrange
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        
        // Act - Get the same connection twice
        try (Connection conn1 = dbConnection.getConnection("test");
             Connection conn2 = dbConnection.getConnection("test")) {
            
            // Assert - Should be the same connection instance from the pool
            assertSame(conn1, conn2, "Should return the same connection instance from the pool");
        }
    }

    @Test
    void testCloseConnection() throws SQLException, ClassNotFoundException {
        // Arrange
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        Connection connection = dbConnection.getConnection("test");
        
        // Act
        dbConnection.close("test");
        
        // Assert
        assertTrue(connection.isClosed(), "Connection should be closed");
    }

    @Test
    void testConnectionWithSsh() {
        // Test SSH configuration without starting an actual SSH server
        DbConnectionConfig sshConfig = new DbConnectionConfig(
                "localhost",
                "3306",
                TEST_DB,
                TEST_USER,
                TEST_PASSWORD
        ).withSsh(
                "localhost", // SSH host
                22,          // SSH port
                "testuser",  // SSH username
                "testpass",  // SSH password
                3307         // Local port
        );

        assertTrue(sshConfig.useSsh(), "Should use SSH tunneling");
        assertEquals("testuser", sshConfig.getSshUsername(), "SSH username should be set");
        assertEquals(3307, sshConfig.getSshLocalPort(), "SSH local port should be set");
    }

    @Test
    void testDatabaseManager() throws SQLException {
        // Arrange
        DatabaseManager dbManager = new DatabaseManager("test");
        
        try {
            // Act - Execute a query
            var results = dbManager.executeQuery("SELECT * FROM users WHERE status = ?", "active");
            
            // Assert
            assertNotNull(results, "Results should not be null");
            assertFalse(results.isEmpty(), "Should return at least one active user");
            
            // Verify record exists
            boolean exists = dbManager.recordExists("users", Map.of("email", "user1@example.com"));
            assertTrue(exists, "User should exist in the database");
            
            // Test single value query
            Object count = dbManager.getSingleValue("SELECT COUNT(*) FROM users");
            assertNotNull(count, "Count should not be null");
            assertTrue(((Number)count).intValue() > 0, "Should have some users");
        } finally {
            // Ensure resources are cleaned up
            dbManager.close();
        }
    }
}

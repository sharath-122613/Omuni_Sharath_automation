package com.cloud.omuni_cloud.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Configuration
@Profile("test")
public class ChandlerTestConfig {
    private static final Logger log = LoggerFactory.getLogger(ChandlerTestConfig.class);
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_SECONDS = 2;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriverClassName;

    @Value("${ssh.tunnel.enabled:false}")
    private boolean sshTunnelEnabled;

    @Autowired(required = false)
    private TestDatabaseConfig testDatabaseConfig;

    @Bean
    @DependsOn("createSshTunnel")
    public DataSource chandlerDataSource() {
        log.info("Creating Chandler database data source");
        
        if (sshTunnelEnabled) {
            log.info("SSH tunnel is enabled, waiting for tunnel to be ready...");
            waitForTunnel();
        }

        HikariConfig config = new HikariConfig();
        
        // Set basic connection properties
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName(dbDriverClassName);
        
        // Connection pool settings
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);
        config.setMinimumIdle(1);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(1800000);
        config.setInitializationFailTimeout(60000);
        
        // Additional connection properties
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
        config.addDataSourceProperty("serverTimezone", "UTC");
        config.addDataSourceProperty("useLegacyDatetimeCode", "false");
        
        HikariDataSource dataSource = new HikariDataSource(config);
        testConnection(dataSource);
        return dataSource;
    }

    private void waitForTunnel() {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                // Create connection properties
                Properties props = new Properties();
                props.setProperty("user", dbUsername);
                props.setProperty("password", dbPassword);
                props.setProperty("useSSL", "false");
                props.setProperty("allowPublicKeyRetrieval", "true");
                props.setProperty("serverTimezone", "UTC");
                
                // Test the connection
                try (Connection connection = DriverManager.getConnection(dbUrl, props)) {
                    if (connection.isValid(5)) {
                        log.info("✅ Successfully connected to database through SSH tunnel");
                        return;
                    }
                }
            } catch (SQLException e) {
                log.warn("Connection attempt {} failed: {}", attempts + 1, e.getMessage());
            }
            
            attempts++;
            if (attempts >= MAX_RETRIES) {
                throw new RuntimeException("Failed to establish database connection after " + MAX_RETRIES + " attempts");
            }
            
            try {
                log.info("Retrying connection in {} seconds... (attempt {}/{})", 
                        RETRY_DELAY_SECONDS, attempts + 1, MAX_RETRIES);
                TimeUnit.SECONDS.sleep(RETRY_DELAY_SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for database connection", ie);
            }
        }
    }

    private void testConnection(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            log.info("Successfully connected to Chandler database: {}", 
                    connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            log.error("Failed to connect to Chandler database", e);
            throw new RuntimeException("Failed to connect to Chandler database", e);
        }
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    public DataSourceInitializer chandlerDataSourceInitializer(DataSource chandlerDataSource) {
        log.info("Initializing Chandler database");
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(chandlerDataSource);
        
        // Optional: Initialize test data
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(true);
        populator.setIgnoreFailedDrops(true);
        
        // Add schema and test data scripts if needed
        // populator.addScript(new ClassPathResource("db/chandler/schema.sql"));
        // populator.addScript(new ClassPathResource("db/chandler/test-data.sql"));
        
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}

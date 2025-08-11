package com.cloud.omuni_cloud.config;

import com.cloud.omuni_cloud.dbutil.DatabaseConnection;
import com.cloud.omuni_cloud.dbutil.DatabaseManager;
import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;
import com.cloud.omuni_cloud.dbutil.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import jakarta.annotation.PostConstruct;

@Configuration
@PropertySource("classpath:application-test.properties")
@Profile("test")
public class TestConfig {
    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);

    @Value("${db.host}")
    private String dbHost;

    @Value("${db.port}")
    private String dbPort;

    @Value("${db.name}")
    private String dbName;

    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    @Value("${ssh.host}")
    private String sshHost;

    @Value("${ssh.port}")
    private int sshPort;

    @Value("${ssh.username}")
    private String sshUsername;

    @Value("${ssh.password}")
    private String sshPassword;

    @Value("${ssh.local.port:3307}")
    private int sshLocalPort;

    @PostConstruct
    public void init() {
        try {
            // Configure the database connection with SSH tunneling
            DbConnectionConfig dbConfig = new DbConnectionConfig(
                dbHost,
                dbPort,
                dbName,
                dbUsername,
                dbPassword
            ).withSsh(
                sshHost,
                sshPort,
                sshUsername,
                sshPassword,
                sshLocalPort
            );

            // Add the configuration to DatabaseConfig
            DatabaseConfig.addDatabaseConfig("nickfury", dbConfig);
            logger.info("Successfully configured database connection for 'nickfury'");
        } catch (Exception e) {
            logger.error("Failed to configure database connection", e);
            throw new RuntimeException("Failed to configure database connection", e);
        }
    }

    // Database connection is now managed by DatabaseTestConfig
}

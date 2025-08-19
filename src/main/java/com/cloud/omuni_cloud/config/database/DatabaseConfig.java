package com.cloud.omuni_cloud.config.database;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({DatabaseProperties.class, DataSourceProperties.class})
public class DatabaseConfig {
    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private final DatabaseProperties dbProperties;
    private Session sshSession;

    @Primary
    @Bean
    public DataSource dataSource() throws SQLException {
        if (dbProperties.getSshTunnel().isEnabled()) {
            setupSshTunnel();
        }
        return createHikariDataSource();
    }

    private void setupSshTunnel() {
        try {
            log.info("Setting up SSH tunnel to {}:{}...", 
                    dbProperties.getSshTunnel().getHost(), 
                    dbProperties.getSshTunnel().getPort());

            JSch jsch = new JSch();
            
            // Add private key if provided, otherwise use password
            if (dbProperties.getSshTunnel().getPrivateKey() != null && !dbProperties.getSshTunnel().getPrivateKey().isEmpty()) {
                jsch.addIdentity(dbProperties.getSshTunnel().getPrivateKey());
            }
            
            sshSession = jsch.getSession(
                    dbProperties.getSshTunnel().getUsername(),
                    dbProperties.getSshTunnel().getHost(),
                    dbProperties.getSshTunnel().getPort()
            );

            // Set password if private key is not provided
            if (dbProperties.getSshTunnel().getPassword() != null && !dbProperties.getSshTunnel().getPassword().isEmpty()) {
                sshSession.setPassword(dbProperties.getSshTunnel().getPassword());
            }

            // Avoid asking for key confirmation
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(config);
            
            // Connect to the SSH server
            sshSession.connect();
            
            // Set up port forwarding
            int assignedPort = sshSession.setPortForwardingL(
                    dbProperties.getSshTunnel().getLocalPort(),
                    dbProperties.getSshTunnel().getRemoteHost(),
                    dbProperties.getSshTunnel().getRemotePort()
            );
            
            log.info("SSH tunnel established on local port: {}", assignedPort);
            
        } catch (JSchException e) {
            log.error("Failed to establish SSH tunnel", e);
            throw new RuntimeException("Failed to establish SSH tunnel", e);
        }
    }

    private HikariDataSource createHikariDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Configure HikariCP
        config.setJdbcUrl(dbProperties.getUrl());
        config.setUsername(dbProperties.getUsername());
        config.setPassword(dbProperties.getPassword());
        config.setDriverClassName(dbProperties.getDriverClassName());
        
        // Hikari pool settings
        config.setPoolName(dbProperties.getHikari().getPoolName());
        config.setMaximumPoolSize(dbProperties.getHikari().getMaximumPoolSize());
        config.setMinimumIdle(dbProperties.getHikari().getMinimumIdle());
        config.setConnectionTimeout(dbProperties.getHikari().getConnectionTimeout());
        config.setIdleTimeout(dbProperties.getHikari().getIdleTimeout());
        config.setMaxLifetime(dbProperties.getHikari().getMaxLifetime());
        
        // Additional properties
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        return new HikariDataSource(config);
    }

    @PreDestroy
    public void close() {
        if (sshSession != null && sshSession.isConnected()) {
            log.info("Closing SSH tunnel...");
            sshSession.disconnect();
        }
    }
}

package com.cloud.omuni_cloud.config;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import jakarta.annotation.PreDestroy;

import java.util.Objects;
import java.util.Properties;

@Configuration
@Profile("test")
public class TestDatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(TestDatabaseConfig.class);
    private static final String SSH_TUNNEL_ENABLED = "SSH_TUNNEL_ENABLED";
    private static final String SSH_HOST = "SSH_HOST";
    private static final String SSH_PORT = "SSH_PORT";
    private static final String SSH_USERNAME = "SSH_USERNAME";
    private static final String SSH_PASSWORD = "SSH_PASSWORD";
    private static final String REMOTE_DB_HOST = "REMOTE_DB_HOST";
    private static final String REMOTE_DB_PORT = "REMOTE_DB_PORT";
    private static final String LOCAL_PORT = "LOCAL_PORT";

    private Session sshSession;
    private final Environment env;

    public TestDatabaseConfig(Environment env) {
        this.env = env;
    }

    private boolean isSshTunnelEnabled() {
        String value = env.getProperty(SSH_TUNNEL_ENABLED);
        return Boolean.parseBoolean(value != null ? value : "false");
    }

    private String getSshHost() {
        return Objects.requireNonNull(env.getProperty(SSH_HOST), 
            "SSH host must be set via " + SSH_HOST + " environment variable");
    }

    private int getSshPort() {
        return Integer.parseInt(Objects.requireNonNullElse(
            env.getProperty(SSH_PORT), "22"));
    }

    private String getSshUsername() {
        return Objects.requireNonNull(env.getProperty(SSH_USERNAME),
            "SSH username must be set via " + SSH_USERNAME + " environment variable");
    }

    private String getSshPassword() {
        return Objects.requireNonNull(env.getProperty(SSH_PASSWORD),
            "SSH password must be set via " + SSH_PASSWORD + " environment variable");
    }

    private String getRemoteDbHost() {
        return Objects.requireNonNull(env.getProperty(REMOTE_DB_HOST),
            "Remote DB host must be set via " + REMOTE_DB_HOST + " environment variable");
    }

    private int getRemoteDbPort() {
        return Integer.parseInt(Objects.requireNonNullElse(
            env.getProperty(REMOTE_DB_PORT), "3306"));
    }

    private int getLocalPort() {
        return Integer.parseInt(Objects.requireNonNullElse(
            env.getProperty(LOCAL_PORT), "13306"));
    }

    @Bean
    @ConditionalOnProperty(name = "ssh.tunnel.enabled", havingValue = "true")
    public synchronized Boolean createSshTunnel() {
        if (!isSshTunnelEnabled()) {
            log.info("SSH tunnel is disabled");
            return false;
        }

        int maxRetries = 3;
        int retryDelay = 5; // seconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String sshHost = getSshHost();
                int sshPort = getSshPort();
                String sshUsername = getSshUsername();
                String sshPassword = getSshPassword();
                int localPort = getLocalPort();
                String remoteDbHost = getRemoteDbHost();
                int remoteDbPort = getRemoteDbPort();

                log.info("Creating SSH tunnel (attempt {}/{}) to {}:{}", 
                        attempt, maxRetries, sshHost, sshPort);
                
                JSch jsch = new JSch();
                sshSession = jsch.getSession(sshUsername, sshHost, sshPort);
                sshSession.setPassword(sshPassword);
                
                // Configure SSH session
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                config.put("ServerAliveInterval", "30");
                sshSession.setConfig(config);
                
                // Set connection timeout to 10 seconds
                sshSession.connect(10000);
                
                // Set up port forwarding
                int assignedPort = sshSession.setPortForwardingL(localPort, remoteDbHost, remoteDbPort);
                log.info("✅ SSH tunnel established. Local port {} → {}:{}", 
                        assignedPort, remoteDbHost, remoteDbPort);
                
                return true;
                
            } catch (JSchException e) {
                log.warn("SSH tunnel attempt {}/{} failed: {}", 
                        attempt, maxRetries, e.getMessage());
                
                if (sshSession != null && sshSession.isConnected()) {
                    sshSession.disconnect();
                }
                
                if (attempt < maxRetries) {
                    try {
                        log.info("Retrying in {} seconds...", retryDelay);
                        Thread.sleep(retryDelay * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting to retry SSH connection", ie);
                    }
                } else {
                    throw new RuntimeException("Failed to establish SSH tunnel after " + maxRetries + " attempts", e);
                }
            }
        }
        
        return false;
    }

    @PreDestroy
    public void closeSshTunnel() {
        if (sshSession != null && sshSession.isConnected()) {
            log.info("Closing SSH tunnel");
            sshSession.disconnect();
        }
    }
}

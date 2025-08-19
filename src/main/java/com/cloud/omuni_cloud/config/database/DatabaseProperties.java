package com.cloud.omuni_cloud.config.database;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private Hikari hikari = new Hikari();
    private SshTunnel sshTunnel = new SshTunnel();

    // Explicit getter for sshTunnel to ensure compatibility
    public SshTunnel getSshTunnel() {
        return sshTunnel;
    }

    // Explicit getters to ensure compatibility
    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public Hikari getHikari() {
        return hikari;
    }

    @Getter
    @Setter
    public static class Hikari {
        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private String poolName = "HikariPool";

        // Explicit getters to ensure compatibility
        public String getPoolName() {
            return poolName;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public int getMinimumIdle() {
            return minimumIdle;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public long getIdleTimeout() {
            return idleTimeout;
        }

        public long getMaxLifetime() {
            return maxLifetime;
        }
    }

    @Getter
    @Setter
    public static class SshTunnel {
        private boolean enabled = false;
        private String host;
        private int port = 22;
        private String username;
        private String password;
        private String privateKey;
        private String remoteHost;
        private int remotePort = 3306;
        private int localPort = 13306;

        // Explicit getters to ensure compatibility
        public boolean isEnabled() {
            return enabled;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public String getRemoteHost() {
            return remoteHost;
        }

        public int getRemotePort() {
            return remotePort;
        }

        public int getLocalPort() {
            return localPort;
        }
    }
}

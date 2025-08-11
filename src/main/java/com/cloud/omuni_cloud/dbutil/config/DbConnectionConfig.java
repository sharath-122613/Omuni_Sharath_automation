package com.cloud.omuni_cloud.dbutil.config;

/**
 * Configuration class for a single database connection
 */
public class DbConnectionConfig {
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;
    private boolean useSsh;
    private String sshHost;
    private int sshPort;
    private String sshUsername;
    private String sshPassword;
    private int sshLocalPort;

    public DbConnectionConfig(String host, String port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
        this.useSsh = false;
    }

    // Getters
    public String getHost() { return host; }
    public String getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean useSsh() { return useSsh; }
    public String getSshHost() { return sshHost; }
    public int getSshPort() { return sshPort; }
    public String getSshUsername() { return sshUsername; }
    public String getSshPassword() { return sshPassword; }
    public int getSshLocalPort() { return sshLocalPort; }

    // Builder-style setters for fluent configuration
    public DbConnectionConfig withSsh(String sshHost, int sshPort, String sshUsername, String sshPassword, int sshLocalPort) {
        this.useSsh = true;
        this.sshHost = sshHost;
        this.sshPort = sshPort;
        this.sshUsername = sshUsername;
        this.sshPassword = sshPassword;
        this.sshLocalPort = sshLocalPort;
        return this;
    }

    @Override
    public String toString() {
        return "DbConnectionConfig{" +
                "host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", database='" + database + '\'' +
                ", username='" + username + '\'' +
                ", useSsh=" + useSsh +
                (useSsh ? 
                    ", sshHost='" + sshHost + '\'' +
                    ", sshPort=" + sshPort +
                    ", sshUsername='" + sshUsername + '\''
                    : "") +
                '}';
    }
}

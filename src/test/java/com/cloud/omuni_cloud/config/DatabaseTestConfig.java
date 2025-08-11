package com.cloud.omuni_cloud.config;

import com.cloud.omuni_cloud.dbutil.DatabaseConnection;
import com.cloud.omuni_cloud.dbutil.DatabaseManager;
import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;
import com.cloud.omuni_cloud.dbutil.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@Profile("test")
@TestPropertySource("classpath:application-test.properties")
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    JdbcTemplateAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@EnableTransactionManagement
@EnableConfigurationProperties(JpaProperties.class)
@EnableJpaRepositories(basePackages = "com.cloud.omuni_cloud")
@EntityScan("com.cloud.omuni_cloud")
@ComponentScan(basePackages = "com.cloud.omuni_cloud")
public class DatabaseTestConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTestConfig.class);

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
            // Clear any existing configurations to avoid conflicts
            DatabaseConfig.clearDatabaseConfigs();
            
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

            // Add the configuration to DatabaseConfig with the name "nickfury"
            DatabaseConfig.addDatabaseConfig("nickfury", dbConfig);
            logger.info("Successfully configured database connection for 'nickfury'");
        } catch (Exception e) {
            logger.error("Failed to configure database connection", e);
            throw new RuntimeException("Failed to configure database connection", e);
        }
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useLegacyDatetimeCode=false&createDatabaseIfNotExist=true", 
            dbHost, dbPort, dbName));
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setAutoCommit(true);
        config.setLeakDetectionThreshold(60000);
        config.setPoolName("TestHikariPool");
        
        // Add Hibernate properties
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        
        return new HikariDataSource(config);
    }
    
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, JpaProperties jpaProperties) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.cloud.omuni_cloud");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.putAll(jpaProperties.getProperties());
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");
        
        // Enable HikariCP connection pool logging
        properties.put("hibernate.hikari.connectionTimeout", "30000");
        properties.put("hibernate.hikari.minimumIdle", "2");
        properties.put("hibernate.hikari.maximumPoolSize", "10");
        properties.put("hibernate.hikari.idleTimeout", "600000");
        properties.put("hibernate.hikari.maxLifetime", "1800000");
        properties.put("hibernate.hikari.autoCommit", "true");
        properties.put("hibernate.hikari.leakDetectionThreshold", "60000");
        properties.put("hibernate.hikari.poolName", "TestHikariPool");
        
        em.setJpaProperties(properties);
        return em;
    }
    
    @Bean(destroyMethod = "close")
    @Primary
    public DatabaseManager databaseManager(DataSource dataSource) {
        DatabaseManager manager = new DatabaseManager("nickfury");
        // Initialize the database connection if needed
        databaseConnection();
        return manager;
    }
    
    @Bean(destroyMethod = "closeAllConnections")
    @Primary
    public DatabaseConnection databaseConnection() {
        return DatabaseConnection.getInstance();
    }
    
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}

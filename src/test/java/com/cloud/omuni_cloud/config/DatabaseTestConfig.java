package com.cloud.omuni_cloud.config;

import com.cloud.omuni_cloud.dbutil.DatabaseConnection;
import com.cloud.omuni_cloud.dbutil.config.DatabaseConfig;
import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

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
@EnableJpaRepositories(basePackages = "com.cloud.omuni_cloud.repository")
@EntityScan("com.cloud.omuni_cloud")
@ComponentScan(basePackages = {
    "com.cloud.omuni_cloud.service",
    "com.cloud.omuni_cloud.config"
})
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
    
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", 
            dbHost, dbPort, dbName));
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        return new HikariDataSource(config);
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.cloud.omuni_cloud.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.format_sql", "true");
        
        em.setJpaProperties(properties);
        return em;
    }
    
    @Value("${ssh.host}")
    private String sshHost;

    @Value("${ssh.port}")
    private int sshPort;

    @Value("${ssh.username:}")
    private String sshUsername;

    @Value("${ssh.password:}")
    private String sshPassword;
    
    @Value("${ssh.privateKey:}")
    private String sshPrivateKey;
    
    @Value("${ssh.passphrase:}")
    private String sshPassphrase;

    @Value("${ssh.local.port:13306}")
    private int localPort;

    @Bean
    @ConditionalOnMissingBean
    public DatabaseConnection databaseConnection() {
        // This bean is kept for backward compatibility
        return DatabaseConnection.getInstance();
    }

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
            );
            
            // Only configure SSH if username is provided
            if (sshUsername != null && !sshUsername.isEmpty()) {
                if (sshPrivateKey != null && !sshPrivateKey.isEmpty()) {
                    // Use key-based authentication if private key is provided
                    logger.warn("Private key authentication is not fully supported in DbConnectionConfig. Using private key as password.");
                    dbConfig.withSsh(
                        sshHost,
                        sshPort,
                        sshUsername,
                        sshPrivateKey, // Using private key as password (not ideal)
                        localPort
                    );
                } else if (sshPassword != null && !sshPassword.isEmpty()) {
                    // Fall back to password authentication if no private key is provided
                    dbConfig.withSsh(
                        sshHost,
                        sshPort,
                        sshUsername,
                        sshPassword,
                        localPort
                    );
                } else {
                    logger.warn("SSH username provided but no authentication method specified. SSH tunneling will not be used.");
                }
            }

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
    @ConditionalOnMissingBean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
    
    @Bean
    @Primary
    public JpaProperties jpaProperties() {
        JpaProperties jpaProperties = new JpaProperties();
        jpaProperties.setShowSql(true);
        
        // Create a Map for Hibernate properties
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.jdbc.batch_size", "30");
        properties.put("hibernate.order_inserts", "true");
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.jdbc.batch_versioned_data", "true");
        
        // Set the properties to jpaProperties
        jpaProperties.getProperties().putAll(properties);
        return jpaProperties;
    }
}

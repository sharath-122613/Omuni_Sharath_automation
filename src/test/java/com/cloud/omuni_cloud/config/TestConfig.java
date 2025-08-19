package com.cloud.omuni_cloud.config;

import com.cloud.omuni_cloud.dbutil.DatabaseConnection;
import com.cloud.omuni_cloud.dbutil.DatabaseManager;
import com.cloud.omuni_cloud.dbutil.config.DbConnectionConfig;
import com.cloud.omuni_cloud.dbutil.config.DatabaseConfig;
import com.cloud.omuni_cloud.mapper.OrderItemMapper;
import com.cloud.omuni_cloud.mapper.OrderMapper;
import com.cloud.omuni_cloud.mapper.OrderMapperImpl;
import com.cloud.omuni_cloud.service.OrderDtoService;
import com.cloud.omuni_cloud.service.OrderService;
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
import org.springframework.context.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.annotation.PostConstruct;
import org.mockito.Mockito;

@Configuration
@Profile("test")
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    JdbcTemplateAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@TestPropertySource("classpath:application-test.properties")
@EnableConfigurationProperties(JpaProperties.class)
@EnableTransactionManagement
@EntityScan("com.cloud.omuni_cloud.entity")
@ComponentScan(
    basePackages = {
        "com.cloud.omuni_cloud.service",
        "com.cloud.omuni_cloud.mapper",
        "com.cloud.omuni_cloud.dbutil"
    },
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.cloud\\.omuni_cloud\\.config\\.OrderController.*"
    )
)
@EnableSpringDataWebSupport
public class TestConfig {
    private static final Logger logger = LoggerFactory.getLogger(TestConfig.class);

    // Database configuration with defaults
    @Value("${db.host:localhost}")
    private String dbHost;

    @Value("${db.port:3306}")
    private String dbPort;

    @Value("${db.name:testdb}")
    private String dbName;

    @Value("${db.username:root}")
    private String dbUsername;

    @Value("${db.password:}")
    private String dbPassword;

    // SSH configuration - all optional with safe defaults
    @Value("${ssh.tunnel.enabled:false}")
    private boolean sshTunnelEnabled;

    @Value("${ssh.tunnel.host:localhost}")
    private String sshHost;

    @Value("${ssh.tunnel.port:22}")
    private int sshPort;

    @Value("${ssh.tunnel.username:}")
    private String sshUsername;

    @Value("${ssh.tunnel.password:}")
    private String sshPassword;

    @Value("${ssh.tunnel.remote-db-host:localhost}")
    private String remoteDbHost;

    @Value("${ssh.tunnel.remote-db-port:3306}")
    private int remoteDbPort;

    @Value("${ssh.tunnel.local-port:13306}")
    private int localPort;

    // Mock beans for testing
    @Bean
    @Primary
    public OrderDtoService orderDtoService() {
        return Mockito.mock(OrderDtoService.class);
    }

    @Bean
    @Primary
    public OrderService orderService() {
        return Mockito.mock(OrderService.class);
    }

    @Bean
    @Primary
    public OrderMapper orderMapper() {
        return new OrderMapperImpl();
    }

    @Bean
    @Primary
    public OrderItemMapper orderItemMapper() {
        return Mockito.mock(OrderItemMapper.class);
    }

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing test configuration...");
            logger.debug("Database configuration - Host: {}, Port: {}, Name: {}, User: {}", 
                dbHost, dbPort, dbName, dbUsername);

            // Configure the database connection
            DbConnectionConfig dbConfig = new DbConnectionConfig(
                dbHost,
                dbPort,
                dbName,
                dbUsername,
                dbPassword
            );

            // Configure SSH tunnel if enabled and credentials are provided
            if (sshTunnelEnabled) {
                if (sshHost != null && !sshHost.isEmpty() && 
                    sshUsername != null && !sshUsername.isEmpty()) {
                    
                    dbConfig = dbConfig.withSsh(
                        sshHost,
                        sshPort,
                        sshUsername,
                        sshPassword != null ? sshPassword : "",
                        localPort
                    );
                    logger.info("SSH tunnel configured: {}@{}:{}", sshUsername, sshHost, sshPort);
                } else {
                    logger.warn("SSH tunnel enabled but required properties are missing. Disabling SSH tunneling.");
                    sshTunnelEnabled = false;
                }
            }

            // Initialize database configuration
            DatabaseConfig.addDatabaseConfig("test", dbConfig);
            logger.info("Database configuration initialized for database: {}", dbName);

        } catch (Exception e) {
            String errorMsg = String.format("Failed to initialize test configuration: %s", e.getMessage());
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    @Bean
    @Primary
    public DatabaseConnection databaseConnection() {
        return DatabaseConnection.getInstance();
    }
}

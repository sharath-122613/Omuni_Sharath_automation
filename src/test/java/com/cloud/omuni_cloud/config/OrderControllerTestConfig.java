package com.cloud.omuni_cloud.config;

import com.cloud.omuni_cloud.controller.OrderController;
import com.cloud.omuni_cloud.mapper.OrderMapper;
import com.cloud.omuni_cloud.mapper.OrderMapperImpl;
import com.cloud.omuni_cloud.service.OrderDtoService;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@TestConfiguration
@EnableWebMvc
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@ComponentScan(
    basePackages = "com.cloud.omuni_cloud.controller",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = OrderController.class
    )
)
public class OrderControllerTestConfig {

    @Bean
    public OrderDtoService orderDtoService() {
        return Mockito.mock(OrderDtoService.class);
    }

    @Bean
    public OrderMapper orderMapper() {
        return new OrderMapperImpl();
    }
}

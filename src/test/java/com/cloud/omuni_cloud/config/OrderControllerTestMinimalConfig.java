package com.cloud.omuni_cloud.config;

import com.cloud.omuni_cloud.controller.OrderController;
import com.cloud.omuni_cloud.mapper.OrderItemMapper;
import com.cloud.omuni_cloud.mapper.OrderMapper;
import com.cloud.omuni_cloud.mapper.OrderMapperImpl;
import com.cloud.omuni_cloud.service.OrderDtoService;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@TestConfiguration
@EnableWebMvc
@EnableSpringDataWebSupport
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
@ComponentScan(
    basePackages = "com.cloud.omuni_cloud.controller",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = OrderController.class
    )
)
public class OrderControllerTestMinimalConfig {

    @Bean
    public OrderDtoService orderDtoService() {
        return Mockito.mock(OrderDtoService.class);
    }

    @Bean
    public OrderMapper orderMapper() {
        return new OrderMapperImpl();
    }
    
    @Bean
    public OrderItemMapper orderItemMapper() {
        return Mockito.mock(OrderItemMapper.class);
    }
    
    @Bean
    public HateoasPageableHandlerMethodArgumentResolver pageableResolver() {
        HateoasPageableHandlerMethodArgumentResolver resolver = new HateoasPageableHandlerMethodArgumentResolver();
        resolver.setFallbackPageable(PageRequest.of(0, 20));
        return resolver;
    }
    
    @Bean
    public Pageable defaultPageable() {
        return PageRequest.of(0, 20);
    }
}

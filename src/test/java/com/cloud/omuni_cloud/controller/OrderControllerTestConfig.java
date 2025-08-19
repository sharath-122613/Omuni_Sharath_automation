package com.cloud.omuni_cloud.controller;

import com.cloud.omuni_cloud.mapper.OrderMapper;
import com.cloud.omuni_cloud.mapper.OrderMapperImpl;
import com.cloud.omuni_cloud.service.OrderDtoService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for OrderController integration tests.
 * Uses Mockito mocks for service layer beans.
 */
@TestConfiguration
@Import(OrderMapperImpl.class)
public class OrderControllerTestConfig {
    
    @Bean
    @Primary
    public OrderDtoService orderDtoService() {
        return Mockito.mock(OrderDtoService.class);
    }
    
    @Bean
    @Primary
    public OrderMapper orderMapper() {
        return new OrderMapperImpl();
    }
}

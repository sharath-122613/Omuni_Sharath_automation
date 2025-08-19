package com.cloud.omuni_cloud.config;

import com.cloud.omuni_cloud.repository.OrderRepository;
import com.cloud.omuni_cloud.repository.SaleOrderRepository;
import com.cloud.omuni_cloud.repository.StoreOrderRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MulesoftTestConfig {
    
    @Bean
    @Primary
    public SaleOrderRepository saleOrderRepository() {
        return Mockito.mock(SaleOrderRepository.class);
    }
    
    @Bean
    @Primary
    public StoreOrderRepository storeOrderRepository() {
        return Mockito.mock(StoreOrderRepository.class);
    }
    
    @Bean
    @Primary
    public OrderRepository orderRepository() {
        return Mockito.mock(OrderRepository.class);
    }
}

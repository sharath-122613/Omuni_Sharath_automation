package com.cloud.omuni_cloud.config;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for MapStruct.
 */
@Configuration
public class MapStructConfig {

    /**
     * Configures the OrderMapper as a Spring bean.
     *
     * @return the OrderMapper instance
     */
    @Bean
    public com.cloud.omuni_cloud.mapper.OrderMapper orderMapper() {
        return Mappers.getMapper(com.cloud.omuni_cloud.mapper.OrderMapper.class);
    }

    /**
     * Configures the OrderItemMapper as a Spring bean.
     *
     * @return the OrderItemMapper instance
     */
    @Bean
    public com.cloud.omuni_cloud.mapper.OrderItemMapper orderItemMapper() {
        return Mappers.getMapper(com.cloud.omuni_cloud.mapper.OrderItemMapper.class);
    }

    /**
     * Base configuration for all mappers in the application.
     * This interface is used as a template for all other mappers.
     */
    @Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
            unmappedTargetPolicy = ReportingPolicy.IGNORE)
    public interface BaseMapper {
        // Base mapper configuration
    }
}

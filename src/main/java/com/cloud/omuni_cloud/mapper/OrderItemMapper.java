package com.cloud.omuni_cloud.mapper;

import com.cloud.omuni_cloud.model.dto.OrderItemDto;
import com.cloud.omuni_cloud.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for converting between OrderItem entity and DTO.
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    /**
     * Convert OrderItem entity to OrderItemDto.
     *
     * @param orderItem the order item entity
     * @return the order item DTO
     */
    OrderItemDto toDto(OrderItem orderItem);

    /**
     * Convert OrderItemDto to OrderItem entity.
     *
     * @param orderItemDto the order item DTO
     * @return the order item entity
     */
    @Mapping(target = "order", ignore = true)
    OrderItem toEntity(OrderItemDto orderItemDto);

    /**
     * Update OrderItem entity from OrderItemDto.
     *
     * @param orderItemDto the order item DTO with updated values
     * @param orderItem   the order item entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    void updateOrderItemFromDto(OrderItemDto orderItemDto, @MappingTarget OrderItem orderItem);
}

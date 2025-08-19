package com.cloud.omuni_cloud.mapper;

import com.cloud.omuni_cloud.model.dto.OrderDto;
import com.cloud.omuni_cloud.model.dto.OrderItemDto;
import com.cloud.omuni_cloud.model.entity.Order;
import com.cloud.omuni_cloud.model.entity.OrderItem;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Mapper for converting between Order entity and DTO.
 */
@Mapper(componentModel = "spring",
        uses = {OrderItemMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    /**
     * Convert Order entity to OrderDto.
     *
     * @param order the order entity
     * @return the order DTO
     */
    OrderDto toDto(Order order);

    /**
     * Convert OrderDto to Order entity.
     *
     * @param orderDto the order DTO
     * @return the order entity
     */
    Order toEntity(OrderDto orderDto);

    /**
     * Update Order entity from OrderDto.
     *
     * @param orderDto the order DTO with updated values
     * @param order    the order entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateOrderFromDto(OrderDto orderDto, @MappingTarget Order order);

    /**
     * Convert a list of Order entities to a list of OrderDto.
     *
     * @param orders the list of order entities
     * @return the list of order DTOs
     */
    List<OrderDto> toDtoList(List<Order> orders);
}

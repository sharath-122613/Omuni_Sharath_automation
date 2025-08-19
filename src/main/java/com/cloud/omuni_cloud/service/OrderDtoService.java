package com.cloud.omuni_cloud.service;

import com.cloud.omuni_cloud.model.dto.OrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for managing orders using DTOs.
 */
public interface OrderDtoService {

    /**
     * Create a new order from DTO.
     *
     * @param orderDto the order DTO to create
     * @return the created order DTO
     */
    OrderDto createOrder(OrderDto orderDto);

    /**
     * Update an existing order from DTO.
     *
     * @param id       the order ID
     * @param orderDto the order DTO with updated values
     * @return the updated order DTO
     */
    OrderDto updateOrder(Long id, OrderDto orderDto);

    /**
     * Get an order by ID.
     *
     * @param id the order ID
     * @return the order DTO if found
     */
    OrderDto getOrderById(Long id);

    /**
     * Get an order by reference number.
     *
     * @param orderReference the order reference number
     * @return the order DTO if found
     */
    OrderDto getOrderByReference(String orderReference);

    /**
     * Get all orders with pagination.
     *
     * @param pageable pagination information
     * @return page of order DTOs
     */
    Page<OrderDto> getAllOrders(Pageable pageable);

    /**
     * Get orders by customer ID.
     *
     * @param customerId the customer ID
     * @param pageable  pagination information
     * @return page of customer's order DTOs
     */
    Page<OrderDto> getOrdersByCustomerId(String customerId, Pageable pageable);

    /**
     * Get orders by status.
     *
     * @param status   the order status
     * @param pageable pagination information
     * @return page of order DTOs with the given status
     */
    Page<OrderDto> getOrdersByStatus(String status, Pageable pageable);

    /**
     * Update order status.
     *
     * @param orderId the order ID
     * @param status  the new status
     * @return the updated order DTO
     */
    OrderDto updateOrderStatus(Long orderId, String status);

    /**
     * Delete an order.
     *
     * @param orderId the order ID to delete
     */
    void deleteOrder(Long orderId);

    /**
     * Find orders that need status updates.
     *
     * @param statuses list of statuses to check
     * @return list of order DTOs needing updates
     */
    List<OrderDto> findOrdersNeedingStatusUpdates(List<String> statuses);
}

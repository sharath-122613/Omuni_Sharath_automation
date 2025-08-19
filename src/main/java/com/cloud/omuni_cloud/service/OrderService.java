package com.cloud.omuni_cloud.service;

import com.cloud.omuni_cloud.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing orders.
 */
public interface OrderService {

    /**
     * Create a new order.
     *
     * @param order the order to create
     * @return the created order
     */
    Order createOrder(Order order);

    /**
     * Update an existing order.
     *
     * @param order the order to update
     * @return the updated order
     */
    Order updateOrder(Order order);

    /**
     * Get an order by ID.
     *
     * @param id the order ID
     * @return the order if found
     */
    Optional<Order> getOrderById(Long id);

    /**
     * Get an order by reference number.
     *
     * @param orderReference the order reference number
     * @return the order if found
     */
    Optional<Order> getOrderByReference(String orderReference);

    /**
     * Find orders that need status updates based on the provided statuses.
     *
     * @param statuses list of statuses to filter orders
     * @return list of orders matching the status criteria
     */
    List<Order> findOrdersNeedingStatusUpdates(List<String> statuses);

    /**
     * Get all orders with pagination.
     *
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> getAllOrders(Pageable pageable);

    /**
     * Get orders by customer ID.
     *
     * @param customerId the customer ID
     * @param pageable  pagination information
     * @return page of customer's orders
     */
    Page<Order> getOrdersByCustomerId(String customerId, Pageable pageable);

    /**
     * Get orders by status.
     *
     * @param status   the order status
     * @param pageable pagination information
     * @return page of orders with the given status
     */
    Page<Order> getOrdersByStatus(String status, Pageable pageable);

    /**
     * Update order status.
     *
     * @param orderId the order ID
     * @param status  the new status
     * @return the updated order
     */
    Order updateOrderStatus(Long orderId, String status);

    /**
     * Cancel an order.
     *
     * @param orderId the order ID
     * @param reason  the cancellation reason
     * @return the cancelled order
     */
    Order cancelOrder(Long orderId, String reason);

    /**
     * Delete an order.
     *
     * @param orderId the order ID to delete
     */
    void deleteOrder(Long orderId);

    /**
     * Check if an order with the given reference exists.
     *
     * @param orderReference the order reference number
     * @return true if exists, false otherwise
     */
    boolean orderExists(String orderReference);

    /**
     * Get orders that need status updates.
     *
     * @param statuses list of statuses to check
     * @return list of orders needing updates
     */
    List<Order> findOrdersNeedingStatusUpdate(List<String> statuses);
}

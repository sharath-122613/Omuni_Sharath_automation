package com.cloud.omuni_cloud.repository;

import com.cloud.omuni_cloud.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity with custom query methods.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    /**
     * Find an order by its reference number.
     *
     * @param orderReference the order reference number
     * @return an Optional containing the order if found
     */
    Optional<Order> findByOrderReference(String orderReference);

    /**
     * Find all orders for a specific customer with pagination.
     *
     * @param customerId the customer ID
     * @param pageable  pagination information
     * @return page of orders for the customer
     */
    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    /**
     * Find orders by status with pagination.
     *
     * @param status   the order status
     * @param pageable pagination information
     * @return page of orders with the given status
     */
    Page<Order> findByStatus(String status, Pageable pageable);

    /**
     * Find orders with any of the given statuses.
     *
     * @param statuses list of statuses to search for
     * @return list of orders with any of the given statuses
     */
    List<Order> findByStatusIn(List<String> statuses);
    
    /**
     * Find orders created within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return list of orders created within the date range
     */
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders by customer ID and status.
     *
     * @param customerId the customer ID
     * @param status     the order status
     * @return list of matching orders
     */
    List<Order> findByCustomerIdAndStatus(String customerId, String status);

    /**
     * Check if an order with the given reference exists.
     *
     * @param orderReference the order reference number
     * @return true if an order with the reference exists, false otherwise
     */
    boolean existsByOrderReference(String orderReference);

    /**
     * Find orders by FC (Fulfillment Center) ID.
     *
     * @param fcId the fulfillment center ID
     * @return list of orders for the FC
     */
    List<Order> findByFcId(String fcId);

    /**
     * Find orders by payment status.
     *
     * @param paymentStatus the payment status
     * @return list of orders with the given payment status
     */
    List<Order> findByPaymentStatus(String paymentStatus);

    /**
     * Find orders by source system.
     *
     * @param source the source system
     * @return list of orders from the source system
     */
    List<Order> findBySource(String source);

    /**
     * Find orders with a specific product.
     *
     * @param productId the product ID
     * @return list of orders containing the product
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.productId = :productId")
    List<Order> findOrdersContainingProduct(@Param("productId") String productId);

    /**
     * Count orders by status.
     *
     * @param status the order status
     * @return count of orders with the given status
     */
    long countByStatus(String status);

    /**
     * Find orders that need status updates (e.g., pending processing).
     *
     * @param statuses list of statuses to include
     * @return list of orders matching the status criteria
     */
    @Query("SELECT o FROM Order o WHERE o.status IN :statuses")
    List<Order> findOrdersNeedingStatusUpdate(@Param("statuses") List<String> statuses);
}

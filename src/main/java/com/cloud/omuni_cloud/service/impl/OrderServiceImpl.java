package com.cloud.omuni_cloud.service.impl;

import com.cloud.omuni_cloud.exception.ResourceNotFoundException;
import com.cloud.omuni_cloud.model.entity.Order;
import com.cloud.omuni_cloud.model.entity.OrderItem;
import com.cloud.omuni_cloud.repository.OrderRepository;
import com.cloud.omuni_cloud.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link OrderService} for managing orders.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        log.info("Creating new order with reference: {}", order.getOrderReference());
        
        // Validate order
        validateOrder(order);
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        
        // Calculate and set order total
        order.calculateTotalAmount();
        
        // Save the order
        Order savedOrder = orderRepository.save(order);
        log.info("Created order with ID: {}", savedOrder.getId());
        
        return savedOrder;
    }

    @Override
    @Transactional
    public Order updateOrder(Order order) {
        log.info("Updating order with ID: {}", order.getId());
        
        // Check if order exists
        Order existingOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + order.getId()));
        
        // Update fields
        existingOrder.setOrderReference(order.getOrderReference());
        existingOrder.setCustomerId(order.getCustomerId());
        
        // Update status if changed
        if (!existingOrder.getStatus().equals(order.getStatus())) {
            existingOrder.updateStatus(order.getStatus());
        }
        
        existingOrder.setTotalAmount(order.getTotalAmount());
        existingOrder.setCurrency(order.getCurrency());
        existingOrder.setFcId(order.getFcId());
        existingOrder.setSource(order.getSource());
        existingOrder.setOrderDate(order.getOrderDate());
        existingOrder.setDeliveryDate(order.getDeliveryDate());
        existingOrder.setShippingAddress(order.getShippingAddress());
        existingOrder.setBillingAddress(order.getBillingAddress());
        existingOrder.setPaymentStatus(order.getPaymentStatus());
        existingOrder.setPaymentMethod(order.getPaymentMethod());
        existingOrder.setTrackingNumber(order.getTrackingNumber());
        existingOrder.setCarrier(order.getCarrier());
        
        // Update notes if changed
        if (order.getNotes() != null && !order.getNotes().equals(existingOrder.getNotes())) {
            existingOrder.addNote("Updated order details");
        }
        
        // Update items
        existingOrder.getItems().clear();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                existingOrder.addItem(item);
            }
            existingOrder.calculateTotalAmount();
        }
        
        // Save the updated order
        Order updatedOrder = orderRepository.save(existingOrder);
        log.info("Updated order with ID: {}", updatedOrder.getId());
        
        return updatedOrder;
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);
        return orderRepository.findById(id);
    }

    @Override
    public Optional<Order> getOrderByReference(String orderReference) {
        log.debug("Fetching order with reference: {}", orderReference);
        return orderRepository.findByOrderReference(orderReference);
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders (page: {}, size: {})", pageable.getPageNumber(), pageable.getPageSize());
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> getOrdersByCustomerId(String customerId, Pageable pageable) {
        log.debug("Fetching orders for customer ID: {} (page: {}, size: {})", 
                customerId, pageable.getPageNumber(), pageable.getPageSize());
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(String status, Pageable pageable) {
        log.debug("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersNeedingStatusUpdates(List<String> statuses) {
        log.debug("Finding orders needing status update with statuses: {}", statuses);
        if (statuses == null || statuses.isEmpty()) {
            return Collections.emptyList();
        }
        return orderRepository.findByStatusIn(statuses);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        log.info("Updating status for order ID: {} to {}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Updated order ID: {} status to: {}", orderId, status);
        
        return updatedOrder;
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        order.setStatus("CANCELLED");
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + 
                "Cancelled on " + LocalDateTime.now() + ": " + reason);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order cancelledOrder = orderRepository.save(order);
        log.info("Cancelled order ID: {}", orderId);
        
        return cancelledOrder;
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        log.info("Deleting order with ID: {}", orderId);
        
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found with ID: " + orderId);
        }
        
        orderRepository.deleteById(orderId);
        log.info("Deleted order with ID: {}", orderId);
    }

    @Override
    public boolean orderExists(String orderReference) {
        return orderRepository.existsByOrderReference(orderReference);
    }

    @Override
    public List<Order> findOrdersNeedingStatusUpdate(List<String> statuses) {
        log.debug("Finding orders needing status update with statuses: {}", statuses);
        return orderRepository.findOrdersNeedingStatusUpdate(statuses);
    }

    /**
     * Validates the order before saving.
     *
     * @param order the order to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (order.getOrderReference() == null || order.getOrderReference().trim().isEmpty()) {
            throw new IllegalArgumentException("Order reference is required");
        }
        
        if (order.getCustomerId() == null || order.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        
        // Validate order items
        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("Product ID is required for all order items");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for all order items");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price must be zero or greater for all order items");
            }
        }
    }

    /**
     * Calculates and sets the total amount for the order.
     *
     * @param order the order to calculate the total for
     */
    private void calculateOrderTotal(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            order.setTotalAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        
        // Calculate item totals
        for (OrderItem item : order.getItems()) {
            BigDecimal itemTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
                    
            // Subtract discount if any
            if (item.getDiscountAmount() != null) {
                itemTotal = itemTotal.subtract(item.getDiscountAmount());
            }
            
            // Add tax if any
            if (item.getTaxAmount() != null) {
                itemTotal = itemTotal.add(item.getTaxAmount());
            }
            
            // Ensure total is not negative
            itemTotal = itemTotal.max(BigDecimal.ZERO);
            
            // Set item total
            item.setTotalPrice(itemTotal);
            
            // Add to order total
            total = total.add(itemTotal);
        }
        
        // Set order total
        order.setTotalAmount(total);
    }
}

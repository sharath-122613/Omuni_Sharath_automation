package com.cloud.omuni_cloud.service.impl;

import com.cloud.omuni_cloud.exception.ResourceNotFoundException;
import com.cloud.omuni_cloud.mapper.OrderMapper;
import com.cloud.omuni_cloud.model.dto.OrderDto;
import com.cloud.omuni_cloud.model.entity.Order;
import com.cloud.omuni_cloud.repository.OrderRepository;
import com.cloud.omuni_cloud.service.OrderDtoService;
import com.cloud.omuni_cloud.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link OrderDtoService} for managing orders with DTOs.
 */
@Service
@RequiredArgsConstructor
public class OrderDtoServiceImpl implements OrderDtoService {
    private static final Logger log = LoggerFactory.getLogger(OrderDtoServiceImpl.class);

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        log.info("Creating new order with reference: {}", orderDto.getOrderReference());
        
        // Map DTO to entity
        Order order = orderMapper.toEntity(orderDto);
        
        // Save the order using the entity service
        Order savedOrder = orderService.createOrder(order);
        
        // Map back to DTO and return
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderDto updateOrder(Long id, OrderDto orderDto) {
        log.info("Updating order with ID: {}", id);
        
        // Check if order exists
        Order existingOrder = orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
        
        // Update the existing order with new values from DTO
        orderMapper.updateOrderFromDto(orderDto, existingOrder);
        
        // Save the updated order
        Order updatedOrder = orderService.updateOrder(existingOrder);
        
        // Map back to DTO and return
        return orderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);
        
        return orderService.getOrderById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderByReference(String orderReference) {
        log.debug("Fetching order with reference: {}", orderReference);
        
        return orderService.getOrderByReference(orderReference)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with reference: " + orderReference));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders (page: {}, size: {})", pageable.getPageNumber(), pageable.getPageSize());
        
        return orderService.getAllOrders(pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByCustomerId(String customerId, Pageable pageable) {
        log.debug("Fetching orders for customer ID: {} (page: {}, size: {})", 
                customerId, pageable.getPageNumber(), pageable.getPageSize());
                
        return orderService.getOrdersByCustomerId(customerId, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByStatus(String status, Pageable pageable) {
        log.debug("Fetching orders with status: {} (page: {}, size: {})", 
                status, pageable.getPageNumber(), pageable.getPageSize());
                
        return orderService.getOrdersByStatus(status, pageable)
                .map(orderMapper::toDto);
    }

    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, String status) {
        log.info("Updating status for order ID: {} to {}", orderId, status);
        
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return orderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        log.info("Deleting order with ID: {}", orderId);
        orderService.deleteOrder(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> findOrdersNeedingStatusUpdates(List<String> statuses) {
        log.debug("Finding orders needing status update with statuses: {}", statuses);
        
        return orderService.findOrdersNeedingStatusUpdates(statuses).stream()
                .map(orderMapper::toDto)
                .collect(java.util.stream.Collectors.toList());
    }
}

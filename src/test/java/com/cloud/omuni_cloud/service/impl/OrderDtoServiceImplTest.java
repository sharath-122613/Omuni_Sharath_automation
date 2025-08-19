package com.cloud.omuni_cloud.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.cloud.omuni_cloud.exception.ResourceNotFoundException;
import com.cloud.omuni_cloud.mapper.OrderMapper;
import com.cloud.omuni_cloud.model.dto.OrderDto;
import com.cloud.omuni_cloud.model.entity.Order;
import com.cloud.omuni_cloud.repository.OrderRepository;
import com.cloud.omuni_cloud.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrderDtoServiceImplTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderDtoServiceImpl orderDtoService;

    private Order order;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        // Create a test order
        order = new Order();
        order.setId(1L);
        order.setOrderReference("TEST-123");
        order.setCustomerId("CUST-001");
        order.setStatus("PENDING");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Create a test DTO
        orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setOrderReference("TEST-123");
        orderDto.setCustomerId("CUST-001");
        orderDto.setStatus("PENDING");
        orderDto.setTotalAmount(new BigDecimal("100.00"));
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        // Arrange
        when(orderMapper.toEntity(any(OrderDto.class))).thenReturn(order);
        when(orderService.createOrder(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        // Act
        OrderDto result = orderDtoService.createOrder(orderDto);

        // Assert
        assertNotNull(result);
        assertEquals(orderDto.getOrderReference(), result.getOrderReference());
        assertEquals(orderDto.getCustomerId(), result.getCustomerId());
        verify(orderService, times(1)).createOrder(any(Order.class));
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        // Act
        OrderDto result = orderDtoService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(orderDto.getId(), result.getId());
        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void getOrderById_WhenOrderNotExists_ShouldThrowException() {
        // Arrange
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderDtoService.getOrderById(999L);
        });
        verify(orderService, times(1)).getOrderById(999L);
    }

    @Test
    void updateOrder_WhenOrderExists_ShouldUpdateAndReturnOrder() {
        // Arrange
        OrderDto updatedDto = new OrderDto();
        updatedDto.setStatus("COMPLETED");

        when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));
        when(orderService.updateOrder(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any(Order.class))).thenReturn(updatedDto);

        // Act
        OrderDto result = orderDtoService.updateOrder(1L, updatedDto);

        // Assert
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(orderService, times(1)).updateOrder(any(Order.class));
    }

    @Test
    void deleteOrder_ShouldCallServiceDelete() {
        // Arrange
        doNothing().when(orderService).deleteOrder(1L);

        // Act
        orderDtoService.deleteOrder(1L);

        // Assert
        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    void getAllOrders_ShouldReturnPageOfOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order), pageable, 1);
        when(orderService.getAllOrders(pageable)).thenReturn(orderPage);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        // Act
        Page<OrderDto> result = orderDtoService.getAllOrders(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderService, times(1)).getAllOrders(pageable);
    }

    @Test
    void updateOrderStatus_WhenOrderExists_ShouldUpdateStatus() {
        // Arrange
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setStatus("SHIPPED");

        OrderDto updatedDto = new OrderDto();
        updatedDto.setStatus("SHIPPED");

        when(orderService.updateOrderStatus(1L, "SHIPPED")).thenReturn(updatedOrder);
        when(orderMapper.toDto(any(Order.class))).thenReturn(updatedDto);

        // Act
        OrderDto result = orderDtoService.updateOrderStatus(1L, "SHIPPED");

        // Assert
        assertNotNull(result);
        assertEquals("SHIPPED", result.getStatus());
        verify(orderService, times(1)).updateOrderStatus(1L, "SHIPPED");
    }

    @Test
    void findOrdersNeedingStatusUpdates_ShouldReturnMatchingOrders() {
        // Arrange
        List<Order> orders = Collections.singletonList(order);
        List<String> statuses = Collections.singletonList("PENDING");
        
        when(orderService.findOrdersNeedingStatusUpdates(statuses)).thenReturn(orders);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        // Act
        List<OrderDto> result = orderDtoService.findOrdersNeedingStatusUpdates(statuses);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(orderService, times(1)).findOrdersNeedingStatusUpdates(statuses);
    }
}

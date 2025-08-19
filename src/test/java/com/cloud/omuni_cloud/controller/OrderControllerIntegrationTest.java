package com.cloud.omuni_cloud.controller;

import com.cloud.omuni_cloud.config.OrderControllerTestMinimalConfig;
import com.cloud.omuni_cloud.model.dto.OrderDto;
import com.cloud.omuni_cloud.service.OrderDtoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.cloud.omuni_cloud.model.dto.OrderItemDto;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(OrderControllerTestMinimalConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.main.web-application-type=servlet"
})
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderDtoService orderDtoService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        // If you need to customize the MockMvc instance, uncomment and modify the following:
        /*
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        */
        
        // Reset all mocks before each test
        Mockito.reset(orderDtoService);
    }

    @Test
    @WithMockUser
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        when(orderDtoService.createOrder(any(OrderDto.class))).thenReturn(orderDto);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderReference", is("TEST-123")));

        verify(orderDtoService, times(1)).createOrder(any(OrderDto.class));
    }

    @Test
    @WithMockUser
    void getOrderById_ShouldReturnOrder() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        when(orderDtoService.getOrderById(1L)).thenReturn(orderDto);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.orderReference", is("TEST-123")));

        verify(orderDtoService, times(1)).getOrderById(1L);
    }

    @Test
    @WithMockUser
    void updateOrder_ShouldUpdateAndReturnOrder() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        orderDto.setStatus("UPDATED");
        when(orderDtoService.updateOrder(anyLong(), any(OrderDto.class))).thenReturn(orderDto);

        // Act & Assert
        mockMvc.perform(put("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UPDATED")));

        verify(orderDtoService, times(1)).updateOrder(eq(1L), any(OrderDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Assuming delete requires admin role
    void deleteOrder_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/orders/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(orderDtoService, times(1)).deleteOrder(1L);
    }

    @Test
    @WithMockUser
    void getAllOrders_ShouldReturnPageOfOrders() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        Page<OrderDto> page = new PageImpl<>(Collections.singletonList(orderDto));
        when(orderDtoService.getAllOrders(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].orderReference", is("TEST-123")));

        verify(orderDtoService, times(1)).getAllOrders(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Assuming status update requires admin role
    void updateOrderStatus_ShouldUpdateStatus() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        orderDto.setStatus("SHIPPED");
        when(orderDtoService.updateOrderStatus(1L, "SHIPPED")).thenReturn(orderDto);

        // Act & Assert
        mockMvc.perform(patch("/api/orders/1/status/SHIPPED")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHIPPED")));

        verify(orderDtoService, times(1)).updateOrderStatus(1L, "SHIPPED");
    }

    @Test
    @WithMockUser
    void getOrdersByCustomerId_ShouldReturnCustomerOrders() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        Page<OrderDto> page = new PageImpl<>(Collections.singletonList(orderDto));
        when(orderDtoService.getOrdersByCustomerId(eq("CUST-001"), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders/customer/CUST-001")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].customerId", is("CUST-001")));

        verify(orderDtoService, times(1)).getOrdersByCustomerId(eq("CUST-001"), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void getOrdersByStatus_ShouldReturnFilteredOrders() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        Page<OrderDto> page = new PageImpl<>(Collections.singletonList(orderDto));
        when(orderDtoService.getOrdersByStatus(eq("PENDING"), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders/status/PENDING")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status", is("PENDING")));

        verify(orderDtoService, times(1)).getOrdersByStatus(eq("PENDING"), any(Pageable.class));
    }

    @Test
    void findOrdersNeedingStatusUpdates_ShouldReturnMatchingOrders() throws Exception {
        // Arrange
        OrderDto orderDto = createTestOrderDto();
        when(orderDtoService.findOrdersNeedingStatusUpdates(anyList()))
                .thenReturn(Collections.singletonList(orderDto));

        // Act & Assert
        mockMvc.perform(get("/api/orders/pending-updates")
                .param("statuses", "PENDING,PROCESSING")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderReference", is("TEST-123")));

        verify(orderDtoService, times(1)).findOrdersNeedingStatusUpdates(anyList());
    }

    private OrderDto createTestOrderDto() {
        OrderDto orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setOrderReference("TEST-123");
        orderDto.setCustomerId("CUST-001");
        orderDto.setStatus("PENDING");
        orderDto.setTotalAmount(new BigDecimal("100.00"));
        orderDto.setCurrency("INR");
        orderDto.setFcId("FC-001");
        orderDto.setSource("WEB");
        orderDto.setOrderDate(LocalDateTime.now());
        
        // Create and add order items with all required fields
        OrderItemDto item1 = new OrderItemDto();
        item1.setId(1L);
        item1.setProductId("PROD-001");
        item1.setProductName("Test Product");
        item1.setSku("SKU-001");
        item1.setEan("1234567890123");
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("50.00"));
        item1.setDiscountAmount(BigDecimal.ZERO);
        item1.setTaxAmount(new BigDecimal("18.00"));
        item1.setTotalPrice(new BigDecimal("100.00"));
        item1.setStatus("PENDING");
        
        orderDto.setItems(Collections.singletonList(item1));
        
        return orderDto;
    }
}

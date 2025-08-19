package com.cloud.omuni_cloud.controller;

import com.cloud.omuni_cloud.model.dto.OrderDto;
import com.cloud.omuni_cloud.service.OrderDtoService;
import com.cloud.omuni_cloud.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing orders.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "APIs for managing orders")
public class OrderController {

    private final OrderDtoService orderDtoService;
    private final OrderMapper orderMapper;

    @PostMapping
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order with the provided details",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Order created successfully",
                content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input"
            )
        }
    )
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody OrderDto orderDto) {
        OrderDto createdOrder = orderDtoService.createOrder(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves an order by its unique identifier",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Order found",
                content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Order not found"
            )
        }
    )
    public ResponseEntity<OrderDto> getOrderById(
            @Parameter(description = "ID of the order to be obtained. Cannot be empty.", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(orderDtoService.getOrderById(id));
    }

    @GetMapping("/reference/{orderReference}")
    @Operation(
        summary = "Get order by reference",
        description = "Retrieves an order by its reference number",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Order found",
                content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Order not found"
            )
        }
    )
    public ResponseEntity<OrderDto> getOrderByReference(
            @Parameter(description = "Reference number of the order to be retrieved", required = true)
            @PathVariable String orderReference) {
        return ResponseEntity.ok(orderDtoService.getOrderByReference(orderReference));
    }

    @GetMapping
    @Operation(
        summary = "Get all orders",
        description = "Retrieves a paginated list of all orders",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Orders retrieved successfully",
                content = @Content(schema = @Schema(implementation = OrderDto.class))
            )
        }
    )
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @Parameter(description = "Pagination information") 
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderDtoService.getAllOrders(pageable));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(
        summary = "Get orders by customer ID",
        description = "Retrieves all orders for a specific customer with pagination",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Customer's orders retrieved successfully",
                content = @Content(schema = @Schema(implementation = OrderDto.class))
            )
        }
    )
    public ResponseEntity<Page<OrderDto>> getOrdersByCustomerId(
            @Parameter(description = "ID of the customer", required = true) 
            @PathVariable String customerId,
            @Parameter(description = "Pagination information") 
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderDtoService.getOrdersByCustomerId(customerId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(
        summary = "Get orders by status",
        description = "Retrieves all orders with a specific status with pagination",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of orders with the specified status"
            )
        }
    )
    public ResponseEntity<Page<OrderDto>> getOrdersByStatus(
            @Parameter(description = "Status to filter by") 
            @PathVariable String status,
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderDtoService.getOrdersByStatus(status, pageable));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update an order",
        description = "Updates an existing order with the provided details",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Order updated successfully",
                content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Order not found"
            )
        }
    )
    public ResponseEntity<OrderDto> updateOrder(
            @Parameter(description = "ID of the order to be updated", required = true)
            @PathVariable Long id,
            @Valid @RequestBody OrderDto orderDto) {
        orderDto.setId(id);
        return ResponseEntity.ok(orderDtoService.updateOrder(id, orderDto));
    }

    @PatchMapping("/{id}/status/{status}")
    @Operation(
        summary = "Update order status",
        description = "Updates the status of an existing order",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Order status updated successfully",
                content = @Content(schema = @Schema(implementation = OrderDto.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Order not found"
            )
        }
    )
    public ResponseEntity<OrderDto> updateOrderStatus(
            @Parameter(description = "ID of the order", required = true) 
            @PathVariable Long id,
            @Parameter(description = "New status for the order", required = true) 
            @PathVariable String status) {
        return ResponseEntity.ok(orderDtoService.updateOrderStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete an order",
        description = "Deletes an order by its ID",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "Order deleted successfully"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Order not found"
            )
        }
    )
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "ID of the order to be deleted", required = true) 
            @PathVariable Long id) {
        orderDtoService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending-updates")
    @Operation(
        summary = "Get orders needing status updates",
        description = "Retrieves a list of orders that need status updates from the external system",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of orders needing status updates",
                content = @Content(schema = @Schema(implementation = OrderDto.class))
            )
        }
    )
    public ResponseEntity<List<OrderDto>> getOrdersNeedingStatusUpdates(
            @Parameter(description = "List of order references to check for updates")
            @RequestParam(required = false) List<String> orderReferences) {
        return ResponseEntity.ok(orderDtoService.findOrdersNeedingStatusUpdates(orderReferences));
    }
}

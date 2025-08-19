package com.cloud.omuni_cloud.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Order entity.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {

    private Long id;

    @NotBlank(message = "Order reference is required")
    @Size(max = 50, message = "Order reference must be less than 50 characters")
    private String orderReference;

    @NotBlank(message = "Customer ID is required")
    @Size(max = 50, message = "Customer ID must be less than 50 characters")
    private String customerId;

    @NotBlank(message = "Status is required")
    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status;

    @DecimalMin(value = "0.0", inclusive = true, message = "Total amount must be zero or greater")
    private BigDecimal totalAmount;

    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency = "INR";

    @Size(max = 50, message = "FC ID must be less than 50 characters")
    private String fcId;

    @Size(max = 50, message = "Source must be less than 50 characters")
    private String source;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryDate;

    @Valid
    @NotEmpty(message = "At least one order item is required")
    private List<OrderItemDto> items = new ArrayList<>();

    @Size(max = 1000, message = "Shipping address must be less than 1000 characters")
    private String shippingAddress;

    @Size(max = 1000, message = "Billing address must be less than 1000 characters")
    private String billingAddress;

    @Size(max = 50, message = "Payment method must be less than 50 characters")
    private String paymentMethod;

    @Size(max = 50, message = "Payment status must be less than 50 characters")
    private String paymentStatus;

    @Size(max = 50, message = "Shipping method must be less than 50 characters")
    private String shippingMethod;

    @Size(max = 100, message = "Tracking number must be less than 100 characters")
    private String trackingNumber;

    @Size(max = 2000, message = "Notes must be less than 2000 characters")
    private String notes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Explicit setter for id to resolve compilation issues
    public void setId(Long id) {
        this.id = id;
    }
}

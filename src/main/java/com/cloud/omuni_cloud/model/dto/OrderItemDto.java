package com.cloud.omuni_cloud.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Data Transfer Object for OrderItem entity.
 */
@Data
public class OrderItemDto {

    private Long id;

    @NotBlank(message = "Product ID is required")
    @Size(max = 50, message = "Product ID must be less than 50 characters")
    private String productId;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must be less than 255 characters")
    private String productName;

    @Size(max = 100, message = "SKU must be less than 100 characters")
    private String sku;

    @Size(max = 50, message = "EAN must be less than 50 characters")
    private String ean;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Unit price must be zero or greater")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Discount amount must be zero or greater")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Tax amount must be zero or greater")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Total price must be zero or greater")
    private BigDecimal totalPrice;

    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status;

    @Size(max = 255, message = "Return reason must be less than 255 characters")
    private String returnReason;

    @Size(max = 2000, message = "Notes must be less than 2000 characters")
    private String notes;
}

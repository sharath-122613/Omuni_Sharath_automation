package com.cloud.omuni_cloud.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an item within an order.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class OrderItem extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(OrderItem.class);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "sku", length = 100)
    private String sku;

    @Column(name = "ean", length = 50)
    private String ean;
    
    // Explicit getters and setters to ensure they're available during compilation
    public Long getId() {
        return super.getId();
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public String getProductId() {
        return productId;
    }
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public String getEan() {
        return ean;
    }
    
    public void setEan(String ean) {
        this.ean = ean;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        calculateTotalPrice();
    }

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "status", length = 50)
    private String status = "PENDING";

    @Column(name = "return_reason", length = 255)
    private String returnReason;

    @Column(name = "is_returned")
    private Boolean isReturned = false;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
    

    
    public Integer getQuantity() {
        return quantity != null ? quantity : 0;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity != null ? quantity : 0;
        calculateTotalPrice();
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice != null ? totalPrice : BigDecimal.ZERO;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice != null ? totalPrice.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount != null ? discountAmount : BigDecimal.ZERO;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount != null ? discountAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        calculateTotalPrice();
    }
    
    public BigDecimal getTaxAmount() {
        return taxAmount != null ? taxAmount : BigDecimal.ZERO;
    }
    
    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount != null ? taxAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        calculateTotalPrice();
    }
    
    /**
     * Calculates the total price of this order item based on quantity, unit price, discount, and tax.
     * The result is stored in the totalPrice field.
     */
    /**
     * Calculates the total price of this order item based on quantity, unit price, discount, and tax.
     * The result is stored in the totalPrice field.
     */
    public void calculateTotalPrice() {
        if (getUnitPrice() == null || getQuantity() == null || getQuantity() <= 0) {
            this.totalPrice = BigDecimal.ZERO;
            return;
        }
        
        try {
            BigDecimal basePrice = getUnitPrice().multiply(BigDecimal.valueOf(getQuantity()));
            BigDecimal discountedPrice = basePrice.subtract(getDiscountAmount());
            BigDecimal finalPrice = discountedPrice.add(getTaxAmount());
            
            this.totalPrice = finalPrice.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Error calculating total price for order item: {}", e.getMessage(), e);
            this.totalPrice = BigDecimal.ZERO;
        }
    }
    
    /**
     * Updates the status of this order item.
     * @param newStatus The new status to set
     */
    public void updateStatus(String newStatus) {
        String oldStatus = this.status;
        this.status = newStatus != null ? newStatus : "PENDING";
        this.updatedAt = LocalDateTime.now();
        log.info("OrderItem {} status changed from {} to {}", this.getId(), oldStatus, newStatus);
    }
    
    /**
     * Updates the quantity of this order item and recalculates the total price.
     * @param newQuantity The new quantity
     * @throws IllegalArgumentException if newQuantity is less than or equal to zero
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        setQuantity(newQuantity);
    }
    
    /**
     * Applies a discount to this order item.
     * @param discount The discount amount to apply
     * @throws IllegalArgumentException if discount is negative
     */
    public void applyDiscount(BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount must be a non-negative value");
        }
        setDiscountAmount(discount);
    }
    
    /**
     * Applies tax to this order item.
     * @param tax The tax amount to apply
     * @throws IllegalArgumentException if tax is negative
     */
    public void applyTax(BigDecimal tax) {
        if (tax == null || tax.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tax must be a non-negative value");
        }
        setTaxAmount(tax);
    }
    
    @PrePersist
    protected void onCreate() {
        calculateTotalPrice();
    }
    
    @PreUpdate
    protected void onUpdate() {
        calculateTotalPrice();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(getId(), orderItem.getId()) &&
               Objects.equals(productId, orderItem.productId) &&
               Objects.equals(order, orderItem.order);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), productId, order);
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + getId() +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                '}';
    }
}

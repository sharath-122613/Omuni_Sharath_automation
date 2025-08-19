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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an order in the system.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Order extends BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(Order.class);
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "order_reference", nullable = false, unique = true, length = 50)
    private String orderReference;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "currency", length = 3)
    private String currency = "INR";
    
    // Explicit getters and setters to ensure they're available during compilation
    public Long getId() {
        return super.getId();
    }
    
    public String getOrderReference() {
        return orderReference;
    }
    

    public String getCustomerId() {
        return customerId;
    }
    

    public List<OrderItem> getItems() {
        return items;
    }
    

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount != null ? totalAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    

    public String getCurrency() {
        return currency;
    }
    

    @Column(name = "fc_id", length = 50)
    private String fcId;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();
    
    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;
    
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;
    
    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;
    
    @Column(name = "payment_status", length = 50)
    private String paymentStatus;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "shipping_method", length = 50)
    private String shippingMethod;
    
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;
    
    @Column(name = "carrier", length = 50)
    private String carrier;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
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

    /**
     * Add an item to the order.
     *
     * @param item the item to add
     */
    public void addItem(OrderItem item) {
        if (item != null) {
            items.add(item);
            item.setOrder(this);
            calculateTotalAmount();
        }
    }

    /**
     * Remove an item from the order.
     *
     * @param item the item to remove
     */
    public void removeItem(OrderItem item) {
        if (item != null) {
            items.remove(item);
            item.setOrder(null);
            calculateTotalAmount();
        }
    }

    /**
     * Clear all items from the order.
     */
    public void clearItems() {
        items.forEach(item -> item.setOrder(null));
        items.clear();
        totalAmount = BigDecimal.ZERO;
    }

    /**
     * Calculate the total amount of the order based on its items.
     */
    public void calculateTotalAmount() {
        if (items != null && !items.isEmpty()) {
            totalAmount = items.stream()
                    .map(OrderItem::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            totalAmount = BigDecimal.ZERO;
        }
    }

    /**
     * Update the status of the order.
     *
     * @param newStatus the new status
     */
    public void updateStatus(String newStatus) {
        if (newStatus != null && !newStatus.isEmpty()) {
            this.status = newStatus;
            this.updatedAt = LocalDateTime.now();
            log.info("Order {} status updated to: {}", orderReference, newStatus);
        }
    }

    /**
     * Add a note to the order.
     *
     * @param note the note to add
     */
    public void addNote(String note) {
        if (note != null && !note.trim().isEmpty()) {
            if (this.notes == null || this.notes.isEmpty()) {
                this.notes = note;
            } else {
                this.notes = String.format("%s\n%s", this.notes, note);
            }
            this.updatedAt = LocalDateTime.now();
            log.info("Note added to order {}: {}", orderReference, note);
        }
    }

    /**
     * Order status values
     */
    public enum OrderStatus {
        PENDING,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED,
        COMPLETED,
        FAILED
    }

    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.PENDING.name();
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        if (currency == null) {
            currency = "INR";
        }
        calculateTotalAmount();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotalAmount();
    }


    
    public String getFcId() {
        return fcId;
    }
    
    public void setFcId(String fcId) {
        this.fcId = fcId;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public LocalDateTime getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public String getCarrier() {
        return carrier;
    }
    
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getShippingMethod() {
        return shippingMethod;
    }
    
    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id) &&
               Objects.equals(orderReference, order.orderReference) &&
               Objects.equals(customerId, order.customerId) &&
               Objects.equals(status, order.status) &&
               Objects.equals(totalAmount, order.totalAmount) &&
               Objects.equals(currency, order.currency) &&
               Objects.equals(fcId, order.fcId) &&
               Objects.equals(source, order.source) &&
               Objects.equals(orderDate, order.orderDate) &&
               Objects.equals(deliveryDate, order.deliveryDate) &&
               Objects.equals(shippingAddress, order.shippingAddress) &&
               Objects.equals(billingAddress, order.billingAddress) &&
               Objects.equals(paymentMethod, order.paymentMethod) &&
               Objects.equals(paymentStatus, order.paymentStatus) &&
               Objects.equals(shippingMethod, order.shippingMethod) &&
               Objects.equals(trackingNumber, order.trackingNumber) &&
               Objects.equals(carrier, order.carrier) &&
               Objects.equals(notes, order.notes) &&
               Objects.equals(createdAt, order.createdAt) &&
               Objects.equals(updatedAt, order.updatedAt) &&
               Objects.equals(createdBy, order.createdBy) &&
               Objects.equals(updatedBy, order.updatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderReference, customerId, status, totalAmount, currency, fcId, source, 
                          orderDate, deliveryDate, shippingAddress, billingAddress, paymentMethod, 
                          paymentStatus, shippingMethod, trackingNumber, carrier, notes, createdAt, 
                          updatedAt, createdBy, updatedBy, items);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderReference='" + orderReference + '\'' +
                ", customerId='" + customerId + '\'' +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", currency='" + currency + '\'' +
                ", fcId='" + fcId + '\'' +
                ", source='" + source + '\'' +
                ", orderDate=" + orderDate +
                ", deliveryDate=" + deliveryDate +
                ", shippingAddress='" + shippingAddress + '\'' +
                ", billingAddress='" + billingAddress + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", shippingMethod='" + shippingMethod + '\'' +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", carrier='" + carrier + '\'' +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", items=" + items.size() +
                '}';
    }
}

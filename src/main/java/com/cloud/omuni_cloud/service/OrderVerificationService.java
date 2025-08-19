package com.cloud.omuni_cloud.service;

import com.cloud.omuni_cloud.entity.SaleOrder;
import com.cloud.omuni_cloud.entity.StoreOrder;
import com.cloud.omuni_cloud.repository.SaleOrderRepository;
import com.cloud.omuni_cloud.repository.StoreOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderVerificationService {
    private final SaleOrderRepository saleOrderRepository;
    private final StoreOrderRepository storeOrderRepository;

    /**
     * Verifies a sale call in Chandler database for Bata orders
     * @param orderNo The order number to verify
     * @return A formatted string with the sale order details or an error message
     */
    public String verifySaleCallInChandlerDBforBataOrders(String orderNo) {
        try {
            Optional<SaleOrder> saleOrderOpt = saleOrderRepository.findFirstByOrderNoOrderByIdDesc(orderNo);
            
            return saleOrderOpt.map(order -> 
                String.format("Sale order found - ID: %s, Status: %s, Total: %s", 
                    order.getId(),
                    order.getStatus() != null ? order.getStatus() : "N/A",
                    order.getTotal() != null ? order.getTotal() : 
                        (order.getAmount() != null ? order.getAmount() : "N/A"))
            ).orElse("[WARNING] No sale order found with orderNo: " + orderNo);
            
        } catch (Exception e) {
            log.error("Error verifying sale order: {}", e.getMessage(), e);
            return "[ERROR] Failed to verify sale order: " + e.getMessage().split("\\n")[0];
        }
    }

    /**
     * Verifies a booking call in Chandler database for Bata orders
     * @param orderId The order ID to verify
     * @return A formatted string with the store order details or an error message
     */
    public String verifyBookingCallInChandlerDBforBataOrders(String orderId) {
        try {
            Optional<StoreOrder> storeOrderOpt = storeOrderRepository.findFirstByOrderIdOrderByIdDesc(orderId);
            
            return storeOrderOpt.map(order -> 
                String.format("Store order found - ID: %s, Status: %s", 
                    order.getId(),
                    order.getStatus() != null ? order.getStatus() : "N/A")
            ).orElse("[WARNING] No store order found with orderId: " + orderId);
            
        } catch (Exception e) {
            log.error("Error verifying store order: {}", e.getMessage(), e);
            return "[ERROR] Failed to verify store order: " + e.getMessage().split("\\n")[0];
        }
    }
}

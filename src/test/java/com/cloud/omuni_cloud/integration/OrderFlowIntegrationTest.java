package com.cloud.omuni_cloud.integration;

import com.cloud.omuni_cloud.api.GenericDetailsApi;
import com.cloud.omuni_cloud.api.InventoryApi;
import com.cloud.omuni_cloud.api.OrderCreationApi;
import com.cloud.omuni_cloud.base.BaseIntegrationTest;
import com.cloud.omuni_cloud.dbutil.DatabaseManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class OrderFlowIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testCompleteOrderFlow() throws Exception {
        try {
            // Step 1: Update inventory to 0
            logToReport("[INFO] Step 1: Update inventory to 0");
            String response0 = InventoryApi.updateInventory(AUTH_TOKEN, FC_ID, EAN, 0);
            logToReport("[INFO] Inventory update (0) response: " + response0);

            // Add a small delay to ensure inventory is updated
            Thread.sleep(2000);

            // Step 2: Update inventory to 1500
            logToReport("[INFO] Step 2: Update inventory to 1500");
            String response1500 = InventoryApi.updateInventory(AUTH_TOKEN, FC_ID, EAN, 1500);
            logToReport("[INFO] Inventory update (1500) response: " + response1500);
            
            // Add a small delay to ensure inventory is updated
            Thread.sleep(2000);

            // Step 3: Create order
            String orderReference = generateOrderReference();
            logToReport("[INFO] Step 3: Creating order with reference: " + orderReference);
            String orderResponse = OrderCreationApi.createOrder(FC_ID, orderReference, EAN, 1);
            logToReport("[INFO] Order creation response: " + orderResponse);

            // Step 4: Verify order in database
            logToReport("[INFO] Step 4: Verifying order in database");
            verifyOrderInDatabase(orderReference);

            // Step 5: Get consignment ID from order
            logToReport("[INFO] Step 5: Getting consignment ID");
            String consignmentId = getConsignmentId(orderReference);
            logToReport("[INFO] Consignment ID: " + consignmentId);

            // Step 6: Verify initial consignment status is 'Created'
            logToReport("[INFO] Step 6: Verifying initial consignment status");
            verifyConsignmentStatus(consignmentId, "Created");

            // Step 7: Update consignment status to 'Packed'
            logToReport("[INFO] Step 7: Updating consignment status to 'Packed'");
            String statusUpdateResponse = GenericDetailsApi.updateConsignmentStatus(
                AUTH_TOKEN, consignmentId, "PACKED", "Auto change by System", 7);
            logToReport("[INFO] Consignment status update response: " + statusUpdateResponse);

            // Step 8: Verify consignment status is now 'Packed'
            verifyConsignmentStatus(consignmentId, "Packed");
            
            // Step 9: Update consignment status to 'Invoiced'
            logToReport("[INFO] Step 9: Updating consignment status to 'Invoiced'");
            statusUpdateResponse = GenericDetailsApi.updateConsignmentStatus(
                AUTH_TOKEN, consignmentId, "INVOICED", "Auto change by System", 7);
            logToReport("[INFO] Consignment status update response: " + statusUpdateResponse);

            // Step 10: Verify sale call in Chandler DB after invoicing
            logToReport("[INFO] Step 10: Verifying sale in Chandler DB");
            verifySaleInChandlerDB(orderReference);
            
            logToReport("[SUCCESS] Order flow test completed successfully!");
            
        } catch (Exception e) {
            logToReport("[ERROR] Test failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private void verifyBookingInChandlerDB(String orderReference) throws SQLException {
        logToReport("[INFO] Verifying booking call in Chandler DB...");
        try (DatabaseManager dbManager = new DatabaseManager("nickfury")) {
            // Implementation of booking verification would go here
            // For now, we'll just log that we would verify the booking
            logToReport("[INFO] Booking verification would be done here for order: " + orderReference);
        } catch (SQLException e) {
            logToReport("[ERROR] Failed to verify booking call in Chandler DB: " + e.getMessage());
            throw e;
        }
    }
    
    private void verifyOrderInDatabase(String orderReference) throws SQLException {
        logToReport("[INFO] Verifying order " + orderReference + " in database...");
        
        try (DatabaseManager dbManager = new DatabaseManager("nickfury")) {
            String query = "SELECT * FROM sale_orders WHERE orderNo = ?";
            List<Map<String, Object>> results = dbManager.executeQuery(query, orderReference);
            
            if (results.isEmpty()) {
                throw new AssertionError("Order " + orderReference + " not found in database");
            }
            
            logToReport("[INFO] Order found in database: " + results.get(0).toString());
        } catch (Exception e) {
            logToReport("[ERROR] Error verifying order in database: " + e.getMessage());
            throw e;
        }
    }
    
    private String getConsignmentId(String orderReference) throws Exception {
        logToReport("[INFO] Getting consignment ID for order " + orderReference);
        
        // In a real implementation, you would query the database or API to get the consignment ID
        // For now, we'll simulate this by returning a formatted string
        // In a real scenario, you would do something like:
        // DatabaseManager dbManager = new DatabaseManager("nickfury");
        // String query = "SELECT consignment_id FROM orders WHERE order_reference = ?";
        // Map<String, Object> result = dbManager.queryForMap(query, orderReference);
        // return result.get("consignment_id").toString();
        
        String consignmentId = "CONS-" + orderReference;
        logToReport("[INFO] Generated consignment ID: " + consignmentId);
        return consignmentId;
    }
    
    private void verifyConsignmentStatus(String consignmentId, String expectedStatus) throws Exception {
        logToReport(String.format("[INFO] Verifying consignment status is '%s'...", expectedStatus));
        
        try {
            // Get current status from the API
            String status = GenericDetailsApi.getConsignmentStatus(AUTH_TOKEN, consignmentId);
            logToReport("[INFO] Current ConsignmentStatus for " + consignmentId + ": " + status);
            
            // Compare statuses case-insensitively
            if (status == null || !expectedStatus.equalsIgnoreCase(status)) {
                String errorMsg = String.format("Expected status '%s' but got '%s'", expectedStatus, status);
                logToReport("[ERROR] " + errorMsg);
                throw new AssertionError(errorMsg);
            }
            
            logToReport("[INFO] Consignment status verified successfully");
        } catch (Exception e) {
            logToReport("[ERROR] Error verifying consignment status: " + e.getMessage());
            throw e;
        }
    }
    
    private void verifySaleInChandlerDB(String orderReference) throws SQLException {
        logToReport("[INFO] Verifying sale in Chandler DB for order " + orderReference);
        
        try (DatabaseManager dbManager = new DatabaseManager("chandler")) {
            String query = "SELECT * FROM sales WHERE order_id = ?";
            List<Map<String, Object>> results = dbManager.executeQuery(query, orderReference);
            
            if (results.isEmpty()) {
                throw new AssertionError("Sale for order " + orderReference + " not found in Chandler DB");
            }
            
            logToReport("[INFO] Sale found in Chandler DB: " + results.get(0).toString());
        } catch (Exception e) {
            logToReport("[ERROR] Error verifying sale in Chandler DB: " + e.getMessage());
            throw e;
        }
    }
}

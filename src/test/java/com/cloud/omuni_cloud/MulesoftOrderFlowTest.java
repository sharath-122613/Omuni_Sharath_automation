package com.cloud.omuni_cloud;

import com.cloud.omuni_cloud.config.DatabaseTestConfig;
import com.cloud.omuni_cloud.dbutil.DatabaseManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.cloud.omuni_cloud.config.TestConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@SpringBootTest(classes = {DatabaseTestConfig.class, TestConfig.class})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class MulesoftOrderFlowTest {
    
    @Autowired
    private DatabaseManager databaseManager;
    private static final String AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJlbWFpbF9pZCI6ImluY3JlZmZAZ21haWwuY29tIiwiYXVkIjoib2RpbiIsImZpcnN0TmFtZSI6IlVuaWNvbW1lcmNlIiwidXNlcl9pZCI6ImI0ZTViNGNkLTYxMzctNDJjZi1hMDQxLTc3YzA1ODRhOTg1YiIsInNlc3Npb24iOiIwZDE0YTZiMS1iOGNkLTRiMDAtOTBjYS1hNTJiOTAzZTQ0N2QiLCJyb2xlcyI6WyJST0xFX0JVU0lORVNTX1VTRVIiXSwiaXNzIjoiYmxhY2tib2x0IiwidGVuYW50SWQiOiI3MzkzY2UzMy04ZTRmLTQxMDYtOWZkYy0zZmRlYTVjMjdlYzkiLCJ0eXAiOiJCZWFyZXIiLCJpYXQiOjE1ODY4NDcxMTEsImp0aSI6ImNiZjRmY2E1LTkyMzYtNDI5MC04YTBjLTk5OWY2NDE0MmJjNSJ9.ookKI4uy8yfvkoYWqt8-qYU4tIM56_pQ5OnYae14jZHt5NvgV-5oCc1on-6czVb0wQBVPl-wgdHB1BvwfptGpBM9q8a2P6Y0_SwGgD-z-Z_3q0cRjUby0QwJPYARr3_onrKs2eAcC--dcUiAE1m5dQir4KkmgVfPXUEvy4qFyDVZAo5ZJsCXGWLK92nAxfy3vGr3QWOBp5KoPGaaP6vAL_jkrVNaF7SwwMDSJg7paBEFmnllMPCO90ILxOvw6anun9bI3Sn2w2Qtt980zqCsmFfHmy-gqe2SLHGVvlFeHe-_1Mi9_VFGdFJip_UH4IWkITG8SjsxIo4_0r_cpW_fug";
    private static final String FC_ID = "Bata_3051";
    private static final String EAN = "9287018100";

    // Static field to track the last used order reference length
    private static int lastOrderRefLen = 0;

    private static final String REPORT_FILE = MulesoftOrderFlowTest.class.getSimpleName() + "_" +
            ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

    private void logToReport(String message) {
        System.out.println(message);
        try (PrintWriter out = new PrintWriter(new FileWriter(REPORT_FILE, true))) {
            out.println(message);
        } catch (IOException e) {
            System.out.println("[FATAL] Could not write to report file: " + e.getMessage());
        }
    }

    @Test
    public void testInventoryAndOrderFlow() throws Exception {

        // Clear report file at the start
        try (PrintWriter out = new PrintWriter(REPORT_FILE)) {
            out.println("Order and Consignment Status Report");
            out.println("-----------------------------------");
        }
        try {
            logToReport("[INFO] Step 1: Update inventory to 0");
            String response0 = InventoryApi.updateInventory(FC_ID, EAN, 0);
            logToReport("[INFO] Inventory update (0) response: " + response0);

            logToReport("[INFO] Step 2: Update inventory to 1500");
            String response1500 = InventoryApi.updateInventory(FC_ID, EAN, 1500);
            logToReport("[INFO] Inventory update (1500) response: " + response1500);

            logToReport("[INFO] Step 3: Create order with random orderReference");
            // Generate a random orderReference between 11 and 17 characters (including 'OS'), increasing each time
            // Use a static counter to ensure increasing length within the test run
            final int minLen = 11, maxLen = 17;
            // Use a static field to track the last used length
            if (MulesoftOrderFlowTest.lastOrderRefLen == 0) {
                MulesoftOrderFlowTest.lastOrderRefLen = minLen;
            } else if (MulesoftOrderFlowTest.lastOrderRefLen < maxLen) {
                MulesoftOrderFlowTest.lastOrderRefLen++;
            }
            int randomLen = MulesoftOrderFlowTest.lastOrderRefLen;
            String uuidPart = UUID.randomUUID().toString().replaceAll("-", "");
            String base = System.currentTimeMillis() + uuidPart;
            String randomRef = "OS" + base.substring(0, randomLen - 2);
            JSONObject body = new JSONObject();
            body.put("metadata", new JSONObject()
                    .put("billing_address_city", "città test")
                    .put("billing_address_address_line2", "")
                    .put("billing_address_address_line1", "via dei test 33")
                    .put("grossTotal", "599")
                    .put("customer_userid", "+2432432438")
                    .put("netPrice", "5138")
                    .put("customer_username", "+2432432438")
                    .put("merchandiseTotal", "5999")
                    .put("locale", "en_IN")
                    .put("billing_address_zip", "12345")
                    .put("customer_middleName", "")
                    .put("customer_lastName", "test")
                    .put("discountTotal", "0")
                    .put("paymentProvider", "")
                    .put("billing_address_phone", "+2432432438")
                    .put("orderHistoryUrl", "")
                    .put("customer_firstName", "Mattia test")
                    .put("customer_email", "mpalla@deloitte.it")
                    .put("netTotal", "6269")
                    .put("billing_address_state", "Cagliari")
                    .put("billing_address_country", "IT")
                    .put("isSmsNotification", "true")
                    .put("customer_phoneNumber", "+2432432438")
                    .put("paymentTransactionId", "")
            );
            body.put("marketPlaceName", "bata");
            body.put("netAmount", 2895);
            body.put("paymentRatioMetadata", new JSONObject()
                    .put("a", 100)
                    .put("b", 100)
                    .put("c", 100)
            );
            body.put("orderingChannel", "cloud");
            body.put("orderStatus", "processing");
            body.put("clusterId", JSONObject.NULL);
            body.put("createdAt", 1539598799000L);
            body.put("orderReference2", randomRef);
            body.put("grossValue", 2895);
            body.put("service", "Bata");
            body.put("orderReference", randomRef);
            body.put("shippingAddress", new JSONObject()
                    .put("zip", "560001")
                    .put("country", "India")
                    .put("address_line1", "AKR Tech Park- B Block, 7th Mile, Off Hosur Road,nKrishna Reddy Industrial Area, Garebhavipalya, Singasandra")
                    .put("city", "Bangalore")
                    .put("phone", "6362745384")
                    .put("state", "karanatka")
                    .put("customerName", "Valluru Suresh")
            );
            body.put("currency", JSONObject.NULL);

            // Items array
            JSONArray items = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("tradeSp", 1000);
            item.put("metadata", new JSONObject()
                    .put("ABCDE", "XYZ")
                    .put("ORDER_REFSITE", "BD07")
                    .put("ORDER_REFSITE1", "BD07")
                    .put("ABCDF", "XYZ")
                    .put("ORDER_REFSITE2", "BD07")
                    .put("ABCD", "XYZ")
            );
            item.put("quantity", 5);
            item.put("netAmount", 1000);
            item.put("discount", 0);
            item.put("mrp", 1000);
            item.put("grossAmount", 1000);
            item.put("productDetails", new JSONObject()
                    .put("category2", JSONObject.NULL)
                    .put("category3", JSONObject.NULL)
                    .put("image", JSONObject.NULL)
                    .put("color", JSONObject.NULL)
                    .put("category1", JSONObject.NULL)
                    .put("sapStyleId", JSONObject.NULL)
                    .put("description", JSONObject.NULL)
                    .put("mrp", JSONObject.NULL)
                    .put("sapSkuId", JSONObject.NULL)
                    .put("size", JSONObject.NULL)
                    .put("eoisSkuId", JSONObject.NULL)
                    .put("styleId", JSONObject.NULL)
                    .put("eanCode", JSONObject.NULL)
                    .put("brand", JSONObject.NULL)
                    .put("grossSP", JSONObject.NULL)
                    .put("skuId", EAN)
            );
            item.put("typeOfTaxes", new JSONArray());
            item.put("itemId", randomRef);
            item.put("itemReference", randomRef);
            item.put("warehouseId", "Bata_3051");
            JSONArray financialStatus = new JSONArray();
            financialStatus.put(new JSONObject()
                    .put("amount", 1000)
                    .put("paymentMode", "paytm233")
                    .put("paymentStatus", "paid")
            );
            financialStatus.put(new JSONObject()
                    .put("amount", 440)
                    .put("paymentMode", "voucher234")
                    .put("paymentStatus", "paid")
            );
            item.put("financialStatus", financialStatus);
            item.put("shippingAmount", 0);
            item.put("taxAmount", 0);
            item.put("skuId", EAN);
            items.put(item);
            body.put("items", items);

            body.put("channelId", "19");
            body.put("customer", new JSONObject()
                    .put("firstName", "valluru")
                    .put("lastName", "suresh")
                    .put("gender", JSONObject.NULL)
                    .put("phonenumber", "6362745384")
                    .put("middleName", JSONObject.NULL)
                    .put("dateOfBirth", JSONObject.NULL)
                    .put("title", JSONObject.NULL)
                    .put("userId", "")
                    .put("email", "sureshvalluru@arvindinternet.com")
                    .put("channelId", JSONObject.NULL)
                    .put("username", JSONObject.NULL)
            );

            String orderResponse = null;
            try {
                orderResponse = OrderCreationApi.createOrder(body.toString(), AUTH_TOKEN);
                logToReport("[INFO] Order creation response: " + orderResponse);
                logToReport("[INFO] OrderReference used: " + randomRef);
                Thread.sleep(10000);

                // Verify booking call in Chandler DB after order creation
                logToReport("[INFO] Verifying booking call in Chandler DB...");
                try {
                    String bookingData = databaseManager.verifyBookingCallInChandlerDBforBataOrders(randomRef);
                    logToReport("[SUCCESS] Booking call verified in Chandler DB. Data: " + bookingData);
                } catch (SQLException e) {
                    logToReport("[ERROR] Failed to verify booking call in Chandler DB: " + e.getMessage());
                    throw e;
                }
            } catch (Exception e) {
                logToReport("[ERROR] Order creation failed: " + e.getMessage());
                if (orderResponse != null) {
                    logToReport("[ERROR] Order creation error response: " + orderResponse);
                }
                throw e;
            }

            logToReport("[INFO] Waiting 50 seconds for order status to update...");
            Thread.sleep(50000);

            logToReport("[INFO] Step 4: Get consignmentId from order details");
            String consignmentId = OrderDetailsApi.getFirstConsignmentId(randomRef, AUTH_TOKEN);
            logToReport("[INFO] ConsignmentId: " + consignmentId);

            logToReport("[INFO] Step 5: Get consignment status");
            String consignmentStatus = GenericDetailsApi.getConsignmentStatus(consignmentId);
            logToReport("[INFO] ConsignmentStatus: " + consignmentStatus);

            if (!"Assigned".equalsIgnoreCase(consignmentStatus)) {
                logToReport("[ERROR] Order is not in Assigned status");
                throw new RuntimeException("Order is not in Assigned status");
            }

            logToReport("[INFO] Step 6: Call ChandlerOrderStatusApi");
            String deliveryShopNo = "3051"; // or extract from order/consignment if needed
            String chandlerResponse = ChandlerOrderStatusApi.updateOrderStatus(randomRef, deliveryShopNo);
            logToReport("[INFO] Chandler order status update response: " + chandlerResponse);

            logToReport("[INFO] Step 7: Get updated consignment status");
            String updatedConsignmentStatus = GenericDetailsApi.getConsignmentStatus(consignmentId);
            logToReport("[INFO] Updated ConsignmentStatus: " + updatedConsignmentStatus);

            logToReport("[INFO] Step 8: Update consignment status to INVOICED");
            String consignmentStatusUpdateResponse = ConsignmentStatusApi.updateConsignmentStatus(consignmentId, "INVOICED", "Auto change by System", 7);
            logToReport("[INFO] Consignment status update response: " + consignmentStatusUpdateResponse);

            logToReport("[INFO] Step 9: Validate consignment status is 'Packed'");
            String packedStatus = GenericDetailsApi.getConsignmentStatus(consignmentId);
            logToReport("[INFO] ConsignmentStatus after invoicing: " + packedStatus);
            
            // Verify sale call in Chandler DB after invoicing
            if ("Packed".equalsIgnoreCase(packedStatus)) {
                logToReport("[INFO] Verifying sale call in Chandler DB...");
                try (DatabaseManager dbManager = new DatabaseManager("nickfury")) {
                    String saleData = dbManager.verifySaleCallInChandlerDBforBataOrders(randomRef);
                    logToReport("[SUCCESS] Sale call verified in Chandler DB. Data: " + saleData);
                } catch (SQLException e) {
                    logToReport("[ERROR] Failed to verify sale call in Chandler DB: " + e.getMessage());
                    throw e;
                }
            }
            
            if (!"Packed".equalsIgnoreCase(packedStatus)) {
                logToReport("[ERROR] Consignment is not invoiced");
                throw new RuntimeException("Consignment is not invoiced");
            } else {
                logToReport("[INFO] Consignment is invoiced and status is 'Packed'");
            }

            // Step 10: Update consignment status to SHIPPED
            logToReport("[INFO] Step 10: Update consignment status to SHIPPED");
            String shippedDate = "2025-07-20T05:04:00Z";
            String shippedResponse = BumblebeeShipmentStatusApi.updateShipmentStatus(consignmentId, "SHIPPED", shippedDate);
            logToReport("[INFO] Shipment status update response (SHIPPED): " + shippedResponse);
            Thread.sleep(5000);
            String shippedStatus = GenericDetailsApi.getConsignmentStatus(consignmentId);
            logToReport("[INFO] ConsignmentStatus after shipping: " + shippedStatus);
            if (!"Shipped".equalsIgnoreCase(shippedStatus)) {
                logToReport("[ERROR] Consignment is not Shipped");
                throw new RuntimeException("Consignment is not Shipped");
            } else {
                logToReport("[INFO] Consignment is shipped and status is 'Shipped'");
            }

            // Step 11: Update consignment status to DELIVERED
            logToReport("[INFO] Step 11: Update consignment status to DELIVERED");
            String deliveredResponse = BumblebeeShipmentStatusApi.updateShipmentStatus(consignmentId, "DELIVERED", shippedDate);
            logToReport("[INFO] Shipment status update response (DELIVERED): " + deliveredResponse);
            Thread.sleep(5000);
            String deliveredStatus = GenericDetailsApi.getConsignmentStatus(consignmentId);
            logToReport("[INFO] ConsignmentStatus after delivery: " + deliveredStatus);
            if (!"Delivered".equalsIgnoreCase(deliveredStatus)) {
                logToReport("[ERROR] Order is not delivered");
                throw new RuntimeException("Order is not delivered");
            } else {
                logToReport("[INFO] Order is delivered. OrderReference: " + randomRef + ", ConsignmentId: " + consignmentId);
            }
        } catch (Exception e) {
            logToReport("[FATAL] Test failed: " + e.getMessage());
            throw e;
        }
    }
}
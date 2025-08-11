package com.cloud.omuni_cloud.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderCreationApi {
    // Authentication token - should be moved to a configuration class in a real application
    private static final String AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJlbWFpbF9pZCI6ImluY3JlZmZAZ21haWwuY29tIiwiYXVkIjoib2RpbiIsImZpcnN0TmFtZSI6IlVuaWNvbW1lcmNlIiwidXNlcl9pZCI6ImI0ZTViNGNkLTYxMzctNDJjZi1hMDQxLTc3YzA1ODRhOTg1YiIsInNlc3Npb24iOiIwZDE0YTZiMS1iOGNkLTRiMDAtOTBjYS1hNTJiOTAzZTQ0N2QiLCJyb2xlcyI6WyJST0xFX0JVU0lORVNTX1VTRVIiXSwiaXNzIjoiYmxhY2tib2x0IiwidGVuYW50SWQiOiI3MzkzY2UzMy04ZTRmLTQxMDYtOWZkYy0zZmRlYTVjMjdlYzkiLCJ0eXAiOiJCZWFyZXIiLCJpYXQiOjE1ODY4NDcxMTEsImp0aSI6ImNiZjRmY2E1LTkyMzYtNDI5MC04YTBjLTk5OWY2NDE0MmJjNSJ9.ookKI4uy8yfvkoYWqt8-qYU4tIM56_pQ5OnYae14jZHt5NvgV-5oCc1on-6czVb0wQBVPl-wgdHB1BvwfptGpBM9q8a2P6Y0_SwGgD-z-Z_3q0cRjUby0QwJPYARr3_onrKs2eAcC--dcUiAE1m5dQir4KkmgVfPXUEvy4qFyDVZAo5ZJsCXGWLK92nAxfy3vGr3QWOBp5KoPGaaP6vAL_jkrVNaF7SwwMDSJg7paBEFmnllMPCO90ILxOvw6anun9bI3Sn2w2Qtt980zqCsmFfHmy-gqe2SLHGVvlFeHe-_1Mi9_VFGdFJip_UH4IWkITG8SjsxIo4_0r_cpW_fug";
    private static final Logger logger = LoggerFactory.getLogger(OrderCreationApi.class);
    private static final String BASE_URL = "https://api-preprod.ailiens.com/b/namo/api";
    private static final String ORDER_ENDPOINT = "/order";
    
    /**
     * Creates a new order
     * @param fcId The fulfillment center ID
     * @param orderReference Unique reference for the order
     * @param ean The product EAN
     * @param quantity The quantity to order
     * @return The API response as a string
     * @throws RuntimeException if there's an error creating the order
     */
    public static String createOrder(String fcId, String orderReference, String ean, int quantity) {
        try {
            // Build the order request body
            JSONObject body = new JSONObject();
            
            // Add billing address
            JSONObject billingAddress = new JSONObject();
            billingAddress.put("city", "Test City");
            billingAddress.put("addressLine1", "123 Test St");
            billingAddress.put("state", "Test State");
            billingAddress.put("country", "IN");
            
            // Add order totals
            body.put("grossTotal", 599);
            body.put("netTotal", 599);
            body.put("orderHistoryUrl", "");
            body.put("isSmsNotification", true);
            body.put("paymentTransactionId", "");
            
            // Add billing address to the root
            body.put("billingAddress", billingAddress);
            
            // Add market place and order details
            body.put("marketPlaceName", "bata");
            body.put("orderReference", orderReference);
            body.put("orderDate", System.currentTimeMillis());
            
            // Add items
            JSONArray items = new JSONArray();
            JSONObject item = new JSONObject();
            item.put("itemId", ean);
            item.put("itemReference", ean);
            item.put("warehouseId", fcId);
            item.put("quantity", quantity);
            item.put("netAmount", 599);
            item.put("discount", 0);
            item.put("mrp", 599);
            item.put("grossAmount", 599);
            item.put("skuId", ean);
            items.put(item);
            
            body.put("items", items);
            
            // Add customer details
            JSONObject customer = new JSONObject();
            customer.put("firstName", "Test");
            customer.put("lastName", "User");
            customer.put("gender", "MALE");
            customer.put("phonenumber", "+1234567890");
            customer.put("email", "test@example.com");
            body.put("customer", customer);
            
            // Add shipping details
            JSONObject shipping = new JSONObject();
            shipping.put("city", "Test City");
            shipping.put("addressLine1", "123 Test St");
            shipping.put("state", "Test State");
            shipping.put("country", "IN");
            shipping.put("phoneNumber", "+1234567890");
            shipping.put("firstName", "Test");
            shipping.put("email", "test@example.com");
            
            body.put("shippingAddress", shipping);
            
            logger.debug("Creating order with reference: {}, EAN: {}, Quantity: {}", orderReference, ean, quantity);
            
            // Make the API call
            Response response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + AUTH_TOKEN)
                .body(body.toString())
                .when()
                .post(BASE_URL + ORDER_ENDPOINT);
                
            String responseBody = response.getBody().asString();
            logger.debug("Order creation response: {}", responseBody);
            
            return responseBody;
            
        } catch (JSONException e) {
            String errorMsg = String.format("Error creating order request body. OrderRef: %s, EAN: %s, Qty: %d", 
                orderReference, ean, quantity);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format("Error creating order. OrderRef: %s, EAN: %s, Qty: %d", 
                orderReference, ean, quantity);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
}

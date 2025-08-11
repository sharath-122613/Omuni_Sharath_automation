package com.cloud.omuni_cloud.factory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDataFactory {
    private static final Logger logger = LoggerFactory.getLogger(TestDataFactory.class);
    
    public static JSONObject createOrderPayload(String orderReference, String ean, String fcId) {
        try {
        JSONObject order = new JSONObject();
        
        // Metadata
        order.put("metadata", new JSONObject()
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
        
        // Order details
        order.put("marketPlaceName", "bata");
        order.put("netAmount", 2895);
        order.put("paymentRatioMetadata", new JSONObject()
            .put("a", 100)
            .put("b", 100)
            .put("c", 100)
        );
        order.put("orderingChannel", "cloud");
        order.put("orderStatus", "processing");
        order.put("clusterId", JSONObject.NULL);
        order.put("createdAt", System.currentTimeMillis());
        order.put("orderReference2", orderReference);
        order.put("grossValue", 2895);
        order.put("service", "Bata");
        order.put("orderReference", orderReference);
        order.put("channelId", "19");
        
        // Shipping address
        order.put("shippingAddress", new JSONObject()
            .put("zip", "560001")
            .put("country", "India")
            .put("address_line1", "AKR Tech Park- B Block, 7th Mile, Off Hosur Road")
            .put("city", "Bangalore")
            .put("phone", "6362745384")
            .put("state", "karnataka")
            .put("customerName", "Test User")
        );
        
        // Customer details
        order.put("customer", new JSONObject()
            .put("firstName", "Test")
            .put("lastName", "User")
            .put("gender", JSONObject.NULL)
            .put("phonenumber", "6362745384")
            .put("middleName", JSONObject.NULL)
            .put("dateOfBirth", JSONObject.NULL)
            .put("title", JSONObject.NULL)
            .put("userId", "")
            .put("email", "test.user@example.com")
            .put("channelId", JSONObject.NULL)
            .put("username", JSONObject.NULL)
        );
        
        // Items
        JSONArray items = new JSONArray();
        items.put(createOrderItem(orderReference, ean, fcId));
        order.put("items", items);
        
            return order;
        } catch (JSONException e) {
            String errorMsg = String.format("Error creating order payload. OrderRef: %s, EAN: %s, FC ID: %s", 
                orderReference, ean, fcId);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    public static JSONObject createOrderItem(String orderReference, String ean, String fcId) {
        try {
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
            .put("eanCode", ean)
            .put("brand", JSONObject.NULL)
            .put("grossSP", JSONObject.NULL)
            .put("skuId", ean)
        );
        item.put("typeOfTaxes", new JSONArray());
        item.put("itemId", orderReference);
        item.put("itemReference", orderReference);
        item.put("warehouseId", fcId);
        
        // Financial status
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
        item.put("skuId", ean);
        
            return item;
        } catch (JSONException e) {
            String errorMsg = String.format("Error creating order item. OrderRef: %s, EAN: %s, FC ID: %s", 
                orderReference, ean, fcId);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
}

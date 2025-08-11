package com.cloud.omuni_cloud;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderApiTest {

    @Test
    public void testOrderApiWithRandomReferences() throws Exception {
        // Generate a random value for all required fields
        String randomRef = "OS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6);
        System.out.println("Generated orderReference: " + randomRef);

        // Build the JSON body
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
                .put("skuId", "9287018100")
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
        item.put("skuId", "9287018100");
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

        // Send the POST request
        Response response = RestAssured.given()
                .baseUri("https://api-preprod.ailiens.com")
                .basePath("/b/namo/api/v1/MULESOFT/order/v2")
                .header("Content-Type", "application/json")
                .header("x-tenant-id", "7393ce33-8e4f-4106-9fdc-3fdea5c27ec9")
                .header("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJlbWFpbF9pZCI6ImluY3JlZmZAZ21haWwuY29tIiwiYXVkIjoib2RpbiIsImZpcnN0TmFtZSI6IlVuaWNvbW1lcmNlIiwidXNlcl9pZCI6ImI0ZTViNGNkLTYxMzctNDJjZi1hMDQxLTc3YzA1ODRhOTg1YiIsInNlc3Npb24iOiIwZDE0YTZiMS1iOGNkLTRiMDAtOTBjYS1hNTJiOTAzZTQ0N2QiLCJyb2xlcyI6WyJST0xFX0JVU0lORVNTX1VTRVIiXSwiaXNzIjoiYmxhY2tib2x0IiwidGVuYW50SWQiOiI3MzkzY2UzMy04ZTRmLTQxMDYtOWZkYy0zZmRlYTVjMjdlYzkiLCJ0eXAiOiJCZWFyZXIiLCJpYXQiOjE1ODY4NDcxMTEsImp0aSI6ImNiZjRmY2E1LTkyMzYtNDI5MC04YTBjLTk5OWY2NDE0MmJjNSJ9.ookKI4uy8yfvkoYWqt8-qYU4tIM56_pQ5OnYae14jZHt5NvgV-5oCc1on-6czVb0wQBVPl-wgdHB1BvwfptGpBM9q8a2P6Y0_SwGgD-z-Z_3q0cRjUby0QwJPYARr3_onrKs2eAcC--dcUiAE1m5dQir4KkmgVfPXUEvy4qFyDVZAo5ZJsCXGWLK92nAxfy3vGr3QWOBp5KoPGaaP6vAL_jkrVNaF7SwwMDSJg7paBEFmnllMPCO90ILxOvw6anun9bI3Sn2w2Qtt980zqCsmFfHmy-gqe2SLHGVvlFeHe-_1Mi9_VFGdFJip_UH4IWkITG8SjsxIo4_0r_cpW_fug")
                .body(body.toString())
                .post();

        // Print the response
        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody().asString());

        // Assert status code (optional)
        // assertEquals(200, response.getStatusCode(), "Expected status code 200");
    }

    @Test
    public void testGetOrderDetailsAndExtractConsignmentId() throws Exception {
        // Use the last generated orderReference from the previous test (replace with actual value if needed)
        String orderReference = "OS1753689096802dec119";
        String url = "https://api-preprod.ailiens.com/b/namo/api/ordersDetails/cloud/" + orderReference;

        Response response = RestAssured.given()
                .header("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJlbWFpbF9pZCI6ImluY3JlZmZAZ21haWwuY29tIiwiYXVkIjoib2RpbiIsImZpcnN0TmFtZSI6IlVuaWNvbW1lcmNlIiwidXNlcl9pZCI6ImI0ZTViNGNkLTYxMzctNDJjZi1hMDQxLTc3YzA1ODRhOTg1YiIsInNlc3Npb24iOiIwZDE0YTZiMS1iOGNkLTRiMDAtOTBjYS1hNTJiOTAzZTQ0N2QiLCJyb2xlcyI6WyJST0xFX0JVU0lORVNTX1VTRVIiXSwiaXNzIjoiYmxhY2tib2x0IiwidGVuYW50SWQiOiI3MzkzY2UzMy04ZTRmLTQxMDYtOWZkYy0zZmRlYTVjMjdlYzkiLCJ0eXAiOiJCZWFyZXIiLCJpYXQiOjE1ODY4NDcxMTEsImp0aSI6ImNiZjRmY2E1LTkyMzYtNDI5MC04YTBjLTk5OWY2NDE0MmJjNSJ9.ookKI4uy8yfvkoYWqt8-qYU4tIM56_pQ5OnYae14jZHt5NvgV-5oCc1on-6czVb0wQBVPl-wgdHB1BvwfptGpBM9q8a2P6Y0_SwGgD-z-Z_3q0cRjUby0QwJPYARr3_onrKs2eAcC--dcUiAE1m5dQir4KkmgVfPXUEvy4qFyDVZAo5ZJsCXGWLK92nAxfy3vGr3QWOBp5KoPGaaP6vAL_jkrVNaF7SwwMDSJg7paBEFmnllMPCO90ILxOvw6anun9bI3Sn2w2Qtt980zqCsmFfHmy-gqe2SLHGVvlFeHe-_1Mi9_VFGdFJip_UH4IWkITG8SjsxIo4_0r_cpW_fug")
                .header("Postman-Token", "28cf65ea-1398-4a78-904b-72d5944b6c90")
                .header("cache-control", "no-cache")
                .get(url);

        System.out.println("Status code: " + response.getStatusCode());
        System.out.println("Response body: " + response.getBody().asString());

        // Try to extract consignmentId from the response JSON
        try {
            JSONObject json = new JSONObject(response.getBody().asString());
            // Adjust the path to consignmentId as per actual response structure
            String consignmentId = json.optString("consignmentId", "NOT_FOUND");
            System.out.println("consignmentId: " + consignmentId);
        } catch (Exception e) {
            System.out.println("Could not extract consignmentId: " + e.getMessage());
        }
    }

    @Test
    public void testGetConsignmentStatusFromGenericDetails() throws Exception {
        // Step 1: Get consignmentId from order details
        String orderReference = "OS1753689096802dec119";
        String orderDetailsUrl = "https://api-preprod.ailiens.com/b/namo/api/ordersDetails/cloud/" + orderReference;

        Response orderDetailsResponse = RestAssured.given()
                .header("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJlbWFpbF9pZCI6ImluY3JlZmZAZ21haWwuY29tIiwiYXVkIjoib2RpbiIsImZpcnN0TmFtZSI6IlVuaWNvbW1lcmNlIiwidXNlcl9pZCI6ImI0ZTViNGNkLTYxMzctNDJjZi1hMDQxLTc3YzA1ODRhOTg1YiIsInNlc3Npb24iOiIwZDE0YTZiMS1iOGNkLTRiMDAtOTBjYS1hNTJiOTAzZTQ0N2QiLCJyb2xlcyI6WyJST0xFX0JVU0lORVNTX1VTRVIiXSwiaXNzIjoiYmxhY2tib2x0IiwidGVuYW50SWQiOiI3MzkzY2UzMy04ZTRmLTQxMDYtOWZkYy0zZmRlYTVjMjdlYzkiLCJ0eXAiOiJCZWFyZXIiLCJpYXQiOjE1ODY4NDcxMTEsImp0aSI6ImNiZjRmY2E1LTkyMzYtNDI5MC04YTBjLTk5OWY2NDE0MmJjNSJ9.ookKI4uy8yfvkoYWqt8-qYU4tIM56_pQ5OnYae14jZHt5NvgV-5oCc1on-6czVb0wQBVPl-wgdHB1BvwfptGpBM9q8a2P6Y0_SwGgD-z-Z_3q0cRjUby0QwJPYARr3_onrKs2eAcC--dcUiAE1m5dQir4KkmgVfPXUEvy4qFyDVZAo5ZJsCXGWLK92nAxfy3vGr3QWOBp5KoPGaaP6vAL_jkrVNaF7SwwMDSJg7paBEFmnllMPCO90ILxOvw6anun9bI3Sn2w2Qtt980zqCsmFfHmy-gqe2SLHGVvlFeHe-_1Mi9_VFGdFJip_UH4IWkITG8SjsxIo4_0r_cpW_fug")
                .header("Postman-Token", "28cf65ea-1398-4a78-904b-72d5944b6c90")
                .header("cache-control", "no-cache")
                .get(orderDetailsUrl);

        String consignmentId = null;
        try {
            JSONObject json = new JSONObject(orderDetailsResponse.getBody().asString());
            if (json.has("consignments")) {
                consignmentId = json.getJSONArray("consignments").getJSONObject(0).optString("consignmentId", null);
            }
        } catch (Exception e) {
            System.out.println("Could not extract consignmentId: " + e.getMessage());
        }
        System.out.println("Extracted consignmentId: " + consignmentId);
        if (consignmentId == null) {
            System.out.println("No consignmentId found, aborting test.");
            return;
        }

        // Step 2: Use consignmentId in POST to getGenericDetails
        String genericDetailsUrl = "https://api-preprod.ailiens.com/b/oms/getGenericDetails";
        JSONObject postBody = new JSONObject();
        postBody.put("consignmentId", new JSONArray().put(consignmentId));
        postBody.put("orderParams", new JSONArray()
                .put("customerDetails")
                .put("storeStaffDetails")
                .put("orderingCenter"));
        postBody.put("consignmentParams", new JSONArray()
                .put("ffCenter")
                .put("b2bDetails")
                .put("paymentDetails"));
        postBody.put("orderLineParams", new JSONArray()
                .put("product")
                .put("addressDetails")
                .put("itemPricingDetails")
                .put("b2bDetails")
                .put("taxSummary")
                .put("discountSummary")
                .put("chargeSummary")
                .put("totalCharge"));
        postBody.put("sambhaProperties", new JSONArray()
                .put("consignmentDelFlow")
                .put("posInvoiceConfig")
                .put("itemAttributes"));
        postBody.put("allConsignmentInfo", false);

        Response genericDetailsResponse = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("X-Roles", "ROLE_SUPER_WOMAN")
                .header("cache-control", "no-cache")
                .header("x-tenant-id", "7393ce33-8e4f-4106-9fdc-3fdea5c27ec9")
                .body(postBody.toString())
                .post(genericDetailsUrl);

        System.out.println("Status code: " + genericDetailsResponse.getStatusCode());
        System.out.println("Response body: " + genericDetailsResponse.getBody().asString());

        // Try to extract consignmentStatus from the response JSON
        try {
            JSONObject json = new JSONObject(genericDetailsResponse.getBody().asString());
            // Navigate to data.consignmentList[0].consignmentStatus.consignmentStatus
            String consignmentStatus = "NOT_FOUND";
            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                if (data.has("consignmentList")) {
                    JSONArray consignmentList = data.getJSONArray("consignmentList");
                    if (consignmentList.length() > 0) {
                        JSONObject consignment = consignmentList.getJSONObject(0);
                        if (consignment.has("consignmentStatus")) {
                            JSONObject statusObj = consignment.getJSONObject("consignmentStatus");
                            consignmentStatus = statusObj.optString("consignmentStatus", "NOT_FOUND");
                        }
                    }
                }
            }
            System.out.println("consignmentStatus: " + consignmentStatus);
        } catch (Exception e) {
            System.out.println("Could not extract consignmentStatus: " + e.getMessage());
        }
    }
} 
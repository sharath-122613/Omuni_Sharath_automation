package com.cloud.omuni_cloud;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class GenericDetailsApi {
    public static String getConsignmentStatus(String consignmentId) throws Exception {
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

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("X-Roles", "ROLE_SUPER_WOMAN")
                .header("cache-control", "no-cache")
                .header("x-tenant-id", "7393ce33-8e4f-4106-9fdc-3fdea5c27ec9")
                .body(postBody.toString())
                .post(genericDetailsUrl);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to get generic details: HTTP " + response.getStatusCode());
        }

        // Extract consignmentStatus from data.consignmentList[0].consignmentStatus.consignmentStatus
        JSONObject json = new JSONObject(response.getBody().asString());
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
        return consignmentStatus;
    }
} 
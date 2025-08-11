package com.cloud.omuni_cloud;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.json.JSONArray;

public class OrderDetailsApi {
    public static String getFirstConsignmentId(String orderReference, String authorizationToken) throws Exception {
        String orderDetailsUrl = "https://api-preprod.ailiens.com/b/namo/api/ordersDetails/cloud/" + orderReference;
        Response response = RestAssured.given()
                .header("Authorization", authorizationToken)
                .header("Postman-Token", "28cf65ea-1398-4a78-904b-72d5944b6c90")
                .header("cache-control", "no-cache")
                .get(orderDetailsUrl);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to get order details: HTTP " + response.getStatusCode());
        }

        JSONObject json = new JSONObject(response.getBody().asString());
        String consignmentId = null;
        if (json.has("consignments")) {
            JSONArray consignments = json.getJSONArray("consignments");
            if (consignments.length() > 0) {
                consignmentId = consignments.getJSONObject(0).optString("consignmentId", null);
            }
        }
        return consignmentId;
    }
} 
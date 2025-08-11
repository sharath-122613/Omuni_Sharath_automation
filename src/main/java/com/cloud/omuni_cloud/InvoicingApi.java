package com.cloud.omuni_cloud;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;

public class InvoicingApi {
    public static String invoiceConsignment(String consignmentId) throws Exception {
        String url = "https://api-preprod.ailiens.com/b/oms/invoiceConsignment";
        JSONObject body = new JSONObject();
        body.put("consignmentId", consignmentId);

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("x-tenant-id", "7393ce33-8e4f-4106-9fdc-3fdea5c27ec9")
                .body(body.toString())
                .post(url);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to invoice consignment: HTTP " + response.getStatusCode() + " - " + response.getBody().asString());
        }
        return response.getBody().asString();
    }
} 
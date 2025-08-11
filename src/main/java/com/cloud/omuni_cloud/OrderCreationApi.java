package com.cloud.omuni_cloud;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class OrderCreationApi {
    public static String createOrder(String orderJsonBody, String authorizationToken) throws Exception {
        String url = "https://api-preprod.ailiens.com/b/namo/api/v1/MULESOFT/order/v2";
        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("x-tenant-id", "7393ce33-8e4f-4106-9fdc-3fdea5c27ec9")
                .header("Authorization", authorizationToken)
                .body(orderJsonBody)
                .post(url);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to create order: HTTP " + response.getStatusCode() + " - " + response.getBody().asString());
        }
        return response.getBody().asString();
    }
} 
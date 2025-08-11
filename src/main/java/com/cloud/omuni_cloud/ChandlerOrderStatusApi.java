package com.cloud.omuni_cloud;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;

public class ChandlerOrderStatusApi {
    public static String updateOrderStatus(String orderReference, String deliveryShopNo) throws Exception {
        String url = "https://api-preprod.ailiens.com/b/chandler/bata/order/status";
        JSONObject body = new JSONObject();
        body.put("orderNo", orderReference);
        body.put("subOrderNo", orderReference);
        body.put("deliveryShopNo", deliveryShopNo);
        body.put("deliveryStatus", "Y");
        body.put("invoiceNo", orderReference);
        body.put("deliveryRemarks", "Order Accepted.");

        Response response = RestAssured.given()
                .header("Accept", "*/*")
                .header("Authorization", "Basic ZGtnbXIzMjE6SGQzNDMxZGFz")
                .header("content-type", "application/json")
                .body(body.toString())
                .post(url);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to update order status: HTTP " + response.getStatusCode() + " - " + response.getBody().asString());
        }
        return response.getBody().asString();
    }
} 
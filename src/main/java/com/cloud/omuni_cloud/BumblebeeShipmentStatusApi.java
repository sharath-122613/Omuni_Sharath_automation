package com.cloud.omuni_cloud;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class BumblebeeShipmentStatusApi {
    public static String updateShipmentStatus(String consignmentId, String status, String deliveredDate) throws Exception {
        String url = "https://api-preprod.ailiens.com/b/bumblebee/api/change/shipmentStatus";
        JSONArray body = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("consignmentId", consignmentId);
        item.put("status", status);
        if (deliveredDate != null) {
            item.put("deliveredDate", deliveredDate);
        }
        body.put(item);

        Response response = RestAssured.given()
                .header("Postman-Token", "5f9d336a-eedb-4fa4-b408-cf38cefc96e0")
                .header("cache-control", "no-cache,no-cache")
                .header("content-type", "application/json")
                .header("postman-token", "2c91d890-d57f-55f1-315b-2699be6771e6")
                .header("x-roles", "ROLE_SUPER_WOMAN")
                .header("x-tenant-id", "7393ce33-8e4f-4106-9fdc-3fdea5c27ec9")
                .body(body.toString())
                .post(url);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to update shipment status: HTTP " + response.getStatusCode() + " - " + response.getBody().asString());
        }
        return response.getBody().asString();
    }
} 
package com.cloud.omuni_cloud;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;


public class ConsignmentStatusApi {
    public static String updateConsignmentStatus(String consignmentId, String status, String comment, int packingTypeId) throws Exception {
        String url = "https://api-preprod.ailiens.com/b/oms/v2/consignment/status";
        JSONObject body = new JSONObject();
        body.put("status", status);
        body.put("comment", comment);
        body.put("consignmentId", consignmentId);
        body.put("packingTypeId", packingTypeId);

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("Postman-Token", "084b4bed-3f65-42b6-8b60-2b965698e8c8")
                .header("cache-control", "no-cache")
                .header("x-roles", "ROLE_SUPER_WOMAN")
                .header("x-tenant-id", "7393ce33-8e4f-4106-9fdc-3fdea5c27ec9")
                .body(body.toString())
                .post(url);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to update consignment status: HTTP " + response.getStatusCode() + " - " + response.getBody().asString());
        }
        return response.getBody().asString();
    }

}
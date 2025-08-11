package com.cloud.omuni_cloud;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class InventoryApi {
    public static String updateInventory(String fcId, String ean, int quantity) throws Exception {
        String url = "https://api-preprod.ailiens.com/b/galactusReloaded/write/fCInventory/updateInventory/nickfury";
        JSONArray body = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("fcId", fcId);
        item.put("ean", ean);
        item.put("sapSkuId", "");
        item.put("quantity", quantity);
        item.put("source", "Out");
        item.put("destination", "Stock");
        item.put("transactionType", "Overwrite");
        item.put("pickFail", false);
        item.put("localPosSale", false);
        body.put(item);

        Response response = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("x-roles", "ROLE_SUPER_WOMAN")
                .header("x-tenant-id", "7393ce33-8e4f-4106-9fdc-3fdea5c27ec9")
                .header("Accept", "application/json")
                .body(body.toString())
                .post(url);

        if (response.getStatusCode() != 200) {
            throw new RuntimeException("Failed to update inventory: HTTP " + response.getStatusCode() + " - " + response.getBody().asString());
        }
        return response.getBody().asString();
    }
} 
package com.cloud.omuni_cloud.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryApi {
    private static final Logger logger = LoggerFactory.getLogger(InventoryApi.class);
    private static final String BASE_URL = "https://api-preprod.ailiens.com/b/namo/api";
    
    /**
     * Updates inventory for a specific EAN at a fulfillment center
     * @param authToken The authentication token for the API
     * @param fcId The fulfillment center ID
     * @param ean The product EAN
     * @param quantity The quantity to update
     * @return The API response as a string
     * @throws RuntimeException if there's an error creating the request body or making the API call
     */
    public static String updateInventory(String authToken, String fcId, String ean, int quantity) {
        try {
            JSONObject requestBody = new JSONObject()
                .put("fcId", fcId)
                .put("ean", ean)
                .put("quantity", quantity);
                
            Response response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(requestBody.toString())
                .when()
                .post(BASE_URL + "/inventory/update");
                
            return response.getBody().asString();
        } catch (JSONException e) {
            String errorMsg = String.format("Error creating request body for inventory update. FC: %s, EAN: %s, Qty: %d", 
                fcId, ean, quantity);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format("Error updating inventory. FC: %s, EAN: %s, Qty: %d", 
                fcId, ean, quantity);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
}

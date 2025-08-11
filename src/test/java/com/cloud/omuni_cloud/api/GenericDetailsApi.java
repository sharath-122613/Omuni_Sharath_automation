package com.cloud.omuni_cloud.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericDetailsApi {
    private static final Logger logger = LoggerFactory.getLogger(GenericDetailsApi.class);
    private static final String BASE_URL = "https://api-preprod.ailiens.com/b/namo/api";
    
    /**
     * Gets the status of a consignment
     * @param authToken The authentication token for the API
     * @param consignmentId The ID of the consignment to check
     * @return The status of the consignment
     * @throws RuntimeException if there's an error getting the consignment status
     */
    public static String getConsignmentStatus(String authToken, String consignmentId) {
        try {
            logger.debug("Getting status for consignment: {}", consignmentId);
            Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get(BASE_URL + "/consignment/" + consignmentId + "/status");
                
            // Parse response to extract status
            String responseBody = response.getBody().asString();
            logger.debug("Consignment status response: {}", responseBody);
            
            JSONObject jsonResponse = new JSONObject(responseBody);
            
            // Try to get status from different possible locations in the response
            if (jsonResponse.has("consignmentStatus")) {
                return jsonResponse.getString("consignmentStatus");
            } else if (jsonResponse.has("status")) {
                return jsonResponse.getString("status");
            } else if (jsonResponse.has("data") && jsonResponse.get("data") instanceof JSONObject) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data.has("consignmentStatus")) {
                    return data.getString("consignmentStatus");
                }
            }
            
            // If we get here, we couldn't find the status in the expected format
            String errorMsg = "Could not determine consignment status from response: " + responseBody;
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
            
        } catch (JSONException e) {
            String errorMsg = String.format("Error parsing consignment status response for consignment %s", consignmentId);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format("Error getting status for consignment %s", consignmentId);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Updates the status of a consignment
     * @param authToken The authentication token for the API
     * @param consignmentId The ID of the consignment to update
     * @param status The new status to set
     * @param comment A comment about the status update
     * @param userId The ID of the user making the update
     * @return The API response as a string
     * @throws RuntimeException if there's an error updating the consignment status
     */
    public static String updateConsignmentStatus(String authToken, String consignmentId, 
            String status, String comment, int userId) {
        try {
            JSONObject requestBody = new JSONObject()
                .put("status", status)
                .put("comment", comment)
                .put("userId", userId);
                
            Response response = RestAssured.given()
                .contentType("application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(requestBody.toString())
                .when()
                .put(BASE_URL + "/consignment/" + consignmentId + "/status");
                
            return response.getBody().asString();
            
        } catch (JSONException e) {
            String errorMsg = String.format("Error creating request body for consignment status update. Consignment: %s", consignmentId);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = String.format("Error updating status for consignment %s", consignmentId);
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }
}

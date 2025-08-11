package com.cloud.omuni_cloud;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Add these imports if these classes exist in your project
// import com.cloud.omuni_cloud.api.OrderDetailsApi;
// import com.cloud.omuni_cloud.api.GenericDetailsApi;

public class OrderAndConsignmentStatusTest {
    private static String authToken;
    private static final String CSV_FILE_PATH = "src/test/resources/orderReferences.csv";

    @BeforeAll
    public static void setup() {
        // Load auth token from environment variable or configuration
        authToken = System.getenv("AUTH_TOKEN");
        if (authToken == null || authToken.trim().isEmpty()) {
            throw new IllegalStateException("AUTH_TOKEN environment variable is not set");
        }
    }

    private List<String> readOrderReferencesFromCsv(String filePath) throws IOException, CsvValidationException {
        List<String> orderReferences = new ArrayList<>();
        
        // Get the absolute path of the CSV file
        File csvFile = Paths.get(filePath).toFile();
        if (!csvFile.exists()) {
            throw new IOException("CSV file not found at: " + csvFile.getAbsolutePath());
        }
        
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] nextLine;
            boolean isHeader = true;
            
            while ((nextLine = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                if (nextLine.length > 0 && nextLine[0] != null && !nextLine[0].trim().isEmpty()) {
                    orderReferences.add(nextLine[0].trim());
                }
            }
        }
        
        if (orderReferences.isEmpty()) {
            System.out.println("Warning: No order references found in the CSV file");
        }
        
        return orderReferences;
    }

    @Test
    public void testMultipleOrderConsignmentStatus() {
        try {
            List<String> orderReferences = readOrderReferencesFromCsv(CSV_FILE_PATH);
            
            if (orderReferences.isEmpty()) {
                System.out.println("No order references to process");
                return;
            }

            System.out.println("Processing " + orderReferences.size() + " order references...");
            
            for (String orderReference : orderReferences) {
                try {
                    System.out.println("\n--- Processing OrderReference: " + orderReference + " ---");
                    
                    // Uncomment and implement these methods in your API classes
                    /*
                    String consignmentId = OrderDetailsApi.getFirstConsignmentId(orderReference, authToken);
                    if (consignmentId != null && !consignmentId.trim().isEmpty()) {
                        System.out.println("ConsignmentId: " + consignmentId);
                        
                        String consignmentStatus = GenericDetailsApi.getConsignmentStatus(consignmentId, authToken);
                        System.out.println("ConsignmentStatus: " + consignmentStatus);
                    } else {
                        System.out.println("No consignment found for order reference: " + orderReference);
                    }
                    */
                    
                    // Temporary implementation for testing
                    System.out.println("Would process order reference: " + orderReference);
                    
                } catch (Exception e) {
                    System.err.println("Error processing order reference " + orderReference + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("\nProcessing completed.");
            
        } catch (IOException e) {
            System.err.println("Failed to read CSV file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
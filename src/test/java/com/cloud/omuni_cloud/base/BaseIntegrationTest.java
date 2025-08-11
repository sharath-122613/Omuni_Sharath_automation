package com.cloud.omuni_cloud.base;

import com.cloud.omuni_cloud.dbutil.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {
    
    protected static final String AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJlbWFpbF9pZCI6ImluY3JlZmZAZ21haWwuY29tIiwiYXVkIjoib2RpbiIsImZpcnN0TmFtZSI6IlVuaWNvbW1lcmNlIiwidXNlcl9pZCI6ImI0ZTViNGNkLTYxMzctNDJjZi1hMDQxLTc3YzA1ODRhOTg1YiIsInNlc3Npb24iOiIwZDE0YTZiMS1iOGNkLTRiMDAtOTBjYS1hNTJiOTAzZTQ0N2QiLCJyb2xlcyI6WyJST0xFX0JVU0lORVNTX1VTRVIiXSwiaXNzIjoiYmxhY2tib2x0IiwidGVuYW50SWQiOiI3MzkzY2UzMy04ZTRmLTQxMDYtOWZkYy0zZmRlYTVjMjdlYzkiLCJ0eXAiOiJCZWFyZXIiLCJpYXQiOjE1ODY4NDcxMTEsImp0aSI6ImNiZjRmY2E1LTkyMzYtNDI5MC04YTBjLTk5OWY2NDE0MmJjNSJ9.ookKI4uy8yfvkoYWqt8-qYU4tIM56_pQ5OnYae14jZHt5NvgV-5oCc1on-6czVb0wQBVPl-wgdHB1BvwfptGpBM9q8a2P6Y0_SwGgD-z-Z_3q0cRjUby0QwJPYARr3_onrKs2eAcC--dcUiAE1m5dQir4KkmgVfPXUEvy4qFyDVZAo5ZJsCXGWLK92nAxfy3vGr3QWOBp5KoPGaaP6vAL_jkrVNaF7SwwMDSJg7paBEFmnllMPCO90ILxOvw6anun9bI3Sn2w2Qtt980zqCsmFfHmy-gqe2SLHGVvlFeHe-_1Mi9_VFGdFJip_UH4IWkITG8SjsxIo4_0r_cpW_fug";
    protected static final String FC_ID = "Bata_3051";
    protected static final String EAN = "9287018100";
    protected static int lastOrderRefLen = 0;
    protected final String reportFile = getClass().getSimpleName() + "_" +
            ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Initialize report file
        try (PrintWriter out = new PrintWriter(reportFile)) {
            out.println("Test Execution Report - " + getClass().getSimpleName());
            out.println("-----------------------------------");
        } catch (Exception e) {
            System.err.println("Failed to initialize report file: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        // Clean up resources if needed
    }

    protected void logToReport(String message) {
        System.out.println(message);
        try (PrintWriter out = new PrintWriter(new FileWriter(reportFile, true))) {
            out.println(message);
        } catch (Exception e) {
            System.err.println("Failed to write to report file: " + e.getMessage());
        }
    }

    protected String generateOrderReference() {
        final int minLen = 11, maxLen = 17;
        if (lastOrderRefLen == 0) {
            lastOrderRefLen = minLen;
        } else if (lastOrderRefLen < maxLen) {
            lastOrderRefLen++;
        }
        String uuidPart = java.util.UUID.randomUUID().toString().replaceAll("-", "");
        String base = System.currentTimeMillis() + uuidPart;
        return "OS" + base.substring(0, lastOrderRefLen - 2);
    }
}

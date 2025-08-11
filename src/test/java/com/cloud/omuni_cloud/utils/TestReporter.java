package com.cloud.omuni_cloud.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestReporter {
    private static final String REPORTS_DIR = System.getProperty("user.dir") + File.separator + "test-reports";
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
    private static final SimpleDateFormat READABLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final String testName;
    private final List<TestStep> testSteps = new ArrayList<>();
    private final Map<String, String> testInfo = new LinkedHashMap<>();
    private final long startTime;
    private final String reportFilePath;

    public TestReporter(String testName) {
        this.testName = testName;
        this.startTime = System.currentTimeMillis();

        System.out.println("[DEBUG] Initializing TestReporter for test: " + testName);
        System.out.println("[DEBUG] Reports directory: " + REPORTS_DIR);

        // Ensure reports directory exists
        File reportsDir = new File(REPORTS_DIR);
        if (!reportsDir.exists()) {
            System.out.println("[DEBUG] Reports directory does not exist. Creating...");
            boolean dirCreated = reportsDir.mkdirs();
            if (dirCreated) {
                System.out.println("[DEBUG] Successfully created reports directory");
            } else {
                System.err.println("[ERROR] Failed to create reports directory at " + REPORTS_DIR);
                System.err.println("[ERROR] Current working directory: " + System.getProperty("user.dir"));
            }
        } else {
            System.out.println("[DEBUG] Reports directory already exists");
        }

        // Generate unique report filename with timestamp
        String timestamp = TIMESTAMP_FORMAT.format(new Date());
        String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_.]", "_");
        String reportFileName = String.format("TestReport_%s_%s.html", safeTestName, timestamp);
        this.reportFilePath = REPORTS_DIR + File.separator + reportFileName;

        System.out.println("[DEBUG] Report will be saved to: " + this.reportFilePath);

        // Add initial test information
        addSystemInfo();
    }

    public static class TestStep {
        private final String description;
        private final String status;
        private final String details;
        private final long timestamp;

        public TestStep(String description, String status, String details) {
            this.description = description;
            this.status = status;
            this.details = details != null ? details : "";
            this.timestamp = System.currentTimeMillis();
        }

        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getDetails() { return details; }
        public String getFormattedTimestamp() {
            return READABLE_DATE_FORMAT.format(new Date(timestamp));
        }
    }

    private void addSystemInfo() {
        testInfo.put("Test Name", testName);
        testInfo.put("Start Time", READABLE_DATE_FORMAT.format(new Date(startTime)));
        testInfo.put("Java Version", System.getProperty("java.version"));
        testInfo.put("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        testInfo.put("User", System.getProperty("user.name"));
    }

    public void addStep(String description, String status) {
        addStep(description, status, null);
    }

    public void addStep(String description, String status, String details) {
        testSteps.add(new TestStep(description, status, details));
    }

    private String generateHtmlContent(boolean testPassed) {
        StringBuilder html = new StringBuilder();
        String result = testPassed ? "PASSED" : "FAILED";
        String statusClass = testPassed ? "pass" : "fail";
        
        // HTML Header
        html.append("<!DOCTYPE html>\n")
           .append("<html>\n")
           .append("<head>\n")
           .append("<title>Test Report: ").append(testName).append("</title>\n")
           .append("<style>\n")
           .append("body { font-family: Arial, sans-serif; line-height: 1.6; margin: 20px; }\n")
           .append(".header { background-color: #f5f5f5; padding: 15px; margin-bottom: 20px; border-radius: 5px; }\n")
           .append(".test-info { margin-bottom: 20px; }\n")
           .append(".test-steps { width: 100%; border-collapse: collapse; margin-top: 20px; }\n")
           .append(".test-steps th, .test-steps td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
           .append(".test-steps th { background-color: #f2f2f2; }\n")
           .append(".pass { color: green; }\n")
           .append(".fail { color: red; }\n")
           .append(".info { color: blue; }\n")
           .append("</style>\n")
           .append("</head>\n")
           .append("<body>\n");

        // Test Header
        html.append("<div class='header'>\n")
           .append("<h1>Test Report: ").append(testName).append("</h1>\n")
           .append("<div class='result ").append(statusClass).append("'>Test Result: ").append(result).append("</div>\n")
           .append("</div>\n");

        // Test Information
        html.append("<div class='test-info'>\n")
           .append("<h2>Test Information</h2>\n")
           .append("<table>\n");
        
        for (Map.Entry<String, String> entry : testInfo.entrySet()) {
            html.append("<tr><td><strong>").append(entry.getKey()).append(":</strong></td><td>").append(entry.getValue()).append("</td></tr>\n");
        }
        html.append("<tr><td><strong>End Time:</strong></td><td>").append(READABLE_DATE_FORMAT.format(new Date())).append("</td></tr>\n");
        html.append("<tr><td><strong>Duration:</strong></td><td>").append((System.currentTimeMillis() - startTime) / 1000.0).append(" seconds</td></tr>\n");
        html.append("</table>\n")
           .append("</div>\n");

        // Test Steps
        if (!testSteps.isEmpty()) {
            html.append("<div class='test-steps-container'>\n")
               .append("<h2>Test Steps</h2>\n")
               .append("<table class='test-steps'>\n")
               .append("<tr><th>Time</th><th>Step</th><th>Status</th><th>Details</th></tr>\n");

            for (TestStep step : testSteps) {
                html.append("<tr>")
                   .append("<td>").append(step.getFormattedTimestamp()).append("</td>")
                   .append("<td>").append(step.getDescription()).append("</td>")
                   .append("<td class='").append(step.getStatus().toLowerCase()).append("'>").append(step.getStatus()).append("</td>")
                   .append("<td>").append(step.getDetails()).append("</td>")
                   .append("</tr>\n");
            }
            html.append("</table>\n")
               .append("</div>\n");
        }

        // Footer
        html.append("</body>\n")
           .append("</html>");

        return html.toString();
    }

    public void generateReport(boolean testPassed) {
        System.out.println("[DEBUG] Generating report. Test passed: " + testPassed);
        System.out.println("[DEBUG] Report path: " + reportFilePath);

        File reportFile = new File(reportFilePath);
        System.out.println("[DEBUG] Report file absolute path: " + reportFile.getAbsolutePath());

        // Ensure parent directories exist
        File parentDir = reportFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            System.out.println("[DEBUG] Creating parent directories: " + parentDir.getAbsolutePath());
            boolean dirsCreated = parentDir.mkdirs();
            System.out.println("[DEBUG] Parent directories created: " + dirsCreated);
        }

        try (FileWriter writer = new FileWriter(reportFile)) {
            String reportContent = generateHtmlContent(testPassed);
            writer.write(reportContent);
            System.out.println("[DEBUG] Report successfully written to: " + reportFile.getAbsolutePath());

            // Verify file was created
            if (reportFile.exists() && reportFile.length() > 0) {
                System.out.println("[DEBUG] Report file exists and has content. Size: " + reportFile.length() + " bytes");
            } else {
                System.err.println("[ERROR] Report file was not created or is empty");
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to generate test report: " + e.getMessage());
            System.err.println("[ERROR] Current working directory: " + System.getProperty("user.dir"));
            e.printStackTrace();
        }
    }

    // ... [rest of the class remains the same] ...
}

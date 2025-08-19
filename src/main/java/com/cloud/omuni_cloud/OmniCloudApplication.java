package com.cloud.omuni_cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Omni Cloud Order Management System
 */
@SpringBootApplication
@EnableScheduling
public class OmniCloudApplication {
    public static void main(String[] args) {
        SpringApplication.run(OmniCloudApplication.class, args);
    }
}

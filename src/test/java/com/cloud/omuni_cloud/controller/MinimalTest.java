package com.cloud.omuni_cloud.controller;

import com.cloud.omuni_cloud.config.OrderControllerTestMinimalConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import(OrderControllerTestMinimalConfig.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for this test
@TestPropertySource(properties = {
    "spring.main.web-application-type=servlet",
    "spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("test")
public class MinimalTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void contextLoads() throws Exception {
        // Just verify that the application context can load
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }
}

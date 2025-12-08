package io.techyowls.demo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrderAndAddItems() throws Exception {
        // Create order
        String orderJson = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"customerId": "customer-123"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.customerId").value("customer-123"))
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String orderId = objectMapper.readTree(orderJson).get("id").asText();

        // Add item
        mockMvc.perform(post("/api/orders/{orderId}/items", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "productId": "prod-1",
                        "productName": "Widget",
                        "quantity": 2,
                        "unitPrice": 10.00
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itemCount").value(2))
            .andExpect(jsonPath("$.total").value(20.00));

        // Submit order
        mockMvc.perform(post("/api/orders/{orderId}/submit", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void shouldRejectEmptyOrderSubmission() throws Exception {
        // Create empty order
        String orderJson = mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"customerId": "customer-123"}
                    """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String orderId = objectMapper.readTree(orderJson).get("id").asText();

        // Try to submit empty order
        mockMvc.perform(post("/api/orders/{orderId}/submit", orderId))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Invalid Operation"))
            .andExpect(jsonPath("$.detail").value(containsString("empty")));
    }

    @Test
    void shouldReturn404ForNonExistentOrder() throws Exception {
        mockMvc.perform(post("/api/orders/non-existent/submit"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Order Not Found"));
    }
}

package io.techyowls.streams.examples;

import io.techyowls.streams.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CollectorExamplesTest {

    private CollectorExamples collectors;
    private List<Order> orders;

    @BeforeEach
    void setUp() {
        collectors = new CollectorExamples();
        orders = createTestOrders();
    }

    @Test
    void groupByStatus() {
        Map<OrderStatus, List<Order>> result = collectors.groupByStatus(orders);

        assertEquals(2, result.get(OrderStatus.PENDING).size());
        assertEquals(1, result.get(OrderStatus.SHIPPED).size());
        assertEquals(1, result.get(OrderStatus.DELIVERED).size());
    }

    @Test
    void countByStatus() {
        Map<OrderStatus, Long> result = collectors.countByStatus(orders);

        assertEquals(2L, result.get(OrderStatus.PENDING));
        assertEquals(1L, result.get(OrderStatus.SHIPPED));
        assertEquals(1L, result.get(OrderStatus.DELIVERED));
    }

    @Test
    void totalByStatus() {
        Map<OrderStatus, BigDecimal> result = collectors.totalByStatus(orders);

        // Pending orders: 150 + 75 = 225
        assertEquals(0, new BigDecimal("225.00").compareTo(result.get(OrderStatus.PENDING)));
    }

    @Test
    void partitionByHighValue() {
        Map<Boolean, List<Order>> result = collectors.partitionByHighValue(orders, new BigDecimal("100"));

        assertEquals(2, result.get(true).size());  // High value orders
        assertEquals(2, result.get(false).size()); // Low value orders
    }

    @Test
    void orderLookupById() {
        Map<String, Order> result = collectors.orderLookupById(orders);

        assertEquals(4, result.size());
        assertNotNull(result.get("ORD-001"));
        assertEquals("CUST-001", result.get("ORD-001").customerId());
    }

    @Test
    void totalSpendByCustomer() {
        Map<String, BigDecimal> result = collectors.totalSpendByCustomer(orders);

        // CUST-001 has orders: 150 + 200 = 350
        assertEquals(0, new BigDecimal("350.00").compareTo(result.get("CUST-001")));
    }

    @Test
    void customerIdList() {
        String result = collectors.customerIdList(orders);

        assertTrue(result.contains("CUST-001"));
        assertTrue(result.contains("CUST-002"));
        assertTrue(result.contains(", "));
    }

    @Test
    void customersByStatus() {
        Map<OrderStatus, Set<String>> result = collectors.customersByStatus(orders);

        assertTrue(result.get(OrderStatus.PENDING).contains("CUST-001"));
        assertTrue(result.get(OrderStatus.PENDING).contains("CUST-002"));
    }

    @Test
    void computeStats() {
        CollectorExamples.OrderStats result = collectors.computeStats(orders);

        assertEquals(4, result.count());
        assertEquals(0, new BigDecimal("525.00").compareTo(result.total()));
    }

    private List<Order> createTestOrders() {
        return List.of(
            new Order("ORD-001", "CUST-001", LocalDate.now(), OrderStatus.PENDING,
                List.of(new OrderItem("PROD-1", "Widget", 3, new BigDecimal("50.00")))),
            new Order("ORD-002", "CUST-002", LocalDate.now(), OrderStatus.PENDING,
                List.of(new OrderItem("PROD-2", "Gadget", 1, new BigDecimal("75.00")))),
            new Order("ORD-003", "CUST-001", LocalDate.now().minusDays(5), OrderStatus.SHIPPED,
                List.of(new OrderItem("PROD-1", "Widget", 4, new BigDecimal("50.00")))),
            new Order("ORD-004", "CUST-003", LocalDate.now().minusDays(10), OrderStatus.DELIVERED,
                List.of(new OrderItem("PROD-3", "Thingamajig", 2, new BigDecimal("50.00"))))
        );
    }
}

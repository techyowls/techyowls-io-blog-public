package io.techyowls.streams.examples;

import io.techyowls.streams.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;

/**
 * Real-world stream patterns for business logic
 */
public class RealWorldPatterns {

    // DTO transformation
    public record OrderSummary(String orderId, String status, BigDecimal total) {}

    public List<OrderSummary> toSummaries(List<Order> orders) {
        return orders.stream()
            .map(o -> new OrderSummary(o.id(), o.status().name(), o.total()))
            .toList();
    }

    // Find top N by criteria
    public List<Order> topOrdersByTotal(List<Order> orders, int n) {
        return orders.stream()
            .sorted(comparing(Order::total).reversed())
            .limit(n)
            .toList();
    }

    // Find first matching or throw
    public Order findOrderOrThrow(List<Order> orders, String orderId) {
        return orders.stream()
            .filter(o -> o.id().equals(orderId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
    }

    // Check conditions
    public boolean allOrdersShipped(List<Order> orders) {
        return orders.stream()
            .allMatch(o -> o.status() == OrderStatus.SHIPPED
                       || o.status() == OrderStatus.DELIVERED);
    }

    public boolean hasAnyPendingOrder(List<Order> orders) {
        return orders.stream()
            .anyMatch(o -> o.status() == OrderStatus.PENDING);
    }

    public boolean noCancelledOrders(List<Order> orders) {
        return orders.stream()
            .noneMatch(o -> o.status() == OrderStatus.CANCELLED);
    }

    // Complex aggregation: revenue by month
    public record MonthlyRevenue(int year, int month, BigDecimal revenue) {}

    public List<MonthlyRevenue> monthlyRevenue(List<Order> orders) {
        return orders.stream()
            .filter(o -> o.status() != OrderStatus.CANCELLED)
            .collect(groupingBy(
                o -> o.orderDate().withDayOfMonth(1),
                reducing(BigDecimal.ZERO, Order::total, BigDecimal::add)
            ))
            .entrySet().stream()
            .map(e -> new MonthlyRevenue(
                e.getKey().getYear(),
                e.getKey().getMonthValue(),
                e.getValue()
            ))
            .sorted(comparing(MonthlyRevenue::year)
                .thenComparing(MonthlyRevenue::month))
            .toList();
    }

    // Customer analytics: top customers by spend
    public record CustomerSpend(String customerId, BigDecimal totalSpent, long orderCount) {}

    public List<CustomerSpend> topCustomers(List<Order> orders, int limit) {
        return orders.stream()
            .filter(o -> o.status() != OrderStatus.CANCELLED)
            .collect(groupingBy(Order::customerId))
            .entrySet().stream()
            .map(e -> new CustomerSpend(
                e.getKey(),
                e.getValue().stream()
                    .map(Order::total)
                    .reduce(BigDecimal.ZERO, BigDecimal::add),
                e.getValue().size()
            ))
            .sorted(comparing(CustomerSpend::totalSpent).reversed())
            .limit(limit)
            .toList();
    }

    // Product analytics: best selling products
    public record ProductSales(String productId, String productName, int totalQuantity) {}

    public List<ProductSales> bestSellingProducts(List<Order> orders, int limit) {
        return orders.stream()
            .filter(o -> o.status() != OrderStatus.CANCELLED)
            .flatMap(o -> o.items().stream())
            .collect(groupingBy(
                item -> item.productId() + "|" + item.productName(),
                summingInt(OrderItem::quantity)
            ))
            .entrySet().stream()
            .map(e -> {
                String[] parts = e.getKey().split("\\|");
                return new ProductSales(parts[0], parts[1], e.getValue());
            })
            .sorted(comparing(ProductSales::totalQuantity).reversed())
            .limit(limit)
            .toList();
    }

    // Filter with multiple optional criteria
    public List<Order> searchOrders(
            List<Order> orders,
            Optional<String> customerId,
            Optional<OrderStatus> status,
            Optional<LocalDate> fromDate,
            Optional<LocalDate> toDate) {

        return orders.stream()
            .filter(o -> customerId.map(c -> o.customerId().equals(c)).orElse(true))
            .filter(o -> status.map(s -> o.status() == s).orElse(true))
            .filter(o -> fromDate.map(d -> !o.orderDate().isBefore(d)).orElse(true))
            .filter(o -> toDate.map(d -> !o.orderDate().isAfter(d)).orElse(true))
            .toList();
    }

    // Batch processing with chunking
    public <T> List<List<T>> chunk(List<T> items, int chunkSize) {
        return java.util.stream.IntStream.range(0, items.size())
            .boxed()
            .collect(groupingBy(i -> i / chunkSize))
            .values().stream()
            .map(indices -> indices.stream()
                .map(items::get)
                .toList())
            .toList();
    }

    // Deduplication by key
    public List<Order> latestOrderPerCustomer(List<Order> orders) {
        return orders.stream()
            .collect(toMap(
                Order::customerId,
                o -> o,
                (o1, o2) -> o1.orderDate().isAfter(o2.orderDate()) ? o1 : o2
            ))
            .values().stream()
            .toList();
    }

    // Null-safe processing
    public List<String> safeExtractEmails(List<Customer> customers) {
        return customers.stream()
            .filter(Objects::nonNull)
            .map(Customer::email)
            .filter(Objects::nonNull)
            .filter(email -> email.contains("@"))
            .distinct()
            .toList();
    }
}

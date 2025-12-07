package io.techyowls.streams.examples;

import io.techyowls.streams.model.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * Advanced collectors: groupingBy, partitioningBy, toMap, reducing
 */
public class CollectorExamples {

    // Group orders by status
    public Map<OrderStatus, List<Order>> groupByStatus(List<Order> orders) {
        return orders.stream()
            .collect(groupingBy(Order::status));
    }

    // Group and count
    public Map<OrderStatus, Long> countByStatus(List<Order> orders) {
        return orders.stream()
            .collect(groupingBy(Order::status, counting()));
    }

    // Group and sum totals
    public Map<OrderStatus, BigDecimal> totalByStatus(List<Order> orders) {
        return orders.stream()
            .collect(groupingBy(
                Order::status,
                reducing(BigDecimal.ZERO, Order::total, BigDecimal::add)
            ));
    }

    // Multi-level grouping: status -> customer -> orders
    public Map<OrderStatus, Map<String, List<Order>>> groupByStatusAndCustomer(List<Order> orders) {
        return orders.stream()
            .collect(groupingBy(
                Order::status,
                groupingBy(Order::customerId)
            ));
    }

    // Partition into two groups (boolean condition)
    public Map<Boolean, List<Order>> partitionByHighValue(List<Order> orders, BigDecimal threshold) {
        return orders.stream()
            .collect(partitioningBy(
                order -> order.total().compareTo(threshold) > 0
            ));
    }

    // toMap - create lookup by ID
    public Map<String, Order> orderLookupById(List<Order> orders) {
        return orders.stream()
            .collect(toMap(
                Order::id,
                Function.identity()
            ));
    }

    // toMap with merge function (handle duplicates)
    public Map<String, BigDecimal> totalSpendByCustomer(List<Order> orders) {
        return orders.stream()
            .collect(toMap(
                Order::customerId,
                Order::total,
                BigDecimal::add  // merge function for duplicate keys
            ));
    }

    // toMap with specific map implementation
    public Map<String, Order> orderedLookup(List<Order> orders) {
        return orders.stream()
            .collect(toMap(
                Order::id,
                Function.identity(),
                (existing, replacement) -> existing,
                LinkedHashMap::new  // preserve insertion order
            ));
    }

    // Collect to specific collection type
    public TreeSet<String> sortedUniqueCustomers(List<Order> orders) {
        return orders.stream()
            .map(Order::customerId)
            .collect(toCollection(TreeSet::new));
    }

    // Join strings
    public String customerIdList(List<Order> orders) {
        return orders.stream()
            .map(Order::customerId)
            .distinct()
            .collect(joining(", "));
    }

    // Summarizing statistics
    public DoubleSummaryStatistics orderTotalStats(List<Order> orders) {
        return orders.stream()
            .map(Order::total)
            .collect(summarizingDouble(BigDecimal::doubleValue));
    }

    // Mapping collector - transform then collect
    public Map<OrderStatus, Set<String>> customersByStatus(List<Order> orders) {
        return orders.stream()
            .collect(groupingBy(
                Order::status,
                mapping(Order::customerId, toSet())
            ));
    }

    // Filtering collector (Java 9+)
    public Map<String, Long> countDeliveredByCustomer(List<Order> orders) {
        return orders.stream()
            .collect(groupingBy(
                Order::customerId,
                filtering(
                    o -> o.status() == OrderStatus.DELIVERED,
                    counting()
                )
            ));
    }

    // FlatMapping collector (Java 9+)
    public Map<String, Set<String>> productsByCustomer(List<Order> orders) {
        return orders.stream()
            .collect(groupingBy(
                Order::customerId,
                flatMapping(
                    order -> order.items().stream().map(OrderItem::productId),
                    toSet()
                )
            ));
    }

    // Teeing collector (Java 12+) - two collectors in parallel
    public record OrderStats(long count, BigDecimal total) {}

    public OrderStats computeStats(List<Order> orders) {
        return orders.stream()
            .collect(teeing(
                counting(),
                reducing(BigDecimal.ZERO, Order::total, BigDecimal::add),
                OrderStats::new
            ));
    }

    // Custom collector - comma-separated with prefix/suffix
    public String formatCustomerList(List<Customer> customers) {
        return customers.stream()
            .map(Customer::name)
            .collect(joining(", ", "Customers: [", "]"));
    }
}

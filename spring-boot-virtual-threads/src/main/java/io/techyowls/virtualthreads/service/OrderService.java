package io.techyowls.virtualthreads.service;

import io.techyowls.virtualthreads.exception.OrderException;
import io.techyowls.virtualthreads.model.Customer;
import io.techyowls.virtualthreads.model.Order;
import io.techyowls.virtualthreads.model.OrderItem;
import io.techyowls.virtualthreads.model.dto.*;
import io.techyowls.virtualthreads.repository.CustomerRepository;
import io.techyowls.virtualthreads.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Main Order Service that orchestrates the order creation process.
 * Demonstrates how virtual threads handle multiple sequential I/O operations.
 *
 * Order Flow:
 * 1. Validate customer (DB)
 * 2. Check inventory (External API)
 * 3. Reserve stock (External API)
 * 4. Process payment (External API)
 * 5. Create order (DB)
 * 6. Send notification (Async)
 */
@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        InventoryService inventoryService,
                        PaymentService paymentService,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }

    /**
     * Create a new order.
     * This method demonstrates sequential I/O operations that benefit from virtual threads.
     * Total I/O time: ~50ms + ~50ms + ~200ms + ~100ms = ~400ms
     * With virtual threads, the carrier thread is released during each wait.
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Creating order for customer {} on thread: {} (virtual: {})",
            request.customerId(),
            Thread.currentThread().getName(),
            Thread.currentThread().isVirtual());

        // Step 1: Validate customer (DB call - ~20ms)
        Customer customer = customerRepository.findById(request.customerId())
            .filter(Customer::isActive)
            .orElseThrow(() -> new OrderException("Customer not found or inactive: " + request.customerId()));

        log.debug("Step 1 complete: Customer validated");

        // Step 2: Check inventory (External API - ~50ms)
        InventoryService.InventoryCheckResult inventoryResult =
            inventoryService.checkAvailability(request.items());

        if (!inventoryResult.allAvailable()) {
            throw new OrderException("Items unavailable: " + inventoryResult.unavailableItems());
        }

        log.debug("Step 2 complete: Inventory checked");

        // Step 3: Calculate total
        BigDecimal totalAmount = calculateTotal(inventoryResult.itemPrices(), request.items());

        // Step 4: Create pending order (DB call)
        Order order = createPendingOrder(customer.getId(), request.items(), totalAmount, inventoryResult);
        order = orderRepository.save(order);

        log.debug("Step 3 complete: Order created with ID {}", order.getId());

        try {
            // Step 5: Reserve stock (External API - ~50ms)
            inventoryService.reserveStock(order.getId(), request.items());
            log.debug("Step 4 complete: Stock reserved");

            // Step 6: Process payment (External API - ~200ms - slowest operation)
            PaymentService.PaymentResult paymentResult = paymentService.processPayment(
                new PaymentService.PaymentRequest(
                    request.paymentInfo().cardToken(),
                    totalAmount,
                    order.getId().toString()
                )
            );

            if (!paymentResult.success()) {
                // Compensation: release stock
                inventoryService.releaseStock(order.getId());
                order.setStatus("FAILED");
                orderRepository.save(order);
                throw new OrderException("Payment failed: " + paymentResult.errorMessage());
            }

            log.debug("Step 5 complete: Payment processed");

            // Step 7: Update order status
            order.setPaymentId(paymentResult.transactionId());
            order.setStatus("CONFIRMED");
            order = orderRepository.save(order);

            // Step 8: Send confirmation (async - doesn't block response)
            notificationService.sendOrderConfirmation(order, customer);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Order {} created successfully in {}ms", order.getId(), duration);

            return OrderResponse.from(order);

        } catch (Exception e) {
            log.error("Order creation failed for customer {}", request.customerId(), e);
            order.setStatus("FAILED");
            orderRepository.save(order);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Optional<OrderResponse> findById(Long id) {
        return orderRepository.findById(id)
            .map(OrderResponse::from);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
            .map(OrderResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
            .map(OrderResponse::from)
            .toList();
    }

    private Order createPendingOrder(Long customerId, List<OrderItemRequest> items,
                                      BigDecimal total, InventoryService.InventoryCheckResult inventory) {
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setTotalAmount(total);

        for (OrderItemRequest item : items) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.productId());
            orderItem.setQuantity(item.quantity());
            orderItem.setProductName(inventory.getProductName(item.productId()));
            orderItem.setUnitPrice(inventory.getPrice(item.productId()));
            order.addItem(orderItem);
        }

        return order;
    }

    private BigDecimal calculateTotal(Map<String, BigDecimal> prices, List<OrderItemRequest> items) {
        return items.stream()
            .map(item -> prices.get(item.productId()).multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

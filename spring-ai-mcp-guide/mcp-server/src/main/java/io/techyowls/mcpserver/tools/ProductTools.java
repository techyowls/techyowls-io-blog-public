package io.techyowls.mcpserver.tools;

import io.techyowls.mcpserver.model.InventoryStatus;
import io.techyowls.mcpserver.model.Order;
import io.techyowls.mcpserver.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP Tools for product operations.
 * These methods are exposed as tools that the LLM can call.
 */
public class ProductTools {

    private static final Logger log = LoggerFactory.getLogger(ProductTools.class);

    // In-memory "database" for demo purposes
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    public ProductTools() {
        // Initialize with sample products
        initializeSampleProducts();
    }

    private void initializeSampleProducts() {
        addProduct(new Product("LAPTOP-001", "ProBook 15", "Electronics",
                "15-inch laptop with 16GB RAM and 512GB SSD", new BigDecimal("999.99"), 25));
        addProduct(new Product("LAPTOP-002", "UltraBook Air", "Electronics",
                "13-inch ultralight laptop for professionals", new BigDecimal("1299.99"), 15));
        addProduct(new Product("PHONE-001", "SmartPhone X", "Electronics",
                "Latest smartphone with 128GB storage", new BigDecimal("799.99"), 50));
        addProduct(new Product("HEADPHONES-001", "Wireless Pro", "Audio",
                "Noise-canceling wireless headphones", new BigDecimal("299.99"), 100));
        addProduct(new Product("KEYBOARD-001", "MechBoard", "Accessories",
                "Mechanical keyboard with RGB lighting", new BigDecimal("149.99"), 75));
        addProduct(new Product("MOUSE-001", "ErgoMouse", "Accessories",
                "Ergonomic wireless mouse", new BigDecimal("79.99"), 200));
        addProduct(new Product("MONITOR-001", "UltraWide 34", "Electronics",
                "34-inch curved ultrawide monitor", new BigDecimal("599.99"), 10));
        addProduct(new Product("WEBCAM-001", "HD Stream", "Accessories",
                "1080p webcam for streaming", new BigDecimal("89.99"), 0)); // Out of stock
    }

    private void addProduct(Product product) {
        products.put(product.sku(), product);
    }

    // ===== MCP TOOLS =====

    @Tool(description = "Search for products by name, category, or description. Returns a list of matching products with details and prices.")
    public List<Product> searchProducts(
            @ToolParam(description = "Search query - can be product name, category, or keyword") String query,
            @ToolParam(description = "Maximum number of results to return (default 10)") Integer limit) {

        log.info("Searching products with query: '{}', limit: {}", query, limit);

        int maxResults = (limit != null && limit > 0) ? limit : 10;
        String searchLower = query.toLowerCase();

        List<Product> results = products.values().stream()
                .filter(p -> p.name().toLowerCase().contains(searchLower) ||
                        p.category().toLowerCase().contains(searchLower) ||
                        p.description().toLowerCase().contains(searchLower))
                .limit(maxResults)
                .toList();

        log.info("Found {} products matching '{}'", results.size(), query);
        return results;
    }

    @Tool(description = "Get detailed information about a specific product by its SKU (stock keeping unit)")
    public Product getProductDetails(
            @ToolParam(description = "Product SKU (e.g., LAPTOP-001)") String sku) {

        log.info("Getting product details for SKU: {}", sku);

        Product product = products.get(sku.toUpperCase());
        if (product == null) {
            log.warn("Product not found: {}", sku);
            throw new IllegalArgumentException("Product not found: " + sku);
        }
        return product;
    }

    @Tool(description = "Check current inventory/stock level for a product. Returns availability status.")
    public InventoryStatus checkInventory(
            @ToolParam(description = "Product SKU to check inventory for") String sku) {

        log.info("Checking inventory for SKU: {}", sku);

        Product product = products.get(sku.toUpperCase());
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + sku);
        }

        return InventoryStatus.fromProduct(product);
    }

    @Tool(description = "Place an order for a product. Requires product SKU, quantity, and customer email.")
    public Order placeOrder(
            @ToolParam(description = "Product SKU to order") String sku,
            @ToolParam(description = "Quantity to order") int quantity,
            @ToolParam(description = "Customer email for order confirmation") String email) {

        log.info("Placing order - SKU: {}, Qty: {}, Email: {}", sku, quantity, email);

        // Validate
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }

        Product product = products.get(sku.toUpperCase());
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + sku);
        }
        if (product.stockQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Available: " + product.stockQuantity());
        }

        // Create order
        Order order = Order.create(sku, quantity, product.price(), email);
        orders.put(order.orderId(), order);

        // Update stock (in real app, this would be transactional)
        Product updatedProduct = new Product(
                product.sku(), product.name(), product.category(),
                product.description(), product.price(),
                product.stockQuantity() - quantity
        );
        products.put(product.sku(), updatedProduct);

        log.info("Order created: {}", order.orderId());
        return order;
    }

    @Tool(description = "Get order status by order ID")
    public Order getOrderStatus(
            @ToolParam(description = "Order ID to check") String orderId) {

        log.info("Getting order status for: {}", orderId);

        Order order = orders.get(orderId.toUpperCase());
        if (order == null) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return order;
    }

    @Tool(description = "List all available product categories")
    public List<String> getCategories() {
        log.info("Getting all categories");

        return products.values().stream()
                .map(Product::category)
                .distinct()
                .sorted()
                .toList();
    }

    @Tool(description = "Get products that are low in stock or out of stock")
    public List<InventoryStatus> getLowStockProducts() {
        log.info("Getting low stock products");

        return products.values().stream()
                .filter(p -> p.stockQuantity() <= 10)
                .map(InventoryStatus::fromProduct)
                .toList();
    }
}

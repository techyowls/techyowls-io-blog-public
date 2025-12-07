package io.techyowls.mcpserver.model;

/**
 * Inventory status response.
 */
public record InventoryStatus(
        String sku,
        String productName,
        int available,
        int reserved,
        String status
) {
    public static InventoryStatus fromProduct(Product product) {
        String status = product.stockQuantity() > 10 ? "IN_STOCK" :
                product.stockQuantity() > 0 ? "LOW_STOCK" : "OUT_OF_STOCK";

        return new InventoryStatus(
                product.sku(),
                product.name(),
                product.stockQuantity(),
                0,
                status
        );
    }
}

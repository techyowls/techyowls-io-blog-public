package io.techyowls.structuredoutput.service;

import io.techyowls.structuredoutput.model.Product;
import jakarta.validation.Validator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Generate product catalogs with validated output.
 */
@Service
public class ProductCatalogService {

    private final ChatClient chatClient;
    private final Validator validator;

    public ProductCatalogService(ChatModel chatModel, Validator validator) {
        this.chatClient = ChatClient.create(chatModel);
        this.validator = validator;
    }

    /**
     * Generate products for a specific category.
     */
    public List<Product> generateCatalog(String category, int count) {
        String prompt = """
            Generate %d products for a %s store.
            Each product should have realistic:
            - SKU (format: CAT-XXXXX)
            - Name
            - Description (1-2 sentences)
            - Price (realistic for the category)
            - Category: %s
            - Tags (3-5 relevant tags)
            """.formatted(count, category, category);

        List<Product> products = chatClient.prompt()
            .user(prompt)
            .call()
            .entity(new ParameterizedTypeReference<List<Product>>() {});

        // Validate each product
        for (Product product : products) {
            var violations = validator.validate(product);
            if (!violations.isEmpty()) {
                System.err.println("Validation failed for " + product.name() + ": " + violations);
            }
        }

        return products;
    }

    /**
     * Generate a single product.
     */
    public Product generateProduct(String description) {
        return chatClient.prompt()
            .user("Create a product: " + description)
            .call()
            .entity(Product.class);
    }
}

package io.techyowls.structuredoutput.controller;

import io.techyowls.structuredoutput.model.Character;
import io.techyowls.structuredoutput.model.Product;
import io.techyowls.structuredoutput.service.CharacterGeneratorService;
import io.techyowls.structuredoutput.service.ProductCatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StructuredOutputController {

    private final CharacterGeneratorService characterService;
    private final ProductCatalogService productService;

    public StructuredOutputController(
            CharacterGeneratorService characterService,
            ProductCatalogService productService) {
        this.characterService = characterService;
        this.productService = productService;
    }

    /**
     * Generate a D&D character.
     * GET /api/character?race=Elf
     */
    @GetMapping("/character")
    public Character generateCharacter(@RequestParam(defaultValue = "Elf") String race) {
        return characterService.generateCharacter(race);
    }

    /**
     * Generate a party of characters.
     * GET /api/party?count=4
     */
    @GetMapping("/party")
    public List<Character> generateParty(@RequestParam(defaultValue = "4") int count) {
        return characterService.generateParty(count);
    }

    /**
     * Generate products for a category.
     * GET /api/products?category=Electronics&count=5
     */
    @GetMapping("/products")
    public List<Product> generateProducts(
            @RequestParam(defaultValue = "Electronics") String category,
            @RequestParam(defaultValue = "5") int count) {
        return productService.generateCatalog(category, count);
    }

    /**
     * Generate a single product.
     * POST /api/product
     * Body: "A portable Bluetooth speaker"
     */
    @PostMapping("/product")
    public Product generateProduct(@RequestBody String description) {
        return productService.generateProduct(description);
    }
}

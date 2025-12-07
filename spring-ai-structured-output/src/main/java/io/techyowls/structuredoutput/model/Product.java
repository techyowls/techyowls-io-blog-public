package io.techyowls.structuredoutput.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product record with validation - used for catalog generation.
 */
public record Product(
    @NotBlank String sku,
    @NotBlank String name,
    String description,
    @Positive BigDecimal price,
    String category,
    @Size(min = 1, max = 10) List<String> tags
) {}

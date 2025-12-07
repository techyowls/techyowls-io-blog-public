package io.techyowls.streams.model;

public record Customer(
    String id,
    String name,
    String email,
    String tier  // BRONZE, SILVER, GOLD, PLATINUM
) {}

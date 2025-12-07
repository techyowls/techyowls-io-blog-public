package io.techyowls.structuredoutput.model;

/**
 * D&D Character record - used for BeanOutputConverter examples.
 */
public record Character(
    String name,
    int age,
    String race,
    String characterClass,
    String bio
) {}

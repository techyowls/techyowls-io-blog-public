package io.techyowls.virtualthreads.model.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentInfo(
    @NotBlank(message = "Card token is required")
    String cardToken,

    String billingAddress
) {}

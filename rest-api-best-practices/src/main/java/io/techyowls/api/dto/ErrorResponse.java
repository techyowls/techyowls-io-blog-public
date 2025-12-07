package io.techyowls.api.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
    String code,
    String message,
    List<FieldError> errors,
    String traceId,
    Instant timestamp
) {
    public ErrorResponse(String code, String message, List<FieldError> errors) {
        this(code, message, errors, null, Instant.now());
    }
}

record FieldError(String field, String message) {}

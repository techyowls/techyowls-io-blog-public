package io.techyowls.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Generic paginated response wrapper.
 */
@Schema(description = "Paginated response")
public record PageResponse<T>(

    @Schema(description = "Page content")
    List<T> content,

    @Schema(description = "Current page number (0-indexed)", example = "0")
    int page,

    @Schema(description = "Page size", example = "20")
    int size,

    @Schema(description = "Total number of elements", example = "150")
    long totalElements,

    @Schema(description = "Total number of pages", example = "8")
    int totalPages,

    @Schema(description = "Whether this is the first page", example = "true")
    boolean first,

    @Schema(description = "Whether this is the last page", example = "false")
    boolean last

) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(
            content,
            page,
            size,
            totalElements,
            totalPages,
            page == 0,
            page >= totalPages - 1
        );
    }
}

package io.github.kbdemiranda.customer.onboarding.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Generic paginated response")
public record PageResponse<T>(
        @Schema(description = "Current page content")
        List<T> content,
        @Schema(description = "Current page index (0-based)", example = "0")
        int page,
        @Schema(description = "Requested page size", example = "10")
        int size,
        @Schema(description = "Total number of elements", example = "1")
        long totalElements,
        @Schema(description = "Total number of pages", example = "1")
        int totalPages
) {
}

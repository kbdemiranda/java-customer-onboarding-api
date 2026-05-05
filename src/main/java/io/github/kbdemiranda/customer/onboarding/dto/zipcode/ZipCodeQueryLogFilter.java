package io.github.kbdemiranda.customer.onboarding.dto.zipcode;

import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeProvider;
import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeQueryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "Filters and pagination for zip code query logs")
public record ZipCodeQueryLogFilter(
        @Schema(description = "Optional zip code filter", example = "01001000")
        String zipCode,
        @Schema(description = "Optional provider filter", example = "WIREMOCK")
        ZipCodeProvider provider,
        @Schema(description = "Optional query status filter", example = "SUCCESS")
        ZipCodeQueryStatus status,
        @Schema(description = "Optional start date-time filter (inclusive)", example = "2026-05-01T00:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
        @Schema(description = "Optional end date-time filter (inclusive)", example = "2026-05-10T23:59:59")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
        @Schema(description = "Page index (0-based)", example = "0", defaultValue = "0")
        @Min(value = 0, message = "page must be greater than or equal to 0") Integer page,
        @Schema(description = "Page size", example = "10", defaultValue = "10")
        @Min(value = 1, message = "size must be greater than 0")
        @Max(value = 100, message = "size must be less than or equal to 100") Integer size
) {
    public ZipCodeQueryLogFilter {
        page = page == null ? 0 : page;
        size = size == null ? 10 : size;
    }
}

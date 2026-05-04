package io.github.kbdemiranda.customer.onboarding.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Standard API error response")
public record ErrorResponse(
        @Schema(description = "Error timestamp with timezone", example = "2026-05-04T15:12:31.300-03:00")
        OffsetDateTime timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "HTTP status reason", example = "Bad Request")
        String error,
        @Schema(description = "Human-readable error message", example = "Invalid value for parameter: externalId")
        String message,
        @Schema(description = "Request path", example = "/api/v1/onboardings/not-a-uuid")
        String path
) {
}

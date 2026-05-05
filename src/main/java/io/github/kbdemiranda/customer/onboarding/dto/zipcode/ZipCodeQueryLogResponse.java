package io.github.kbdemiranda.customer.onboarding.dto.zipcode;

import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeProvider;
import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeQueryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Zip code query log response")
public record ZipCodeQueryLogResponse(
        UUID externalId,
        String zipCode,
        ZipCodeProvider provider,
        ZipCodeQueryStatus status,
        LocalDateTime requestTimestamp,
        String responseBody,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

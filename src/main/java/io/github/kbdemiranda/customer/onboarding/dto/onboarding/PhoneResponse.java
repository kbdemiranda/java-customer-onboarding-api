package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Phone response")
public record PhoneResponse(
        @Schema(description = "Public phone identifier")
        UUID externalId,
        @Schema(description = "Phone number")
        String phoneNumber,
        @Schema(description = "Indicates whether this is the primary phone")
        Boolean primaryPhone
) {
}

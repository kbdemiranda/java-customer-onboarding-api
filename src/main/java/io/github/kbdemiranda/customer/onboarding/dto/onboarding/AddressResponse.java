package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Address response")
public record AddressResponse(
        @Schema(description = "Public address identifier")
        UUID externalId,
        @Schema(description = "Address zip code")
        String zipCode,
        @Schema(description = "Street name")
        String street,
        @Schema(description = "Address number")
        String number,
        @Schema(description = "Address complement")
        String complement,
        @Schema(description = "Neighborhood")
        String neighborhood,
        @Schema(description = "City")
        String city,
        @Schema(description = "State")
        String state,
        @Schema(description = "Indicates whether this is the primary address")
        Boolean primaryAddress
) {
}

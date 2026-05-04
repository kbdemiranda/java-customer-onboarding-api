package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Address payload for onboarding creation")
public record AddressRequest(
        @Schema(description = "Address zip code", example = "01001-000")
        @NotBlank String zipCode,
        @Schema(description = "Address number", example = "100")
        @NotBlank String number,
        @Schema(description = "Address complement", example = "Apt 10")
        String complement,
        @Schema(description = "Defines whether this is the primary address", example = "true")
        @NotNull Boolean primaryAddress
) {
}

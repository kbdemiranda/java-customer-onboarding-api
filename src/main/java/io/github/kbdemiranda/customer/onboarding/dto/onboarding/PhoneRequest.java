package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Phone payload for onboarding creation")
public record PhoneRequest(
        @Schema(description = "Phone number", example = "11999999999")
        @NotBlank String phoneNumber,
        @Schema(description = "Defines whether this is the primary phone", example = "false")
        @NotNull Boolean primaryPhone
) {
}

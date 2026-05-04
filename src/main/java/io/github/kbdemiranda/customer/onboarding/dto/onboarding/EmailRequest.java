package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Email payload for onboarding creation")
public record EmailRequest(
        @Schema(description = "Email address", example = "john@example.com")
        @NotBlank @Email String email,
        @Schema(description = "Defines whether this is the primary email", example = "true")
        @NotNull Boolean primaryEmail
) {
}

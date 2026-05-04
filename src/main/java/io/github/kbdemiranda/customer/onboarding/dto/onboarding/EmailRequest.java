package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EmailRequest(
        @NotBlank @Email String email,
        @NotNull Boolean primaryEmail
) {
}

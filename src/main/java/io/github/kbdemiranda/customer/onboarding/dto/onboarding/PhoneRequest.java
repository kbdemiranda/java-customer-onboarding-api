package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PhoneRequest(
        @NotBlank String phoneNumber,
        @NotNull Boolean primaryPhone
) {
}

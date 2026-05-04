package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequest(
        @NotBlank String zipCode,
        @NotBlank String number,
        String complement,
        @NotNull Boolean primaryAddress
) {
}

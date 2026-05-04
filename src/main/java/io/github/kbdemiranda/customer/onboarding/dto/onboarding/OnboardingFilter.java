package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record OnboardingFilter(
        String cpf,
        OnboardingStatus status,
        @Min(value = 0, message = "page must be greater than or equal to 0") Integer page,
        @Min(value = 1, message = "size must be greater than 0")
        @Max(value = 100, message = "size must be less than or equal to 100") Integer size
) {
    public OnboardingFilter {
        page = page == null ? 0 : page;
        size = size == null ? 10 : size;
    }
}

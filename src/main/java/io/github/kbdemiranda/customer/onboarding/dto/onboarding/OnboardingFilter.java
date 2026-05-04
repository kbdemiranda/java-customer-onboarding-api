package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Filters and pagination for onboarding listing")
public record OnboardingFilter(
        @Schema(description = "Optional CPF filter (formatted or digits only)", example = "123.456.789-09")
        String cpf,
        @Schema(description = "Optional onboarding status filter", example = "DOCUMENTS_PENDING")
        OnboardingStatus status,
        @Schema(description = "Page index (0-based)", example = "0", defaultValue = "0")
        @Min(value = 0, message = "page must be greater than or equal to 0") Integer page,
        @Schema(description = "Page size", example = "10", defaultValue = "10")
        @Min(value = 1, message = "size must be greater than 0")
        @Max(value = 100, message = "size must be less than or equal to 100") Integer size
) {
    public OnboardingFilter {
        page = page == null ? 0 : page;
        size = size == null ? 10 : size;
    }
}

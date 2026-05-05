package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Onboarding response")
public record OnboardingResponse(
        @Schema(description = "Public onboarding identifier")
        UUID externalId,
        @Schema(description = "Onboarding protocol used by end users", example = "58392017463051")
        String protocol,
        @Schema(description = "Customer full name")
        String fullName,
        @Schema(description = "Normalized customer CPF")
        String cpf,
        @Schema(description = "Current onboarding status")
        OnboardingStatus status,
        @Schema(description = "Customer emails")
        List<EmailResponse> emails,
        @Schema(description = "Customer phones")
        List<PhoneResponse> phones,
        @Schema(description = "Customer addresses")
        List<AddressResponse> addresses,
        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {
}

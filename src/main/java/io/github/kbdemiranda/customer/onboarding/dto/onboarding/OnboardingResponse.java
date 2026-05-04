package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OnboardingResponse(
        UUID externalId,
        String fullName,
        String cpf,
        OnboardingStatus status,
        List<EmailResponse> emails,
        List<PhoneResponse> phones,
        List<AddressResponse> addresses,
        LocalDateTime createdAt
) {
}

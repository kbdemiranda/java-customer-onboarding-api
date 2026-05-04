package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import java.util.UUID;

public record PhoneResponse(
        UUID externalId,
        String phoneNumber,
        Boolean primaryPhone
) {
}

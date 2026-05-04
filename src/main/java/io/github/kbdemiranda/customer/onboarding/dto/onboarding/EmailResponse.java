package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import java.util.UUID;

public record EmailResponse(
        UUID externalId,
        String email,
        Boolean primaryEmail
) {
}

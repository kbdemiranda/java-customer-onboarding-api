package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import java.util.UUID;

public record AddressResponse(
        UUID externalId,
        String zipCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        Boolean primaryAddress
) {
}

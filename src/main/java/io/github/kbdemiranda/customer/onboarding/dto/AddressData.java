package io.github.kbdemiranda.customer.onboarding.dto;

public record AddressData(
        String zipCode,
        String street,
        String neighborhood,
        String city,
        String state
) {
}

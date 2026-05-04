package io.github.kbdemiranda.customer.onboarding.client;

import io.github.kbdemiranda.customer.onboarding.dto.AddressData;

import java.util.Optional;

public interface ZipCodeClient {
    Optional<AddressData> findByZipCode(String zipCode);
}

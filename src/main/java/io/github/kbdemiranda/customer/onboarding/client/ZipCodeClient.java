package io.github.kbdemiranda.customer.onboarding.client;

import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeResponse;
import java.util.Optional;

public interface ZipCodeClient {
    Optional<ZipCodeResponse> findByZipCode(String zipCode);
}

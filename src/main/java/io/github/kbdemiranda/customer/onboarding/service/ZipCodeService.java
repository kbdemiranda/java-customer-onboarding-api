package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeResponse;

public interface ZipCodeService {

    ZipCodeResponse searchZipCode(String zipCode);
}

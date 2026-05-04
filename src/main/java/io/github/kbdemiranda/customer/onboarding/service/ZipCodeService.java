package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.client.ViaCepZipCodeClient;
import io.github.kbdemiranda.customer.onboarding.client.WireMockZipCodeClient;
import io.github.kbdemiranda.customer.onboarding.dto.AddressData;
import io.github.kbdemiranda.customer.onboarding.exception.ZipCodeNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ZipCodeService {

    private final WireMockZipCodeClient wireMockZipCodeClient;
    private final ViaCepZipCodeClient viaCepZipCodeClient;

    public ZipCodeService(WireMockZipCodeClient wireMockZipCodeClient,
                          ViaCepZipCodeClient viaCepZipCodeClient) {
        this.wireMockZipCodeClient = wireMockZipCodeClient;
        this.viaCepZipCodeClient = viaCepZipCodeClient;
    }

    public AddressData getAddressOrThrow(String zipCode) {
        return wireMockZipCodeClient.findByZipCode(zipCode)
                .or(() -> viaCepZipCodeClient.findByZipCode(zipCode))
                .orElseThrow(ZipCodeNotFoundException::new);
    }
}

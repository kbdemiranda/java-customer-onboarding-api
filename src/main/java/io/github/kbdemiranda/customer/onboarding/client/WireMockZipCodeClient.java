package io.github.kbdemiranda.customer.onboarding.client;

import io.github.kbdemiranda.customer.onboarding.dto.AddressData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class WireMockZipCodeClient implements ZipCodeClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public WireMockZipCodeClient(RestTemplate restTemplate,
                                 @Value("${zip-code.wiremock.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<AddressData> findByZipCode(String zipCode) {
        String url = "%s/zip-codes/%s".formatted(baseUrl, zipCode);

        try {
            ResponseEntity<WireMockAddressResponse> response =
                    restTemplate.getForEntity(url, WireMockAddressResponse.class);

            WireMockAddressResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }

            return Optional.of(new AddressData(
                    body.zipCode(),
                    body.street(),
                    body.neighborhood(),
                    body.city(),
                    body.state()
            ));
        } catch (HttpClientErrorException.NotFound | ResourceAccessException e) {
            return Optional.empty();
        }
    }

    private record WireMockAddressResponse(
            String zipCode,
            String street,
            String neighborhood,
            String city,
            String state
    ) {
    }
}

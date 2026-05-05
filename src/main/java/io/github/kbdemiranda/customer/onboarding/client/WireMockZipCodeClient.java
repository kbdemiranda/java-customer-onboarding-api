package io.github.kbdemiranda.customer.onboarding.client;

import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeResponse;
import io.github.kbdemiranda.customer.onboarding.exception.ExternalProviderException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
    public Optional<ZipCodeResponse> findByZipCode(String zipCode) {
        String url = "%s/zip-codes/%s".formatted(baseUrl, zipCode);

        try {
            ResponseEntity<ZipCodeResponse> response = restTemplate.getForEntity(url, ZipCodeResponse.class);
            ZipCodeResponse body = response.getBody();
            if (body == null || isBlank(body.zipCode()) || isBlank(body.city()) || isBlank(body.state())) {
                return Optional.empty();
            }
            return Optional.of(body);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ExternalProviderException("WireMock provider failed", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

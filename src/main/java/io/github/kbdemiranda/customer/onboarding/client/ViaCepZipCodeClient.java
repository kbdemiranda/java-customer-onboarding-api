package io.github.kbdemiranda.customer.onboarding.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class ViaCepZipCodeClient implements ZipCodeClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ViaCepZipCodeClient(RestTemplate restTemplate,
                               @Value("${zip-code.viacep.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<ZipCodeResponse> findByZipCode(String zipCode) {
        String url = "%s/%s/json".formatted(baseUrl, zipCode);

        try {
            ResponseEntity<ViaCepAddressResponse> response = restTemplate.getForEntity(url, ViaCepAddressResponse.class);
            ViaCepAddressResponse body = response.getBody();
            if (body == null || Boolean.TRUE.equals(body.erro())) {
                return Optional.empty();
            }

            String resolvedZipCode = firstNotBlank(body.cep(), body.zipCode());
            String resolvedStreet = firstNotBlank(body.logradouro(), body.street());
            String resolvedNeighborhood = firstNotBlank(body.bairro(), body.neighborhood());
            String resolvedCity = firstNotBlank(body.localidade(), body.city());
            String resolvedState = firstNotBlank(body.uf(), body.state());

            if (isBlank(resolvedZipCode) && isBlank(resolvedStreet)
                    && isBlank(resolvedNeighborhood) && isBlank(resolvedCity) && isBlank(resolvedState)) {
                return Optional.empty();
            }

            return Optional.of(new ZipCodeResponse(
                    isBlank(resolvedZipCode) ? zipCode : resolvedZipCode.replaceAll("\\D", ""),
                    resolvedStreet,
                    resolvedNeighborhood,
                    resolvedCity,
                    resolvedState
            ));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException e) {
            throw new ExternalProviderException("ViaCEP provider failed", e);
        }
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ViaCepAddressResponse(
            @JsonAlias("zipCode") String cep,
            String zipCode,
            @JsonAlias("street") String logradouro,
            String street,
            @JsonAlias("neighborhood") String bairro,
            String neighborhood,
            @JsonAlias("city") String localidade,
            String city,
            @JsonAlias("state") String uf,
            String state,
            @JsonProperty("erro") Boolean erro
    ) {
    }
}

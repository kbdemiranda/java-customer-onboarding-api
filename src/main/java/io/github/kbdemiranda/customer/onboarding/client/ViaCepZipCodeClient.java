package io.github.kbdemiranda.customer.onboarding.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kbdemiranda.customer.onboarding.dto.AddressData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

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
    public Optional<AddressData> findByZipCode(String zipCode) {
        String url = "%s/%s/json".formatted(baseUrl, zipCode);

        try {
            ResponseEntity<ViaCepAddressResponse> response =
                    restTemplate.getForEntity(url, ViaCepAddressResponse.class);

            ViaCepAddressResponse body = response.getBody();
            if (body == null || Boolean.TRUE.equals(body.erro()) || isInvalid(body)) {
                return Optional.empty();
            }

            return Optional.of(new AddressData(
                    body.cep(),
                    body.logradouro(),
                    body.bairro(),
                    body.localidade(),
                    body.uf()
            ));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    private boolean isInvalid(ViaCepAddressResponse response) {
        return isBlank(response.cep())
                || isBlank(response.localidade())
                || isBlank(response.uf());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record ViaCepAddressResponse(
            String cep,
            String logradouro,
            String bairro,
            String localidade,
            String uf,
            @JsonProperty("erro") Boolean erro
    ) {
    }
}

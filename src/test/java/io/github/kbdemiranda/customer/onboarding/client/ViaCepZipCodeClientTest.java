package io.github.kbdemiranda.customer.onboarding.client;

import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeResponse;
import io.github.kbdemiranda.customer.onboarding.exception.ExternalProviderException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ViaCepZipCodeClientTest {

    private MockRestServiceServer server;
    private ViaCepZipCodeClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new ViaCepZipCodeClient(restTemplate, "https://viacep.com.br/ws");
    }

    @Test
    void shouldReturnEmptyWhenViaCepReturnsErroTrue() {
        server.expect(once(), requestTo("https://viacep.com.br/ws/99999999/json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "erro": true
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<ZipCodeResponse> result = client.findByZipCode("99999999");

        assertTrue(result.isEmpty());
        server.verify();
    }

    @Test
    void shouldReturnAddressWhenViaCepResponseContainsAliasFields() {
        server.expect(once(), requestTo("https://viacep.com.br/ws/01001000/json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "zipCode": "01001-000",
                          "street": "Praca da Se",
                          "neighborhood": "Se",
                          "city": "Sao Paulo",
                          "state": "SP"
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<ZipCodeResponse> result = client.findByZipCode("01001000");

        assertTrue(result.isPresent());
        assertEquals("01001000", result.get().zipCode());
        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenEssentialFieldsAreMissing() {
        server.expect(once(), requestTo("https://viacep.com.br/ws/01001000/json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "cep": "01001000",
                          "localidade": "Sao Paulo",
                          "uf": "SP"
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<ZipCodeResponse> result = client.findByZipCode("01001000");

        assertTrue(result.isEmpty());
        server.verify();
    }

    @Test
    void shouldThrowExternalProviderExceptionWhenViaCepFails() {
        server.expect(once(), requestTo("https://viacep.com.br/ws/01001000/json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(ExternalProviderException.class, () -> client.findByZipCode("01001000"));
        server.verify();
    }
}

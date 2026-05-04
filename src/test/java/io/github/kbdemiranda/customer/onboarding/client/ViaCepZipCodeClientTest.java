package io.github.kbdemiranda.customer.onboarding.client;

import io.github.kbdemiranda.customer.onboarding.dto.AddressData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ViaCepZipCodeClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private ViaCepZipCodeClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new ViaCepZipCodeClient(restTemplate, "https://viacep.com.br/ws");
    }

    @Test
    void shouldReturnEmptyWhenViaCepReturnsErroTrue() {
        String zipCode = "99999999";
        server.expect(once(), requestTo("https://viacep.com.br/ws/99999999/json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "erro": true
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<AddressData> result = client.findByZipCode(zipCode);

        assertTrue(result.isEmpty());
        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenViaCepResponseIsInvalid() {
        String zipCode = "01001000";
        server.expect(once(), requestTo("https://viacep.com.br/ws/01001000/json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "cep": "",
                          "localidade": "Sao Paulo",
                          "uf": "SP"
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<AddressData> result = client.findByZipCode(zipCode);

        assertTrue(result.isEmpty());
        server.verify();
    }

    @Test
    void shouldReturnAddressWhenViaCepResponseIsValid() {
        String zipCode = "01001000";
        server.expect(once(), requestTo("https://viacep.com.br/ws/01001000/json"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "cep": "01001000",
                          "logradouro": "Praca da Se",
                          "bairro": "Se",
                          "localidade": "Sao Paulo",
                          "uf": "SP"
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<AddressData> result = client.findByZipCode(zipCode);

        assertTrue(result.isPresent());
        assertEquals("01001000", result.get().zipCode());
        server.verify();
    }
}

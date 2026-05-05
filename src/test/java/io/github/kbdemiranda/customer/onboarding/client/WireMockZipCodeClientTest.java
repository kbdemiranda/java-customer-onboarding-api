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

class WireMockZipCodeClientTest {

    private MockRestServiceServer server;
    private WireMockZipCodeClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new WireMockZipCodeClient(restTemplate, "http://localhost:8081");
    }

    @Test
    void shouldReturnAddressWhenWireMockReturns200() {
        String zipCode = "01001000";
        server.expect(once(), requestTo("http://localhost:8081/zip-codes/01001000"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "zipCode": "01001000",
                          "street": "Praca da Se",
                          "neighborhood": "Se",
                          "city": "Sao Paulo",
                          "state": "SP"
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<ZipCodeResponse> result = client.findByZipCode(zipCode);

        assertTrue(result.isPresent());
        assertEquals("01001000", result.get().zipCode());
        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenWireMockReturns404() {
        String zipCode = "99999999";
        server.expect(once(), requestTo("http://localhost:8081/zip-codes/99999999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<ZipCodeResponse> result = client.findByZipCode(zipCode);

        assertTrue(result.isEmpty());
        server.verify();
    }

    @Test
    void shouldThrowExternalProviderExceptionWhenWireMockFails() {
        String zipCode = "01001000";
        server.expect(once(), requestTo("http://localhost:8081/zip-codes/01001000"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(ExternalProviderException.class, () -> client.findByZipCode(zipCode));
        server.verify();
    }
}

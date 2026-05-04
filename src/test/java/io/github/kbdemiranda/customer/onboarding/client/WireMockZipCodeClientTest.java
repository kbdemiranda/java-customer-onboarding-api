package io.github.kbdemiranda.customer.onboarding.client;

import io.github.kbdemiranda.customer.onboarding.dto.AddressData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WireMockZipCodeClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private WireMockZipCodeClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
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

        Optional<AddressData> result = client.findByZipCode(zipCode);

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

        Optional<AddressData> result = client.findByZipCode(zipCode);

        assertTrue(result.isEmpty());
        server.verify();
    }
}

package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.client.ViaCepZipCodeClient;
import io.github.kbdemiranda.customer.onboarding.client.WireMockZipCodeClient;
import io.github.kbdemiranda.customer.onboarding.dto.AddressData;
import io.github.kbdemiranda.customer.onboarding.exception.ZipCodeNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZipCodeServiceTest {

    private final WireMockZipCodeClient wireMockZipCodeClient = mock(WireMockZipCodeClient.class);
    private final ViaCepZipCodeClient viaCepZipCodeClient = mock(ViaCepZipCodeClient.class);
    private final ZipCodeService zipCodeService = new ZipCodeService(wireMockZipCodeClient, viaCepZipCodeClient);

    @Test
    void shouldReturnAddressFromWireMockWithoutFallback() {
        String zipCode = "01001000";
        AddressData wireMockAddress = new AddressData(zipCode, "Street A", "Neighborhood A", "Sao Paulo", "SP");

        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.of(wireMockAddress));

        AddressData result = zipCodeService.getAddressOrThrow(zipCode);

        assertEquals(wireMockAddress, result);
        verify(viaCepZipCodeClient, never()).findByZipCode(zipCode);
    }

    @Test
    void shouldFallbackToViaCepWhenWireMockNotFound() {
        String zipCode = "01001000";
        AddressData viaCepAddress = new AddressData(zipCode, "Street B", "Neighborhood B", "Sao Paulo", "SP");

        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());
        when(viaCepZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.of(viaCepAddress));

        AddressData result = zipCodeService.getAddressOrThrow(zipCode);

        assertEquals(viaCepAddress, result);
        verify(viaCepZipCodeClient).findByZipCode(zipCode);
    }

    @Test
    void shouldThrowWhenZipCodeNotFoundInBothProviders() {
        String zipCode = "99999999";

        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());
        when(viaCepZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());

        ZipCodeNotFoundException exception =
                assertThrows(ZipCodeNotFoundException.class, () -> zipCodeService.getAddressOrThrow(zipCode));

        assertEquals("Zip code not found", exception.getMessage());
    }
}

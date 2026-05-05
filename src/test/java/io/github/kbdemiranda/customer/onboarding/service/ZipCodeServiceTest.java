package io.github.kbdemiranda.customer.onboarding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kbdemiranda.customer.onboarding.client.ViaCepZipCodeClient;
import io.github.kbdemiranda.customer.onboarding.client.WireMockZipCodeClient;
import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeResponse;
import io.github.kbdemiranda.customer.onboarding.entity.ZipCodeQueryLog;
import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeProvider;
import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeQueryStatus;
import io.github.kbdemiranda.customer.onboarding.exception.BusinessValidationException;
import io.github.kbdemiranda.customer.onboarding.exception.ExternalProviderException;
import io.github.kbdemiranda.customer.onboarding.exception.ZipCodeNotFoundException;
import io.github.kbdemiranda.customer.onboarding.repository.ZipCodeQueryLogRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZipCodeServiceTest {

    private final WireMockZipCodeClient wireMockZipCodeClient = mock(WireMockZipCodeClient.class);
    private final ViaCepZipCodeClient viaCepZipCodeClient = mock(ViaCepZipCodeClient.class);
    private final ZipCodeQueryLogRepository zipCodeQueryLogRepository = mock(ZipCodeQueryLogRepository.class);
    private final ZipCodeService zipCodeService = new ZipCodeServiceImpl(
            wireMockZipCodeClient,
            viaCepZipCodeClient,
            zipCodeQueryLogRepository,
            new ObjectMapper()
    );

    @Test
    void shouldReturnAddressWhenFoundInWireMock() {
        String zipCode = "01001000";
        ZipCodeResponse wireMockAddress = new ZipCodeResponse(zipCode, "Street A", "Neighborhood A", "Sao Paulo", "SP");
        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.of(wireMockAddress));

        ZipCodeResponse result = zipCodeService.searchZipCode(zipCode);

        assertEquals(wireMockAddress, result);
        verify(viaCepZipCodeClient, never()).findByZipCode(zipCode);
    }

    @Test
    void shouldFallbackToViaCepWhenWireMockDoesNotFindZipCode() {
        String zipCode = "01001000";
        ZipCodeResponse viaCepAddress = new ZipCodeResponse(zipCode, "Street B", "Neighborhood B", "Sao Paulo", "SP");

        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());
        when(viaCepZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.of(viaCepAddress));

        ZipCodeResponse result = zipCodeService.searchZipCode(zipCode);

        assertEquals(viaCepAddress, result);
        verify(viaCepZipCodeClient).findByZipCode(zipCode);
    }

    @Test
    void shouldThrowNotFoundWhenZipCodeDoesNotExist() {
        String zipCode = "99999999";
        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());
        when(viaCepZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());

        ZipCodeNotFoundException exception =
                assertThrows(ZipCodeNotFoundException.class, () -> zipCodeService.searchZipCode(zipCode));

        assertEquals("Zip code not found", exception.getMessage());
    }

    @Test
    void shouldSaveSuccessLogForWireMock() {
        String zipCode = "01001000";
        ZipCodeResponse wireMockAddress = new ZipCodeResponse(zipCode, "Street A", "Neighborhood A", "Sao Paulo", "SP");
        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.of(wireMockAddress));

        zipCodeService.searchZipCode(zipCode);

        verify(zipCodeQueryLogRepository).save(any(ZipCodeQueryLog.class));
    }

    @Test
    void shouldSaveSuccessLogForViaCep() {
        String zipCode = "01001000";
        ZipCodeResponse viaCepAddress = new ZipCodeResponse(zipCode, "Street B", "Neighborhood B", "Sao Paulo", "SP");
        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());
        when(viaCepZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.of(viaCepAddress));

        zipCodeService.searchZipCode(zipCode);

        verify(zipCodeQueryLogRepository).save(any(ZipCodeQueryLog.class));
    }

    @Test
    void shouldSaveNotFoundLog() {
        String zipCode = "99999999";
        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());
        when(viaCepZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());

        assertThrows(ZipCodeNotFoundException.class, () -> zipCodeService.searchZipCode(zipCode));

        verify(zipCodeQueryLogRepository).save(any(ZipCodeQueryLog.class));
    }

    @Test
    void shouldFallbackToViaCepWhenWireMockFails() {
        String zipCode = "01001000";
        ZipCodeResponse viaCepAddress = new ZipCodeResponse(zipCode, "Street B", "Neighborhood B", "Sao Paulo", "SP");
        when(wireMockZipCodeClient.findByZipCode(zipCode))
                .thenThrow(new ExternalProviderException("WireMock provider failed"));
        when(viaCepZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.of(viaCepAddress));

        ZipCodeResponse result = zipCodeService.searchZipCode(zipCode);

        assertEquals(viaCepAddress, result);
    }

    
    void shouldSaveErrorLogWhenBothProvidersFail() {
        String zipCode = "01001000";
        when(wireMockZipCodeClient.findByZipCode(zipCode))
                .thenThrow(new ExternalProviderException("WireMock provider failed"));
        when(viaCepZipCodeClient.findByZipCode(zipCode))
                .thenThrow(new ExternalProviderException("ViaCEP provider failed"));

        assertThrows(ExternalProviderException.class, () -> zipCodeService.searchZipCode(zipCode));

        verify(zipCodeQueryLogRepository).save(any(ZipCodeQueryLog.class));
    }

    @Test
    void shouldRejectInvalidZipCode() {
        assertThrows(BusinessValidationException.class, () -> zipCodeService.searchZipCode("123"));
        verify(zipCodeQueryLogRepository, never()).save(any());
    }

    @Test
    void shouldMarkNotFoundLogWithViaCepProvider() {
        String zipCode = "99999999";
        when(wireMockZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());
        when(viaCepZipCodeClient.findByZipCode(zipCode)).thenReturn(Optional.empty());

        assertThrows(ZipCodeNotFoundException.class, () -> zipCodeService.searchZipCode(zipCode));

        verify(zipCodeQueryLogRepository).save(any(ZipCodeQueryLog.class));
    }
}

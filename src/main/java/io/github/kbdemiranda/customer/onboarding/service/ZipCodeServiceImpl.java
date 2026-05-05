package io.github.kbdemiranda.customer.onboarding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class ZipCodeServiceImpl implements ZipCodeService {

    private final WireMockZipCodeClient wireMockZipCodeClient;
    private final ViaCepZipCodeClient viaCepZipCodeClient;
    private final ZipCodeQueryLogRepository zipCodeQueryLogRepository;
    private final ObjectMapper objectMapper;

    public ZipCodeServiceImpl(WireMockZipCodeClient wireMockZipCodeClient,
                              ViaCepZipCodeClient viaCepZipCodeClient,
                              ZipCodeQueryLogRepository zipCodeQueryLogRepository,
                              ObjectMapper objectMapper) {
        this.wireMockZipCodeClient = wireMockZipCodeClient;
        this.viaCepZipCodeClient = viaCepZipCodeClient;
        this.zipCodeQueryLogRepository = zipCodeQueryLogRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ZipCodeResponse searchZipCode(String zipCode) {
        String normalizedZipCode = normalizeAndValidate(zipCode);
        LocalDateTime requestTimestamp = LocalDateTime.now();
        ExternalProviderException wireMockFailure = null;

        try {
            var wireMockResult = wireMockZipCodeClient.findByZipCode(normalizedZipCode);
            if (wireMockResult.isPresent()) {
                ZipCodeResponse response = normalizeResponseZipCode(wireMockResult.get(), normalizedZipCode);
                saveLog(normalizedZipCode, ZipCodeProvider.WIREMOCK, ZipCodeQueryStatus.SUCCESS,
                        requestTimestamp, safeSerialize(response), null);
                return response;
            }
        } catch (ExternalProviderException ex) {
            wireMockFailure = ex;
        }

        try {
            var viaCepResult = viaCepZipCodeClient.findByZipCode(normalizedZipCode);
            if (viaCepResult.isPresent()) {
                ZipCodeResponse response = normalizeResponseZipCode(viaCepResult.get(), normalizedZipCode);
                saveLog(normalizedZipCode, ZipCodeProvider.VIACEP, ZipCodeQueryStatus.SUCCESS,
                        requestTimestamp, safeSerialize(response), null);
                return response;
            }
        } catch (ExternalProviderException ex) {
            String errorMessage = wireMockFailure == null
                    ? ex.getMessage()
                    : "WireMock failed: %s | ViaCEP failed: %s".formatted(wireMockFailure.getMessage(), ex.getMessage());
            saveLog(normalizedZipCode, ZipCodeProvider.VIACEP, ZipCodeQueryStatus.ERROR,
                    requestTimestamp, null, errorMessage);
            throw ex;
        }

        saveLog(normalizedZipCode, ZipCodeProvider.VIACEP, ZipCodeQueryStatus.NOT_FOUND,
                requestTimestamp, null, "Zip code not found");
        throw new ZipCodeNotFoundException();
    }

    private void saveLog(String zipCode,
                         ZipCodeProvider provider,
                         ZipCodeQueryStatus status,
                         LocalDateTime requestTimestamp,
                         String responseBody,
                         String errorMessage) {
        ZipCodeQueryLog log = new ZipCodeQueryLog();
        log.setZipCode(zipCode);
        log.setProvider(provider);
        log.setStatus(status);
        log.setRequestTimestamp(requestTimestamp);
        log.setResponseBody(responseBody);
        log.setErrorMessage(errorMessage);
        zipCodeQueryLogRepository.save(log);
    }

    private String safeSerialize(ZipCodeResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new ExternalProviderException("Failed to serialize zip code provider response", e);
        }
    }

    private String normalizeAndValidate(String zipCode) {
        String normalized = zipCode == null ? "" : zipCode.replaceAll("\\D", "");
        if (normalized.length() != 8) {
            throw new BusinessValidationException("zipCode must contain exactly 8 digits");
        }
        return normalized;
    }

    private ZipCodeResponse normalizeResponseZipCode(ZipCodeResponse response, String normalizedZipCode) {
        return new ZipCodeResponse(
                normalizedZipCode,
                response.street(),
                response.neighborhood(),
                response.city(),
                response.state()
        );
    }
}

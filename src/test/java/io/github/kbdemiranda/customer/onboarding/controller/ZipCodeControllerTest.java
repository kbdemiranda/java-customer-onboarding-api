package io.github.kbdemiranda.customer.onboarding.controller;

import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeResponse;
import io.github.kbdemiranda.customer.onboarding.exception.BusinessValidationException;
import io.github.kbdemiranda.customer.onboarding.exception.ExternalProviderException;
import io.github.kbdemiranda.customer.onboarding.exception.GlobalExceptionHandler;
import io.github.kbdemiranda.customer.onboarding.exception.ZipCodeNotFoundException;
import io.github.kbdemiranda.customer.onboarding.service.ZipCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ZipCodeController.class)
@Import(GlobalExceptionHandler.class)
class ZipCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ZipCodeService zipCodeService;

    @Test
    void shouldReturn200WhenZipCodeExists() throws Exception {
        when(zipCodeService.searchZipCode("01001-000"))
                .thenReturn(new ZipCodeResponse("01001000", "Praca da Se", "Se", "Sao Paulo", "SP"));

        mockMvc.perform(get("/api/v1/zip-codes/01001-000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zipCode").value("01001000"));
    }

    @Test
    void shouldReturn400WhenZipCodeIsInvalid() throws Exception {
        when(zipCodeService.searchZipCode("123"))
                .thenThrow(new BusinessValidationException("zipCode must contain exactly 8 digits"));

        mockMvc.perform(get("/api/v1/zip-codes/123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn422WhenZipCodeNotFound() throws Exception {
        when(zipCodeService.searchZipCode("00000000"))
                .thenThrow(new ZipCodeNotFoundException());

        mockMvc.perform(get("/api/v1/zip-codes/00000000"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturn502WhenProviderFails() throws Exception {
        when(zipCodeService.searchZipCode("01001000"))
                .thenThrow(new ExternalProviderException("WireMock provider failed"));

        mockMvc.perform(get("/api/v1/zip-codes/01001000"))
                .andExpect(status().isBadGateway());
    }
}

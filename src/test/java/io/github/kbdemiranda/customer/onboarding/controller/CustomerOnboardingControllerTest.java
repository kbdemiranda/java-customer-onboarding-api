package io.github.kbdemiranda.customer.onboarding.controller;

import io.github.kbdemiranda.customer.onboarding.dto.onboarding.AddressResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.EmailResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.PhoneResponse;
import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import io.github.kbdemiranda.customer.onboarding.exception.BusinessValidationException;
import io.github.kbdemiranda.customer.onboarding.exception.CpfAlreadyExistsException;
import io.github.kbdemiranda.customer.onboarding.exception.GlobalExceptionHandler;
import io.github.kbdemiranda.customer.onboarding.exception.ZipCodeNotFoundException;
import io.github.kbdemiranda.customer.onboarding.service.CustomerOnboardingService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomerOnboardingController.class)
@Import(GlobalExceptionHandler.class)
class CustomerOnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerOnboardingService customerOnboardingService;

    @Test
    void shouldReturnCreatedWhenOnboardingIsCreated() throws Exception {
        OnboardingResponse response = new OnboardingResponse(
                UUID.randomUUID(),
                "John Doe",
                "12345678909",
                OnboardingStatus.DOCUMENTS_PENDING,
                List.of(new EmailResponse(UUID.randomUUID(), "john@example.com", true)),
                List.of(new PhoneResponse(UUID.randomUUID(), "11999999999", false)),
                List.of(new AddressResponse(UUID.randomUUID(), "01001000", "Praca da Se", "100", "Apt 10", "Se", "Sao Paulo", "SP", true)),
                LocalDateTime.now()
        );
        when(customerOnboardingService.createOnboarding(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/onboardings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DOCUMENTS_PENDING"))
                .andExpect(jsonPath("$.cpf").value("12345678909"));
    }

    @Test
    void shouldReturnBadRequestWhenBeanValidationFails() throws Exception {
        String invalidPayload = """
                {
                  "fullName": "",
                  "cpf": "123",
                  "emails": [],
                  "phones": [],
                  "addresses": []
                }
                """;

        mockMvc.perform(post("/api/v1/onboardings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnConflictWhenCpfAlreadyExists() throws Exception {
        when(customerOnboardingService.createOnboarding(any()))
                .thenThrow(new CpfAlreadyExistsException("12345678909"));

        mockMvc.perform(post("/api/v1/onboardings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenZipCodeIsNotFound() throws Exception {
        when(customerOnboardingService.createOnboarding(any()))
                .thenThrow(new ZipCodeNotFoundException());

        mockMvc.perform(post("/api/v1/onboardings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void shouldReturnBadRequestWhenBusinessValidationFails() throws Exception {
        when(customerOnboardingService.createOnboarding(any()))
                .thenThrow(new BusinessValidationException("At most one primary email is allowed"));

        mockMvc.perform(post("/api/v1/onboardings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private String validPayload() {
        return """
                {
                  "fullName": "John Doe",
                  "cpf": "123.456.789-09",
                  "emails": [
                    {
                      "email": "john@example.com",
                      "primaryEmail": true
                    }
                  ],
                  "phones": [
                    {
                      "phoneNumber": "11999999999",
                      "primaryPhone": false
                    }
                  ],
                  "addresses": [
                    {
                      "zipCode": "01001-000",
                      "number": "100",
                      "complement": "Apt 10",
                      "primaryAddress": true
                    }
                  ]
                }
                """;
    }
}

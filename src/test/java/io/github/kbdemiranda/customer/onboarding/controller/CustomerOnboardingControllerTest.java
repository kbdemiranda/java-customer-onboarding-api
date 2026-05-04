package io.github.kbdemiranda.customer.onboarding.controller;

import io.github.kbdemiranda.customer.onboarding.dto.audit.AuditLogResponse;
import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.document.DocumentResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.AddressResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.EmailResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingFilter;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.PhoneResponse;
import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import io.github.kbdemiranda.customer.onboarding.enums.AuditAction;
import io.github.kbdemiranda.customer.onboarding.exception.BusinessValidationException;
import io.github.kbdemiranda.customer.onboarding.exception.CpfAlreadyExistsException;
import io.github.kbdemiranda.customer.onboarding.exception.GlobalExceptionHandler;
import io.github.kbdemiranda.customer.onboarding.exception.InvalidDocumentException;
import io.github.kbdemiranda.customer.onboarding.exception.ResourceNotFoundException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    void shouldReturnCreatedWhenDocumentIsUploaded() throws Exception {
        UUID onboardingExternalId = UUID.randomUUID();
        DocumentResponse response = new DocumentResponse(
                UUID.randomUUID(),
                DocumentType.CPF,
                "document.pdf",
                "application/pdf",
                7L,
                LocalDateTime.now()
        );
        when(customerOnboardingService.uploadDocument(any(), any(), any())).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", "content".getBytes());
        mockMvc.perform(multipart("/api/v1/onboardings/{externalId}/documents", onboardingExternalId)
                        .file(file)
                        .param("documentType", "CPF"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentType").value("CPF"))
                .andExpect(jsonPath("$.originalFileName").value("document.pdf"));
    }

    @Test
    void shouldReturnBadRequestWhenDocumentTypeIsInvalidOnUpload() throws Exception {
        UUID onboardingExternalId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/onboardings/{externalId}/documents", onboardingExternalId)
                        .file(file)
                        .param("documentType", "INVALID_TYPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidDocumentExceptionIsThrownOnUpload() throws Exception {
        UUID onboardingExternalId = UUID.randomUUID();
        when(customerOnboardingService.uploadDocument(any(), any(), any()))
                .thenThrow(new InvalidDocumentException("Unsupported file type"));

        MockMultipartFile file = new MockMultipartFile("file", "document.txt", "text/plain", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/onboardings/{externalId}/documents", onboardingExternalId)
                        .file(file)
                        .param("documentType", "CPF"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Unsupported file type"));
    }

    @Test
    void shouldReturnNotFoundWhenOnboardingDoesNotExistOnUpload() throws Exception {
        UUID onboardingExternalId = UUID.randomUUID();
        when(customerOnboardingService.uploadDocument(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Onboarding not found"));

        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/onboardings/{externalId}/documents", onboardingExternalId)
                        .file(file)
                        .param("documentType", "CPF"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Onboarding not found"));
    }

    @Test
    void shouldReturnOkWhenGettingDocumentsByOnboardingExternalId() throws Exception {
        UUID onboardingExternalId = UUID.randomUUID();
        DocumentResponse firstDocument = new DocumentResponse(
                UUID.randomUUID(),
                DocumentType.CPF,
                "cpf.pdf",
                "application/pdf",
                1200L,
                LocalDateTime.of(2026, 5, 4, 10, 0)
        );
        DocumentResponse secondDocument = new DocumentResponse(
                UUID.randomUUID(),
                DocumentType.RG,
                "rg.png",
                "image/png",
                2400L,
                LocalDateTime.of(2026, 5, 3, 10, 0)
        );
        when(customerOnboardingService.getDocuments(onboardingExternalId)).thenReturn(List.of(firstDocument, secondDocument));

        mockMvc.perform(get("/api/v1/onboardings/{externalId}/documents", onboardingExternalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalId").value(firstDocument.externalId().toString()))
                .andExpect(jsonPath("$[0].documentType").value("CPF"))
                .andExpect(jsonPath("$[0].originalFileName").value("cpf.pdf"))
                .andExpect(jsonPath("$[0].contentType").value("application/pdf"))
                .andExpect(jsonPath("$[0].fileSize").value(1200))
                .andExpect(jsonPath("$[0].storagePath").doesNotExist())
                .andExpect(jsonPath("$[1].externalId").value(secondDocument.externalId().toString()))
                .andExpect(jsonPath("$[1].documentType").value("RG"));
    }

    @Test
    void shouldReturnNotFoundWhenGettingDocumentsForNonExistingOnboarding() throws Exception {
        UUID onboardingExternalId = UUID.randomUUID();
        when(customerOnboardingService.getDocuments(onboardingExternalId))
                .thenThrow(new ResourceNotFoundException("Onboarding not found"));

        mockMvc.perform(get("/api/v1/onboardings/{externalId}/documents", onboardingExternalId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Onboarding not found"));
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

    @Test
    void shouldReturnOkWhenListingOnboardingsWithPaginationAndStatusFilter() throws Exception {
        OnboardingResponse onboarding = new OnboardingResponse(
                UUID.randomUUID(),
                "John Doe",
                "12345678909",
                OnboardingStatus.DOCUMENTS_PENDING,
                List.of(new EmailResponse(UUID.randomUUID(), "john@example.com", true)),
                List.of(new PhoneResponse(UUID.randomUUID(), "11999999999", false)),
                List.of(new AddressResponse(UUID.randomUUID(), "01001000", "Praca da Se", "100", "Apt 10", "Se", "Sao Paulo", "SP", true)),
                LocalDateTime.now()
        );
        PageResponse<OnboardingResponse> response = new PageResponse<>(List.of(onboarding), 0, 10, 1, 1);

        when(customerOnboardingService.listOnboardings(any(OnboardingFilter.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/onboardings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "DOCUMENTS_PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("DOCUMENTS_PENDING"));
    }

    @Test
    void shouldReturnOkWhenGettingOnboardingByExternalId() throws Exception {
        UUID externalId = UUID.randomUUID();
        OnboardingResponse response = new OnboardingResponse(
                externalId,
                "John Doe",
                "12345678909",
                OnboardingStatus.DOCUMENTS_PENDING,
                List.of(new EmailResponse(UUID.randomUUID(), "john@example.com", true)),
                List.of(new PhoneResponse(UUID.randomUUID(), "11999999999", false)),
                List.of(new AddressResponse(UUID.randomUUID(), "01001000", "Praca da Se", "100", "Apt 10", "Se", "Sao Paulo", "SP", true)),
                LocalDateTime.now()
        );
        when(customerOnboardingService.getByExternalId(externalId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/onboardings/{externalId}", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(externalId.toString()))
                .andExpect(jsonPath("$.status").value("DOCUMENTS_PENDING"))
                .andExpect(jsonPath("$.cpf").value("12345678909"));
    }

    @Test
    void shouldReturnNotFoundWhenOnboardingByExternalIdDoesNotExist() throws Exception {
        UUID externalId = UUID.randomUUID();
        when(customerOnboardingService.getByExternalId(externalId))
                .thenThrow(new ResourceNotFoundException("Onboarding not found"));

        mockMvc.perform(get("/api/v1/onboardings/{externalId}", externalId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Onboarding not found"));
    }

    @Test
    void shouldReturnOkWhenGettingAuditLogsByOnboardingExternalId() throws Exception {
        UUID onboardingExternalId = UUID.randomUUID();
        AuditLogResponse firstLog = new AuditLogResponse(
                UUID.randomUUID(),
                AuditAction.DOCUMENT_UPLOADED,
                "SUCCESS",
                "Document uploaded successfully",
                LocalDateTime.of(2026, 5, 4, 12, 30)
        );
        AuditLogResponse secondLog = new AuditLogResponse(
                UUID.randomUUID(),
                AuditAction.ONBOARDING_CREATED,
                "SUCCESS",
                "Onboarding created successfully",
                LocalDateTime.of(2026, 5, 3, 9, 0)
        );
        when(customerOnboardingService.getAuditLogs(onboardingExternalId)).thenReturn(List.of(firstLog, secondLog));

        mockMvc.perform(get("/api/v1/onboardings/{externalId}/audit-logs", onboardingExternalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalId").value(firstLog.externalId().toString()))
                .andExpect(jsonPath("$[0].action").value("DOCUMENT_UPLOADED"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[0].message").value("Document uploaded successfully"))
                .andExpect(jsonPath("$[1].externalId").value(secondLog.externalId().toString()))
                .andExpect(jsonPath("$[1].action").value("ONBOARDING_CREATED"));
    }

    @Test
    void shouldReturnNotFoundWhenGettingAuditLogsForNonExistingOnboarding() throws Exception {
        UUID onboardingExternalId = UUID.randomUUID();
        when(customerOnboardingService.getAuditLogs(onboardingExternalId))
                .thenThrow(new ResourceNotFoundException("Onboarding not found"));

        mockMvc.perform(get("/api/v1/onboardings/{externalId}/audit-logs", onboardingExternalId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Onboarding not found"));
    }

    @Test
    void shouldBindFormattedCpfFilterWhenListingOnboardings() throws Exception {
        PageResponse<OnboardingResponse> response = new PageResponse<>(List.of(), 0, 10, 0, 0);
        when(customerOnboardingService.listOnboardings(any(OnboardingFilter.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/onboardings")
                        .param("cpf", "123.456.789-09"))
                .andExpect(status().isOk());

        verify(customerOnboardingService).listOnboardings(argThat(filter ->
                "123.456.789-09".equals(filter.cpf()) && filter.page() == 0 && filter.size() == 10
        ));
    }

    @Test
    void shouldReturnBadRequestWhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/onboardings")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenSizeIsZero() throws Exception {
        mockMvc.perform(get("/api/v1/onboardings")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenSizeExceedsLimit() throws Exception {
        mockMvc.perform(get("/api/v1/onboardings")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadRequestWhenExternalIdIsMalformed() throws Exception {
        mockMvc.perform(get("/api/v1/onboardings/{externalId}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldReturnBadGatewayWhenExternalProviderFails() throws Exception {
        when(customerOnboardingService.createOnboarding(any()))
                .thenThrow(new RestClientException("Provider timeout"));

        mockMvc.perform(post("/api/v1/onboardings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedExceptionOccurs() throws Exception {
        when(customerOnboardingService.createOnboarding(any()))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(post("/api/v1/onboardings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
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

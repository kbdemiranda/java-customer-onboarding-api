package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeResponse;
import io.github.kbdemiranda.customer.onboarding.dto.audit.AuditLogResponse;
import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.document.DocumentResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.AddressRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.CreateOnboardingRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.EmailRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingFilter;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.PhoneRequest;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerDocument;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import io.github.kbdemiranda.customer.onboarding.entity.OnboardingAuditLog;
import io.github.kbdemiranda.customer.onboarding.enums.AuditAction;
import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import io.github.kbdemiranda.customer.onboarding.exception.BusinessValidationException;
import io.github.kbdemiranda.customer.onboarding.exception.CpfAlreadyExistsException;
import io.github.kbdemiranda.customer.onboarding.exception.InvalidDocumentException;
import io.github.kbdemiranda.customer.onboarding.exception.ResourceNotFoundException;
import io.github.kbdemiranda.customer.onboarding.exception.ZipCodeNotFoundException;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerAddressMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerDocumentMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerEmailMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerOnboardingMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerPhoneMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.AuditLogMapper;
import java.time.LocalDateTime;
import io.github.kbdemiranda.customer.onboarding.repository.CustomerDocumentRepository;
import io.github.kbdemiranda.customer.onboarding.repository.CustomerOnboardingRepository;
import io.github.kbdemiranda.customer.onboarding.repository.OnboardingAuditLogRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerOnboardingServiceImplTest {

    private final CustomerOnboardingRepository customerOnboardingRepository = mock(CustomerOnboardingRepository.class);
    private final CustomerDocumentRepository customerDocumentRepository = mock(CustomerDocumentRepository.class);
    private final OnboardingAuditLogRepository onboardingAuditLogRepository = mock(OnboardingAuditLogRepository.class);
    private final ZipCodeService zipCodeService = mock(ZipCodeService.class);

    private CustomerOnboardingServiceImpl service;

    @BeforeEach
    void setUp() {
        CustomerAddressMapper customerAddressMapper = new CustomerAddressMapper();
        CustomerDocumentMapper customerDocumentMapper = new CustomerDocumentMapper();
        CustomerEmailMapper customerEmailMapper = new CustomerEmailMapper();
        CustomerPhoneMapper customerPhoneMapper = new CustomerPhoneMapper();
        AuditLogMapper auditLogMapper = new AuditLogMapper();
        CustomerOnboardingMapper customerOnboardingMapper =
                new CustomerOnboardingMapper(customerAddressMapper, customerEmailMapper, customerPhoneMapper);

        service = new CustomerOnboardingServiceImpl(
                customerOnboardingRepository,
                customerDocumentRepository,
                onboardingAuditLogRepository,
                zipCodeService,
                customerOnboardingMapper,
                customerEmailMapper,
                customerPhoneMapper,
                customerAddressMapper,
                customerDocumentMapper,
                auditLogMapper
        );
    }

    @Test
    void shouldCreateOnboardingWithZipCodeEnrichmentAndAuditLog() {
        CreateOnboardingRequest request = validRequest();
        ZipCodeResponse addressData = new ZipCodeResponse("01001000", "Praca da Se", "Se", "Sao Paulo", "SP");

        when(customerOnboardingRepository.existsByCpf("12345678909")).thenReturn(false);
        when(customerOnboardingRepository.existsByProtocol(any())).thenReturn(false);
        when(zipCodeService.searchZipCode("01001000")).thenReturn(addressData);
        when(customerOnboardingRepository.save(any(CustomerOnboarding.class))).thenAnswer(invocation -> {
            CustomerOnboarding onboarding = invocation.getArgument(0);
            onboarding.setExternalId(UUID.randomUUID());
            return onboarding;
        });

        OnboardingResponse response = service.createOnboarding(request);

        assertEquals("12345678909", response.cpf());
        assertEquals(14, response.protocol().length());
        assertEquals(OnboardingStatus.DOCUMENTS_PENDING, response.status());
        assertEquals(1, response.addresses().size());
        assertEquals("Praca da Se", response.addresses().get(0).street());
        verify(onboardingAuditLogRepository).save(any());
    }

    @Test
    void shouldThrowWhenCpfAlreadyExists() {
        CreateOnboardingRequest request = validRequest();
        when(customerOnboardingRepository.existsByCpf("12345678909")).thenReturn(true);

        assertThrows(CpfAlreadyExistsException.class, () -> service.createOnboarding(request));
    }

    @Test
    void shouldThrowWhenMoreThanOnePrimaryEmail() {
        CreateOnboardingRequest request = new CreateOnboardingRequest(
                "John Doe",
                "123.456.789-09",
                List.of(
                        new EmailRequest("john@example.com", true),
                        new EmailRequest("john.work@example.com", true)
                ),
                List.of(new PhoneRequest("11999999999", false)),
                List.of(new AddressRequest("01001-000", "100", null, true))
        );

        assertThrows(BusinessValidationException.class, () -> service.createOnboarding(request));
    }

    @Test
    void shouldThrowWhenNoPrimaryContactMethodExists() {
        CreateOnboardingRequest request = new CreateOnboardingRequest(
                "John Doe",
                "123.456.789-09",
                List.of(new EmailRequest("john@example.com", false)),
                List.of(new PhoneRequest("11999999999", false)),
                List.of(new AddressRequest("01001-000", "100", null, true))
        );

        assertThrows(BusinessValidationException.class, () -> service.createOnboarding(request));
    }

    @Test
    void shouldPropagateWhenZipCodeNotFound() {
        CreateOnboardingRequest request = validRequest();

        when(customerOnboardingRepository.existsByCpf("12345678909")).thenReturn(false);
        when(zipCodeService.searchZipCode("01001000")).thenThrow(new ZipCodeNotFoundException());

        assertThrows(ZipCodeNotFoundException.class, () -> service.createOnboarding(request));
    }

    @Test
    void shouldListOnboardingsUsingDefaultPagination() {
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.DOCUMENTS_PENDING);
        Page<CustomerOnboarding> onboardingPage = new PageImpl<>(
                List.of(onboarding),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")),
                1
        );

        when(customerOnboardingRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))))
                .thenReturn(onboardingPage);

        PageResponse<OnboardingResponse> response = service.listOnboardings(new OnboardingFilter(null, null, null, null));

        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(1, response.totalElements());
        assertEquals(1, response.content().size());
        assertEquals("12345678909", response.content().get(0).cpf());
    }

    @Test
    void shouldFilterByNormalizedCpfWhenListingOnboardings() {
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.DOCUMENTS_PENDING);
        when(customerOnboardingRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(onboarding), PageRequest.of(0, 10), 1));

        PageResponse<OnboardingResponse> response =
                service.listOnboardings(new OnboardingFilter("123.456.789-09", null, 0, 10));

        assertEquals(1, response.content().size());
        assertEquals("12345678909", response.content().get(0).cpf());
    }

    @Test
    void shouldFilterByStatusWhenListingOnboardings() {
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.APPROVED);
        when(customerOnboardingRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(onboarding), PageRequest.of(0, 10), 1));

        PageResponse<OnboardingResponse> response =
                service.listOnboardings(new OnboardingFilter(null, OnboardingStatus.APPROVED, 0, 10));

        assertEquals(1, response.content().size());
        assertEquals(OnboardingStatus.APPROVED, response.content().get(0).status());
    }

    @Test
    void shouldApplyCpfAndStatusFiltersWhenListingOnboardings() {
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.APPROVED);
        when(customerOnboardingRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(onboarding), PageRequest.of(1, 5), 1));

        PageResponse<OnboardingResponse> response =
                service.listOnboardings(new OnboardingFilter("123.456.789-09", OnboardingStatus.APPROVED, 1, 5));

        assertEquals(1, response.page());
        assertEquals(5, response.size());
        assertEquals(1, response.content().size());
        assertEquals("12345678909", response.content().get(0).cpf());
        assertEquals(OnboardingStatus.APPROVED, response.content().get(0).status());
    }

    @Test
    void shouldRequestDescendingCreatedAtSortWhenListingOnboardings() {
        when(customerOnboardingRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        service.listOnboardings(new OnboardingFilter(null, null, 0, 10));

        verify(customerOnboardingRepository)
                .findAll(any(Specification.class), eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @Test
    void shouldGetOnboardingByProtocol() {
        String protocol = "58392017463051";
        UUID externalId = UUID.randomUUID();
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.DOCUMENTS_PENDING);
        onboarding.setExternalId(externalId);
        onboarding.setProtocol(protocol);

        when(customerOnboardingRepository.findFirstByProtocolOrderByCreatedAtDesc(protocol))
                .thenReturn(java.util.Optional.of(onboarding));

        OnboardingResponse response = service.getByProtocol(protocol);

        assertEquals(externalId, response.externalId());
        assertEquals(protocol, response.protocol());
        assertEquals("12345678909", response.cpf());
        assertEquals(OnboardingStatus.DOCUMENTS_PENDING, response.status());
    }

    @Test
    void shouldThrowWhenOnboardingByProtocolIsNotFound() {
        String protocol = "58392017463051";
        when(customerOnboardingRepository.findFirstByProtocolOrderByCreatedAtDesc(protocol))
                .thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> service.getByProtocol(protocol));

        assertEquals("Onboarding not found", exception.getMessage());
    }

    @Test
    void shouldReturnAuditLogsOrderedByCreatedAtDescWhenOnboardingExists() {
        UUID onboardingExternalId = UUID.randomUUID();
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.DOCUMENTS_RECEIVED);
        onboarding.setExternalId(onboardingExternalId);

        OnboardingAuditLog newestLog = new OnboardingAuditLog();
        newestLog.setExternalId(UUID.randomUUID());
        newestLog.setAction(AuditAction.DOCUMENT_UPLOADED);
        newestLog.setStatus("SUCCESS");
        newestLog.setMessage("Document uploaded successfully");
        newestLog.setCreatedAt(LocalDateTime.of(2026, 5, 4, 12, 30));

        OnboardingAuditLog oldestLog = new OnboardingAuditLog();
        oldestLog.setExternalId(UUID.randomUUID());
        oldestLog.setAction(AuditAction.ONBOARDING_CREATED);
        oldestLog.setStatus("SUCCESS");
        oldestLog.setMessage("Onboarding created successfully");
        oldestLog.setCreatedAt(LocalDateTime.of(2026, 5, 3, 9, 0));

        when(customerOnboardingRepository.findByExternalId(onboardingExternalId)).thenReturn(java.util.Optional.of(onboarding));
        when(onboardingAuditLogRepository.findByOnboardingExternalIdOrderByCreatedAtDesc(onboardingExternalId))
                .thenReturn(List.of(newestLog, oldestLog));

        List<AuditLogResponse> response = service.getAuditLogs(onboardingExternalId);

        assertEquals(2, response.size());
        assertEquals(newestLog.getExternalId(), response.get(0).externalId());
        assertEquals(newestLog.getAction(), response.get(0).action());
        assertEquals(newestLog.getStatus(), response.get(0).status());
        assertEquals(newestLog.getMessage(), response.get(0).message());
        assertEquals(newestLog.getCreatedAt(), response.get(0).createdAt());

        assertEquals(oldestLog.getExternalId(), response.get(1).externalId());
        assertEquals(oldestLog.getCreatedAt(), response.get(1).createdAt());
    }

    @Test
    void shouldThrowWhenGettingAuditLogsForNonExistingOnboarding() {
        UUID onboardingExternalId = UUID.randomUUID();
        when(customerOnboardingRepository.findByExternalId(onboardingExternalId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> service.getAuditLogs(onboardingExternalId));

        assertEquals("Onboarding not found", exception.getMessage());
        verify(onboardingAuditLogRepository, never())
                .findByOnboardingExternalIdOrderByCreatedAtDesc(any(UUID.class));
    }

    @Test
    void shouldReturnDocumentsOrderedByCreatedAtDescWhenOnboardingExists() {
        UUID onboardingExternalId = UUID.randomUUID();
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.DOCUMENTS_RECEIVED);
        onboarding.setExternalId(onboardingExternalId);

        CustomerDocument newestDocument = new CustomerDocument();
        newestDocument.setExternalId(UUID.randomUUID());
        newestDocument.setDocumentType(DocumentType.CPF);
        newestDocument.setOriginalFileName("cpf.pdf");
        newestDocument.setContentType("application/pdf");
        newestDocument.setFileSize(1200L);
        newestDocument.setStoragePath("uploads/newest.pdf");
        newestDocument.setCreatedAt(LocalDateTime.of(2026, 5, 4, 12, 30));

        CustomerDocument oldestDocument = new CustomerDocument();
        oldestDocument.setExternalId(UUID.randomUUID());
        oldestDocument.setDocumentType(DocumentType.IDENTITY_REGISTER);
        oldestDocument.setOriginalFileName("rg.png");
        oldestDocument.setContentType("image/png");
        oldestDocument.setFileSize(900L);
        oldestDocument.setStoragePath("uploads/oldest.png");
        oldestDocument.setCreatedAt(LocalDateTime.of(2026, 5, 3, 9, 0));

        when(customerOnboardingRepository.findByExternalId(onboardingExternalId)).thenReturn(java.util.Optional.of(onboarding));
        when(customerDocumentRepository.findByOnboardingExternalIdOrderByCreatedAtDesc(onboardingExternalId))
                .thenReturn(List.of(newestDocument, oldestDocument));

        List<DocumentResponse> response = service.getDocuments(onboardingExternalId);

        assertEquals(2, response.size());
        assertEquals(newestDocument.getExternalId(), response.get(0).externalId());
        assertEquals(newestDocument.getDocumentType(), response.get(0).documentType());
        assertEquals(newestDocument.getOriginalFileName(), response.get(0).originalFileName());
        assertEquals(newestDocument.getContentType(), response.get(0).contentType());
        assertEquals(newestDocument.getFileSize(), response.get(0).fileSize());
        assertEquals(newestDocument.getCreatedAt(), response.get(0).createdAt());

        assertEquals(oldestDocument.getExternalId(), response.get(1).externalId());
        assertEquals(oldestDocument.getCreatedAt(), response.get(1).createdAt());
    }

    @Test
    void shouldThrowWhenGettingDocumentsForNonExistingOnboarding() {
        UUID onboardingExternalId = UUID.randomUUID();
        when(customerOnboardingRepository.findByExternalId(onboardingExternalId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(ResourceNotFoundException.class, () -> service.getDocuments(onboardingExternalId));

        assertEquals("Onboarding not found", exception.getMessage());
        verify(customerDocumentRepository, never())
                .findByOnboardingExternalIdOrderByCreatedAtDesc(any(UUID.class));
    }

    @Test
    void shouldUploadDocumentAndUpdateStatusAndCreateAudit() {
        UUID onboardingExternalId = UUID.randomUUID();
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.DOCUMENTS_PENDING);
        onboarding.setExternalId(onboardingExternalId);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "content".getBytes(StandardCharsets.UTF_8)
        );

        when(customerOnboardingRepository.findByExternalId(onboardingExternalId)).thenReturn(java.util.Optional.of(onboarding));
        when(customerDocumentRepository.save(any(CustomerDocument.class))).thenAnswer(invocation -> {
            CustomerDocument document = invocation.getArgument(0);
            document.setExternalId(UUID.randomUUID());
            return document;
        });

        DocumentResponse response = service.uploadDocument(onboardingExternalId, file, DocumentType.CPF);

        assertEquals("document.pdf", response.originalFileName());
        assertEquals(DocumentType.CPF, response.documentType());
        assertEquals(OnboardingStatus.DOCUMENTS_RECEIVED, onboarding.getStatus());
        verify(customerOnboardingRepository).save(onboarding);
        verify(onboardingAuditLogRepository).save(any());
    }

    @Test
    void shouldThrowWhenUploadingDocumentToNonExistingOnboarding() {
        UUID onboardingExternalId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "content".getBytes(StandardCharsets.UTF_8)
        );
        when(customerOnboardingRepository.findByExternalId(onboardingExternalId)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.uploadDocument(onboardingExternalId, file, DocumentType.CPF));
    }

    @Test
    void shouldThrowWhenUploadedFileIsEmpty() {
        UUID onboardingExternalId = UUID.randomUUID();
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.DOCUMENTS_PENDING);
        onboarding.setExternalId(onboardingExternalId);
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[0]);
        when(customerOnboardingRepository.findByExternalId(onboardingExternalId)).thenReturn(java.util.Optional.of(onboarding));

        assertThrows(InvalidDocumentException.class,
                () -> service.uploadDocument(onboardingExternalId, file, DocumentType.CPF));
    }

    @Test
    void shouldThrowWhenUploadedFileHasUnsupportedContentType() {
        UUID onboardingExternalId = UUID.randomUUID();
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.DOCUMENTS_PENDING);
        onboarding.setExternalId(onboardingExternalId);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "content".getBytes(StandardCharsets.UTF_8)
        );
        when(customerOnboardingRepository.findByExternalId(onboardingExternalId)).thenReturn(java.util.Optional.of(onboarding));

        assertThrows(InvalidDocumentException.class,
                () -> service.uploadDocument(onboardingExternalId, file, DocumentType.CPF));
    }

    @Test
    void shouldThrowWhenUploadedFileExceedsMaxSize() {
        UUID onboardingExternalId = UUID.randomUUID();
        CustomerOnboarding onboarding = onboardingEntity("12345678909", OnboardingStatus.DOCUMENTS_PENDING);
        onboarding.setExternalId(onboardingExternalId);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                new byte[(5 * 1024 * 1024) + 1]
        );
        when(customerOnboardingRepository.findByExternalId(onboardingExternalId)).thenReturn(java.util.Optional.of(onboarding));

        assertThrows(InvalidDocumentException.class,
                () -> service.uploadDocument(onboardingExternalId, file, DocumentType.CPF));
    }

    private CreateOnboardingRequest validRequest() {
        return new CreateOnboardingRequest(
                "John Doe",
                "123.456.789-09",
                List.of(new EmailRequest("john@example.com", true)),
                List.of(new PhoneRequest("11999999999", false)),
                List.of(new AddressRequest("01001-000", "100", "Apt 10", true))
        );
    }

    private CustomerOnboarding onboardingEntity(String cpf, OnboardingStatus status) {
        CustomerOnboarding onboarding = new CustomerOnboarding();
        onboarding.setExternalId(UUID.randomUUID());
        onboarding.setProtocol("58392017463051");
        onboarding.setFullName("John Doe");
        onboarding.setCpf(cpf);
        onboarding.setStatus(status);
        return onboarding;
    }
}

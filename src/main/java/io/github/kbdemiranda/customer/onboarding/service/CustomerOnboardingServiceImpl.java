package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.dto.AddressData;
import io.github.kbdemiranda.customer.onboarding.dto.audit.AuditLogResponse;
import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.document.DocumentResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.AddressRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.CreateOnboardingRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.EmailRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingFilter;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.PhoneRequest;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerAddress;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerDocument;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerEmail;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerPhone;
import io.github.kbdemiranda.customer.onboarding.entity.OnboardingAuditLog;
import io.github.kbdemiranda.customer.onboarding.enums.AuditAction;
import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import io.github.kbdemiranda.customer.onboarding.exception.BusinessValidationException;
import io.github.kbdemiranda.customer.onboarding.exception.CpfAlreadyExistsException;
import io.github.kbdemiranda.customer.onboarding.exception.InvalidDocumentException;
import io.github.kbdemiranda.customer.onboarding.exception.ResourceNotFoundException;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerAddressMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerDocumentMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerEmailMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerOnboardingMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerPhoneMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.AuditLogMapper;
import io.github.kbdemiranda.customer.onboarding.repository.CustomerDocumentRepository;
import io.github.kbdemiranda.customer.onboarding.repository.CustomerOnboardingRepository;
import io.github.kbdemiranda.customer.onboarding.repository.OnboardingAuditLogRepository;
import io.github.kbdemiranda.customer.onboarding.repository.specification.CustomerOnboardingSpecification;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CustomerOnboardingServiceImpl implements CustomerOnboardingService {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    private final CustomerOnboardingRepository customerOnboardingRepository;
    private final CustomerDocumentRepository customerDocumentRepository;
    private final OnboardingAuditLogRepository onboardingAuditLogRepository;
    private final ZipCodeService zipCodeService;
    private final CustomerOnboardingMapper customerOnboardingMapper;
    private final CustomerEmailMapper customerEmailMapper;
    private final CustomerPhoneMapper customerPhoneMapper;
    private final CustomerAddressMapper customerAddressMapper;
    private final CustomerDocumentMapper customerDocumentMapper;
    private final AuditLogMapper auditLogMapper;

    public CustomerOnboardingServiceImpl(CustomerOnboardingRepository customerOnboardingRepository,
                                         CustomerDocumentRepository customerDocumentRepository,
                                         OnboardingAuditLogRepository onboardingAuditLogRepository,
                                         ZipCodeService zipCodeService,
                                         CustomerOnboardingMapper customerOnboardingMapper,
                                         CustomerEmailMapper customerEmailMapper,
                                         CustomerPhoneMapper customerPhoneMapper,
                                         CustomerAddressMapper customerAddressMapper,
                                         CustomerDocumentMapper customerDocumentMapper,
                                         AuditLogMapper auditLogMapper) {
        this.customerOnboardingRepository = customerOnboardingRepository;
        this.customerDocumentRepository = customerDocumentRepository;
        this.onboardingAuditLogRepository = onboardingAuditLogRepository;
        this.zipCodeService = zipCodeService;
        this.customerOnboardingMapper = customerOnboardingMapper;
        this.customerEmailMapper = customerEmailMapper;
        this.customerPhoneMapper = customerPhoneMapper;
        this.customerAddressMapper = customerAddressMapper;
        this.customerDocumentMapper = customerDocumentMapper;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    @Transactional
    public OnboardingResponse createOnboarding(CreateOnboardingRequest request) {
        validateRequest(request);

        String normalizedCpf = normalizeDigits(request.cpf());
        if (customerOnboardingRepository.existsByCpf(normalizedCpf)) {
            throw new CpfAlreadyExistsException(normalizedCpf);
        }

        CustomerOnboarding onboarding = customerOnboardingMapper.toEntity(request);
        onboarding.setCpf(normalizedCpf);
        onboarding.setStatus(OnboardingStatus.DOCUMENTS_PENDING);

        List<CustomerEmail> emails = request.emails().stream()
                .map(emailRequest -> customerEmailMapper.toEntity(emailRequest, onboarding))
                .toList();

        List<CustomerPhone> phones = request.phones().stream()
                .map(phoneRequest -> customerPhoneMapper.toEntity(phoneRequest, onboarding))
                .toList();

        List<CustomerAddress> addresses = request.addresses().stream()
                .map(addressRequest -> toEnrichedAddress(addressRequest, onboarding))
                .toList();

        onboarding.setEmails(emails);
        onboarding.setPhones(phones);
        onboarding.setAddresses(addresses);

        CustomerOnboarding savedOnboarding = customerOnboardingRepository.save(onboarding);

        OnboardingAuditLog auditLog = new OnboardingAuditLog();
        auditLog.setOnboarding(savedOnboarding);
        auditLog.setAction(AuditAction.ONBOARDING_CREATED);
        auditLog.setStatus("SUCCESS");
        auditLog.setMessage("Onboarding created successfully");
        onboardingAuditLogRepository.save(auditLog);

        return customerOnboardingMapper.toResponse(savedOnboarding);
    }

    @Override
    public OnboardingResponse getByExternalId(UUID externalId) {
        CustomerOnboarding onboarding = customerOnboardingRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found"));
        return customerOnboardingMapper.toResponse(onboarding);
    }

    @Override
    public PageResponse<OnboardingResponse> listOnboardings(OnboardingFilter criteria) {
        String normalizedCpf = normalizeDigits(criteria.cpf());
        Specification<CustomerOnboarding> specification = Specification
                .where(CustomerOnboardingSpecification.cpfEquals(normalizedCpf.isBlank() ? null : normalizedCpf))
                .and(CustomerOnboardingSpecification.statusEquals(criteria.status()));

        Pageable pageable = PageRequest.of(
                criteria.page(),
                criteria.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<CustomerOnboarding> onboardingPage = customerOnboardingRepository.findAll(specification, pageable);
        List<OnboardingResponse> content = onboardingPage.getContent().stream()
                .map(customerOnboardingMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                onboardingPage.getNumber(),
                onboardingPage.getSize(),
                onboardingPage.getTotalElements(),
                onboardingPage.getTotalPages()
        );
    }

    @Override
    @Transactional
    public DocumentResponse uploadDocument(UUID externalId, MultipartFile file, DocumentType documentType) {
        CustomerOnboarding onboarding = customerOnboardingRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found"));

        validateDocument(file, documentType);

        String originalFileName = file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()
                ? "document"
                : file.getOriginalFilename();
        Path storagePath = storeFile(externalId, originalFileName, file);

        CustomerDocument document = new CustomerDocument();
        document.setOnboarding(onboarding);
        document.setOriginalFileName(originalFileName);
        document.setContentType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setDocumentType(documentType);
        document.setStoragePath(storagePath.toString());
        CustomerDocument savedDocument = customerDocumentRepository.save(document);

        onboarding.setStatus(OnboardingStatus.DOCUMENTS_RECEIVED);
        customerOnboardingRepository.save(onboarding);

        OnboardingAuditLog auditLog = new OnboardingAuditLog();
        auditLog.setOnboarding(onboarding);
        auditLog.setAction(AuditAction.DOCUMENT_UPLOADED);
        auditLog.setStatus("SUCCESS");
        auditLog.setMessage("Document uploaded successfully");
        onboardingAuditLogRepository.save(auditLog);

        return customerDocumentMapper.toResponse(savedDocument);
    }

    @Override
    public List<DocumentResponse> getDocuments(UUID onboardingExternalId) {
        customerOnboardingRepository.findByExternalId(onboardingExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found"));

        List<CustomerDocument> documents =
                customerDocumentRepository.findByOnboardingExternalIdOrderByCreatedAtDesc(onboardingExternalId);
        return customerDocumentMapper.toResponseList(documents);
    }

    @Override
    public List<AuditLogResponse> getAuditLogs(UUID onboardingExternalId) {
        customerOnboardingRepository.findByExternalId(onboardingExternalId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found"));

        List<OnboardingAuditLog> auditLogs =
                onboardingAuditLogRepository.findByOnboardingExternalIdOrderByCreatedAtDesc(onboardingExternalId);

        return auditLogMapper.toResponseList(auditLogs);
    }

    private CustomerAddress toEnrichedAddress(AddressRequest request, CustomerOnboarding onboarding) {
        String normalizedZipCode = normalizeDigits(request.zipCode());
        AddressData addressData = zipCodeService.getAddressOrThrow(normalizedZipCode);

        CustomerAddress address = customerAddressMapper.toEntity(request, onboarding);
        address.setZipCode(normalizedZipCode);
        address.setStreet(addressData.street());
        address.setNeighborhood(addressData.neighborhood());
        address.setCity(addressData.city());
        address.setState(addressData.state());
        return address;
    }

    private void validateRequest(CreateOnboardingRequest request) {
        if (request == null) {
            throw new BusinessValidationException("Request must not be null");
        }
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new BusinessValidationException("fullName must not be blank");
        }
        if (request.emails() == null) {
            throw new BusinessValidationException("emails must not be null");
        }
        if (request.phones() == null) {
            throw new BusinessValidationException("phones must not be null");
        }
        if (request.addresses() == null) {
            throw new BusinessValidationException("addresses must not be null");
        }

        validatePrimaryEmails(request.emails());
        validatePrimaryPhones(request.phones());
        validatePrimaryAddresses(request.addresses());
        validatePrimaryContactMethod(request.emails(), request.phones());
    }

    private void validatePrimaryEmails(List<EmailRequest> emails) {
        long primaryCount = emails.stream().filter(email -> Boolean.TRUE.equals(email.primaryEmail())).count();
        if (primaryCount > 1) {
            throw new BusinessValidationException("At most one primary email is allowed");
        }
    }

    private void validatePrimaryPhones(List<PhoneRequest> phones) {
        long primaryCount = phones.stream().filter(phone -> Boolean.TRUE.equals(phone.primaryPhone())).count();
        if (primaryCount > 1) {
            throw new BusinessValidationException("At most one primary phone is allowed");
        }
    }

    private void validatePrimaryAddresses(List<AddressRequest> addresses) {
        long primaryCount = addresses.stream().filter(address -> Boolean.TRUE.equals(address.primaryAddress())).count();
        if (primaryCount > 1) {
            throw new BusinessValidationException("At most one primary address is allowed");
        }
    }

    private void validatePrimaryContactMethod(List<EmailRequest> emails, List<PhoneRequest> phones) {
        boolean hasPrimaryEmail = emails.stream().anyMatch(email -> Boolean.TRUE.equals(email.primaryEmail()));
        boolean hasPrimaryPhone = phones.stream().anyMatch(phone -> Boolean.TRUE.equals(phone.primaryPhone()));
        if (!hasPrimaryEmail && !hasPrimaryPhone) {
            throw new BusinessValidationException("At least one contact method must be primary (email or phone)");
        }
    }

    private String normalizeDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private void validateDocument(MultipartFile file, DocumentType documentType) {
        if (documentType == null) {
            throw new InvalidDocumentException("Document type is required");
        }
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("File must not be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidDocumentException("Unsupported file type");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidDocumentException("File size exceeds the maximum limit of 5MB");
        }
    }

    private Path storeFile(UUID onboardingExternalId, String originalFileName, MultipartFile file) {
        try {
            Path directory = Path.of("uploads", onboardingExternalId.toString());
            Files.createDirectories(directory);

            String extension = resolveExtension(originalFileName);
            String storedFileName = UUID.randomUUID() + extension;
            Path destination = directory.resolve(storedFileName);
            file.transferTo(destination);
            return destination;
        } catch (IOException ex) {
            throw new InvalidDocumentException("Failed to store document");
        }
    }

    private String resolveExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex);
    }
}

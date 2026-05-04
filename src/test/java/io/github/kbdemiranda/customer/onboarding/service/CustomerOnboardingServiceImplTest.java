package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.dto.AddressData;
import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.AddressRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.CreateOnboardingRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.EmailRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingFilter;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.PhoneRequest;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import io.github.kbdemiranda.customer.onboarding.exception.BusinessValidationException;
import io.github.kbdemiranda.customer.onboarding.exception.CpfAlreadyExistsException;
import io.github.kbdemiranda.customer.onboarding.exception.ZipCodeNotFoundException;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerAddressMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerEmailMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerOnboardingMapper;
import io.github.kbdemiranda.customer.onboarding.mapper.CustomerPhoneMapper;
import io.github.kbdemiranda.customer.onboarding.repository.CustomerOnboardingRepository;
import io.github.kbdemiranda.customer.onboarding.repository.OnboardingAuditLogRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerOnboardingServiceImplTest {

    private final CustomerOnboardingRepository customerOnboardingRepository = mock(CustomerOnboardingRepository.class);
    private final OnboardingAuditLogRepository onboardingAuditLogRepository = mock(OnboardingAuditLogRepository.class);
    private final ZipCodeService zipCodeService = mock(ZipCodeService.class);

    private CustomerOnboardingServiceImpl service;

    @BeforeEach
    void setUp() {
        CustomerAddressMapper customerAddressMapper = new CustomerAddressMapper();
        CustomerEmailMapper customerEmailMapper = new CustomerEmailMapper();
        CustomerPhoneMapper customerPhoneMapper = new CustomerPhoneMapper();
        CustomerOnboardingMapper customerOnboardingMapper =
                new CustomerOnboardingMapper(customerAddressMapper, customerEmailMapper, customerPhoneMapper);

        service = new CustomerOnboardingServiceImpl(
                customerOnboardingRepository,
                onboardingAuditLogRepository,
                zipCodeService,
                customerOnboardingMapper,
                customerEmailMapper,
                customerPhoneMapper,
                customerAddressMapper
        );
    }

    @Test
    void shouldCreateOnboardingWithZipCodeEnrichmentAndAuditLog() {
        CreateOnboardingRequest request = validRequest();
        AddressData addressData = new AddressData("01001000", "Praca da Se", "Se", "Sao Paulo", "SP");

        when(customerOnboardingRepository.existsByCpf("12345678909")).thenReturn(false);
        when(zipCodeService.getAddressOrThrow("01001000")).thenReturn(addressData);
        when(customerOnboardingRepository.save(any(CustomerOnboarding.class))).thenAnswer(invocation -> {
            CustomerOnboarding onboarding = invocation.getArgument(0);
            onboarding.setExternalId(UUID.randomUUID());
            return onboarding;
        });

        OnboardingResponse response = service.createOnboarding(request);

        assertEquals("12345678909", response.cpf());
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
        when(zipCodeService.getAddressOrThrow("01001000")).thenThrow(new ZipCodeNotFoundException());

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
        onboarding.setFullName("John Doe");
        onboarding.setCpf(cpf);
        onboarding.setStatus(status);
        return onboarding;
    }
}

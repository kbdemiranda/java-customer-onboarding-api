package io.github.kbdemiranda.customer.onboarding.mapper;

import io.github.kbdemiranda.customer.onboarding.dto.onboarding.CreateOnboardingRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import org.springframework.stereotype.Component;

@Component
public class CustomerOnboardingMapper {

    private final CustomerAddressMapper customerAddressMapper;
    private final CustomerEmailMapper customerEmailMapper;
    private final CustomerPhoneMapper customerPhoneMapper;

    public CustomerOnboardingMapper(
            CustomerAddressMapper customerAddressMapper,
            CustomerEmailMapper customerEmailMapper,
            CustomerPhoneMapper customerPhoneMapper
    ) {
        this.customerAddressMapper = customerAddressMapper;
        this.customerEmailMapper = customerEmailMapper;
        this.customerPhoneMapper = customerPhoneMapper;
    }

    public CustomerOnboarding toEntity(CreateOnboardingRequest request) {
        CustomerOnboarding entity = new CustomerOnboarding();
        entity.setFullName(request.fullName());
        entity.setCpf(request.cpf());
        return entity;
    }

    public OnboardingResponse toResponse(CustomerOnboarding entity) {
        return new OnboardingResponse(
                entity.getExternalId(),
                entity.getProtocol(),
                entity.getFullName(),
                entity.getCpf(),
                entity.getStatus(),
                customerEmailMapper.toResponseList(entity.getEmails()),
                customerPhoneMapper.toResponseList(entity.getPhones()),
                customerAddressMapper.toResponseList(entity.getAddresses()),
                entity.getCreatedAt()
        );
    }
}

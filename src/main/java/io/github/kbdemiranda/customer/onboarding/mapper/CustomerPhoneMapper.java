package io.github.kbdemiranda.customer.onboarding.mapper;

import io.github.kbdemiranda.customer.onboarding.dto.onboarding.PhoneRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.PhoneResponse;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerPhone;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomerPhoneMapper {

    public CustomerPhone toEntity(PhoneRequest request, CustomerOnboarding onboarding) {
        CustomerPhone entity = new CustomerPhone();
        entity.setOnboarding(onboarding);
        entity.setPhoneNumber(request.phoneNumber());
        entity.setPrimaryPhone(request.primaryPhone());
        return entity;
    }

    public PhoneResponse toResponse(CustomerPhone entity) {
        return new PhoneResponse(
                entity.getExternalId(),
                entity.getPhoneNumber(),
                entity.getPrimaryPhone()
        );
    }

    public List<PhoneResponse> toResponseList(List<CustomerPhone> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}

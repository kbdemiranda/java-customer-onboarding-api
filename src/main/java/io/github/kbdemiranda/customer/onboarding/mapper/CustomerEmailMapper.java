package io.github.kbdemiranda.customer.onboarding.mapper;

import io.github.kbdemiranda.customer.onboarding.dto.onboarding.EmailRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.EmailResponse;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerEmail;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomerEmailMapper {

    public CustomerEmail toEntity(EmailRequest request, CustomerOnboarding onboarding) {
        CustomerEmail entity = new CustomerEmail();
        entity.setOnboarding(onboarding);
        entity.setEmail(request.email());
        entity.setPrimaryEmail(request.primaryEmail());
        return entity;
    }

    public EmailResponse toResponse(CustomerEmail entity) {
        return new EmailResponse(
                entity.getExternalId(),
                entity.getEmail(),
                entity.getPrimaryEmail()
        );
    }

    public List<EmailResponse> toResponseList(List<CustomerEmail> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}

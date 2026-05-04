package io.github.kbdemiranda.customer.onboarding.mapper;

import io.github.kbdemiranda.customer.onboarding.dto.onboarding.AddressRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.AddressResponse;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerAddress;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomerAddressMapper {

    public CustomerAddress toEntity(AddressRequest request, CustomerOnboarding onboarding) {
        CustomerAddress entity = new CustomerAddress();
        entity.setOnboarding(onboarding);
        entity.setZipCode(request.zipCode());
        entity.setNumber(request.number());
        entity.setComplement(request.complement());
        entity.setPrimaryAddress(request.primaryAddress());
        return entity;
    }

    public AddressResponse toResponse(CustomerAddress entity) {
        return new AddressResponse(
                entity.getExternalId(),
                entity.getZipCode(),
                entity.getStreet(),
                entity.getNumber(),
                entity.getComplement(),
                entity.getNeighborhood(),
                entity.getCity(),
                entity.getState(),
                entity.getPrimaryAddress()
        );
    }

    public List<AddressResponse> toResponseList(List<CustomerAddress> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}

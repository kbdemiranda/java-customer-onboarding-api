package io.github.kbdemiranda.customer.onboarding.repository;

import io.github.kbdemiranda.customer.onboarding.entity.CustomerAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    Optional<CustomerAddress> findByExternalId(UUID externalId);

    List<CustomerAddress> findByOnboardingExternalId(UUID onboardingExternalId);
}

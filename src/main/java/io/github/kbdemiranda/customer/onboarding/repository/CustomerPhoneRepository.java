package io.github.kbdemiranda.customer.onboarding.repository;

import io.github.kbdemiranda.customer.onboarding.entity.CustomerPhone;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPhoneRepository extends JpaRepository<CustomerPhone, Long> {

    Optional<CustomerPhone> findByExternalId(UUID externalId);

    List<CustomerPhone> findByOnboardingExternalId(UUID onboardingExternalId);
}

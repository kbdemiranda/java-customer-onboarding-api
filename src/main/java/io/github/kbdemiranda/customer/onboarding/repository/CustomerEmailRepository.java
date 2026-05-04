package io.github.kbdemiranda.customer.onboarding.repository;

import io.github.kbdemiranda.customer.onboarding.entity.CustomerEmail;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerEmailRepository extends JpaRepository<CustomerEmail, Long> {

    Optional<CustomerEmail> findByExternalId(UUID externalId);

    List<CustomerEmail> findByOnboardingExternalId(UUID onboardingExternalId);
}

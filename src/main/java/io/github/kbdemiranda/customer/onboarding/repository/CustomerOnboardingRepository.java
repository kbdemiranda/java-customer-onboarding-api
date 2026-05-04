package io.github.kbdemiranda.customer.onboarding.repository;

import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOnboardingRepository extends JpaRepository<CustomerOnboarding, Long> {

    Optional<CustomerOnboarding> findByExternalId(UUID externalId);

    boolean existsByCpf(String cpf);

    Page<CustomerOnboarding> findByStatus(OnboardingStatus status, Pageable pageable);
}

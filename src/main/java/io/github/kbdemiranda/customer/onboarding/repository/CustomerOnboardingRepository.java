package io.github.kbdemiranda.customer.onboarding.repository;

import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CustomerOnboardingRepository extends JpaRepository<CustomerOnboarding, Long>,
        JpaSpecificationExecutor<CustomerOnboarding> {

    Optional<CustomerOnboarding> findByExternalId(UUID externalId);
    Optional<CustomerOnboarding> findFirstByProtocolOrderByCreatedAtDesc(String protocol);

    boolean existsByCpf(String cpf);

    Page<CustomerOnboarding> findByStatus(OnboardingStatus status, Pageable pageable);
}

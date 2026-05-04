package io.github.kbdemiranda.customer.onboarding.repository;

import io.github.kbdemiranda.customer.onboarding.entity.OnboardingAuditLog;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingAuditLogRepository extends JpaRepository<OnboardingAuditLog, Long> {

    Optional<OnboardingAuditLog> findByExternalId(UUID externalId);

    Page<OnboardingAuditLog> findByOnboardingExternalId(UUID onboardingExternalId, Pageable pageable);
}

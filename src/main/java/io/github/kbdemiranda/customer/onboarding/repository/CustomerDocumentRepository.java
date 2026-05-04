package io.github.kbdemiranda.customer.onboarding.repository;

import io.github.kbdemiranda.customer.onboarding.entity.CustomerDocument;
import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerDocumentRepository extends JpaRepository<CustomerDocument, Long> {

    Optional<CustomerDocument> findByExternalId(UUID externalId);

    Page<CustomerDocument> findByOnboardingExternalId(UUID onboardingExternalId, Pageable pageable);

    Page<CustomerDocument> findByOnboardingExternalIdAndDocumentType(
            UUID onboardingExternalId,
            DocumentType documentType,
            Pageable pageable
    );
}

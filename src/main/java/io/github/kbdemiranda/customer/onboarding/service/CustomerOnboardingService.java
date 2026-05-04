package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.dto.audit.AuditLogResponse;
import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.document.DocumentResponse;
import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.CreateOnboardingRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingFilter;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface CustomerOnboardingService {

    OnboardingResponse createOnboarding(CreateOnboardingRequest request);

    OnboardingResponse getByExternalId(UUID externalId);

    PageResponse<OnboardingResponse> listOnboardings(OnboardingFilter criteria);

    DocumentResponse uploadDocument(UUID externalId, MultipartFile file, DocumentType documentType);

    List<DocumentResponse> getDocuments(UUID onboardingExternalId);

    List<AuditLogResponse> getAuditLogs(UUID onboardingExternalId);
}

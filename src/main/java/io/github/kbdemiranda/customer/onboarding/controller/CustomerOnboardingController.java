package io.github.kbdemiranda.customer.onboarding.controller;

import io.github.kbdemiranda.customer.onboarding.dto.audit.AuditLogResponse;
import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.document.DocumentResponse;
import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.CreateOnboardingRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingFilter;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import io.github.kbdemiranda.customer.onboarding.service.CustomerOnboardingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/api/v1/onboardings")
public class CustomerOnboardingController {

    private final CustomerOnboardingService customerOnboardingService;

    public CustomerOnboardingController(CustomerOnboardingService customerOnboardingService) {
        this.customerOnboardingService = customerOnboardingService;
    }

    @PostMapping
    public ResponseEntity<OnboardingResponse> createOnboarding(@Valid @RequestBody CreateOnboardingRequest request) {
        OnboardingResponse response = customerOnboardingService.createOnboarding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(path = "/{externalId}/documents", consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponse> uploadDocument(@PathVariable UUID externalId,
                                                           @RequestPart("file") MultipartFile file,
                                                           @RequestParam("documentType") DocumentType documentType) {
        DocumentResponse response = customerOnboardingService.uploadDocument(externalId, file, documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<OnboardingResponse> getByExternalId(@PathVariable UUID externalId) {
        OnboardingResponse response = customerOnboardingService.getByExternalId(externalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<OnboardingResponse>> listOnboardings(@Valid @ModelAttribute OnboardingFilter criteria) {
        PageResponse<OnboardingResponse> response = customerOnboardingService.listOnboardings(criteria);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{externalId}/audit-logs")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(@PathVariable UUID externalId) {
        List<AuditLogResponse> response = customerOnboardingService.getAuditLogs(externalId);
        return ResponseEntity.ok(response);
    }
}

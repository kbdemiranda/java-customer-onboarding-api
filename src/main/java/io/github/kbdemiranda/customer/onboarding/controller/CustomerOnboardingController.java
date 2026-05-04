package io.github.kbdemiranda.customer.onboarding.controller;

import io.github.kbdemiranda.customer.onboarding.dto.onboarding.CreateOnboardingRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import io.github.kbdemiranda.customer.onboarding.service.CustomerOnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
}

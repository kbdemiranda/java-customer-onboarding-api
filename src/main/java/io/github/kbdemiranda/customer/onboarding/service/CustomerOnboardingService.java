package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.CreateOnboardingRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingFilter;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;

public interface CustomerOnboardingService {

    OnboardingResponse createOnboarding(CreateOnboardingRequest request);

    PageResponse<OnboardingResponse> listOnboardings(OnboardingFilter criteria);
}

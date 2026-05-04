package io.github.kbdemiranda.customer.onboarding.repository.specification;

import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import io.github.kbdemiranda.customer.onboarding.enums.OnboardingStatus;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerOnboardingSpecification {

    private CustomerOnboardingSpecification() {
    }

    public static Specification<CustomerOnboarding> cpfEquals(String cpf) {
        return (root, query, criteriaBuilder) ->
                cpf == null || cpf.isBlank() ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("cpf"), cpf);
    }

    public static Specification<CustomerOnboarding> statusEquals(OnboardingStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("status"), status);
    }
}

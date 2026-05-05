package io.github.kbdemiranda.customer.onboarding.repository.specification;

import io.github.kbdemiranda.customer.onboarding.entity.ZipCodeQueryLog;
import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeProvider;
import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeQueryStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public final class ZipCodeQueryLogSpecification {

    private ZipCodeQueryLogSpecification() {
    }

    public static Specification<ZipCodeQueryLog> zipCodeEquals(String zipCode) {
        return (root, query, cb) ->
                zipCode == null || zipCode.isBlank() ? cb.conjunction() : cb.equal(root.get("zipCode"), zipCode);
    }

    public static Specification<ZipCodeQueryLog> providerEquals(ZipCodeProvider provider) {
        return (root, query, cb) ->
                provider == null ? cb.conjunction() : cb.equal(root.get("provider"), provider);
    }

    public static Specification<ZipCodeQueryLog> statusEquals(ZipCodeQueryStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<ZipCodeQueryLog> requestTimestampFrom(LocalDateTime dateFrom) {
        return (root, query, cb) ->
                dateFrom == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("requestTimestamp"), dateFrom);
    }

    public static Specification<ZipCodeQueryLog> requestTimestampTo(LocalDateTime dateTo) {
        return (root, query, cb) ->
                dateTo == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("requestTimestamp"), dateTo);
    }
}

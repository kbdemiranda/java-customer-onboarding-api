package io.github.kbdemiranda.customer.onboarding.mapper;

import io.github.kbdemiranda.customer.onboarding.dto.audit.AuditLogResponse;
import io.github.kbdemiranda.customer.onboarding.entity.OnboardingAuditLog;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(OnboardingAuditLog entity) {
        return new AuditLogResponse(
                entity.getExternalId(),
                entity.getAction(),
                entity.getStatus(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }

    public List<AuditLogResponse> toResponseList(List<OnboardingAuditLog> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}

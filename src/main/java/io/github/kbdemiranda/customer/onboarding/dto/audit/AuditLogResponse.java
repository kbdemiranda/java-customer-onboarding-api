package io.github.kbdemiranda.customer.onboarding.dto.audit;

import io.github.kbdemiranda.customer.onboarding.enums.AuditAction;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID externalId,
        AuditAction action,
        String status,
        String message,
        LocalDateTime createdAt
) {
}

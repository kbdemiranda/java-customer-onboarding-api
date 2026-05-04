package io.github.kbdemiranda.customer.onboarding.dto.audit;

import io.github.kbdemiranda.customer.onboarding.enums.AuditAction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Audit log entry response")
public record AuditLogResponse(
        @Schema(description = "Public audit log identifier")
        UUID externalId,
        @Schema(description = "Audit action type")
        AuditAction action,
        @Schema(description = "Audit status", example = "SUCCESS")
        String status,
        @Schema(description = "Audit message", example = "Onboarding created successfully")
        String message,
        @Schema(description = "Audit event timestamp")
        LocalDateTime createdAt
) {
}

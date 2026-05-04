package io.github.kbdemiranda.customer.onboarding.dto.document;

import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID externalId,
        DocumentType documentType,
        String originalFileName,
        String contentType,
        Long fileSize,
        LocalDateTime createdAt
) {
}

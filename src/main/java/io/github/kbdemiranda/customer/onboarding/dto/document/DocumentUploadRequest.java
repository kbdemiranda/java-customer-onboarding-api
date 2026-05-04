package io.github.kbdemiranda.customer.onboarding.dto.document;

import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import jakarta.validation.constraints.NotNull;

public record DocumentUploadRequest(
        @NotNull DocumentType documentType
) {
}

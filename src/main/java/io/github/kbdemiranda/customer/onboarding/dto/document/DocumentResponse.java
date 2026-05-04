package io.github.kbdemiranda.customer.onboarding.dto.document;

import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Uploaded onboarding document response")
public record DocumentResponse(
        @Schema(description = "Public document identifier")
        UUID externalId,
        @Schema(description = "Document type")
        DocumentType documentType,
        @Schema(description = "Original uploaded file name", example = "cpf.pdf")
        String originalFileName,
        @Schema(description = "Uploaded file content type", example = "application/pdf")
        String contentType,
        @Schema(description = "Uploaded file size in bytes", example = "1200")
        Long fileSize,
        @Schema(description = "Upload timestamp")
        LocalDateTime createdAt
) {
}

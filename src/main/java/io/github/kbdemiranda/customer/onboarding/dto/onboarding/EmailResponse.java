package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Email response")
public record EmailResponse(
        @Schema(description = "Public email identifier")
        UUID externalId,
        @Schema(description = "Email address")
        String email,
        @Schema(description = "Indicates whether this is the primary email")
        Boolean primaryEmail
) {
}

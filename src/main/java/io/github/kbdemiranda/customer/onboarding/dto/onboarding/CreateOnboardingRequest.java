package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.validator.constraints.br.CPF;

@Schema(description = "Request payload to create a customer onboarding")
public record CreateOnboardingRequest(
        @Schema(description = "Customer full name", example = "John Doe")
        @NotBlank String fullName,
        @Schema(description = "Customer CPF (formatted or digits only)", example = "123.456.789-09")
        @NotBlank @CPF String cpf,
        @Schema(description = "Customer email contacts")
        @NotNull List<EmailRequest> emails,
        @Schema(description = "Customer phone contacts")
        @NotNull List<PhoneRequest> phones,
        @Schema(description = "Customer addresses")
        @NotNull List<AddressRequest> addresses
) {
}

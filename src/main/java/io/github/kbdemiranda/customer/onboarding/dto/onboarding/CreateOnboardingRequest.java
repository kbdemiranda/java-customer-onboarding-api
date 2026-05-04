package io.github.kbdemiranda.customer.onboarding.dto.onboarding;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.hibernate.validator.constraints.br.CPF;

public record CreateOnboardingRequest(
        @NotBlank String fullName,
        @NotBlank @CPF String cpf,
        @NotNull List<EmailRequest> emails,
        @NotNull List<PhoneRequest> phones,
        @NotNull List<AddressRequest> addresses
) {
}

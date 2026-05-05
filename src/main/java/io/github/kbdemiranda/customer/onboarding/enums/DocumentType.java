package io.github.kbdemiranda.customer.onboarding.enums;

import java.util.Locale;

public enum DocumentType {
    CPF,
    IDENTITY_REGISTER,
    DRIVER_LICENSE,
    PASSPORT,
    PROOF_OF_ADDRESS;

    public static DocumentType fromValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Document type is required");
        }

        String normalized = rawValue.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "CPF" -> CPF;
            case "IDENTITY_REGISTER", "RG" ->
                    IDENTITY_REGISTER;
            case "DRIVER_LICENSE", "CNH" -> DRIVER_LICENSE;
            case "PASSPORT" -> PASSPORT;
            case "PROOF_OF_ADDRESS" ->
                    PROOF_OF_ADDRESS;
            default -> throw new IllegalArgumentException("Invalid document type: " + rawValue);
        };
    }
}

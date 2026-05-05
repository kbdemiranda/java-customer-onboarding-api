package io.github.kbdemiranda.customer.onboarding.dto.zipcode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Zip code lookup response")
public record ZipCodeResponse(
        @Schema(description = "Normalized zip code", example = "01001000")
        String zipCode,
        @Schema(description = "Street name", example = "Praca da Se")
        String street,
        @Schema(description = "Neighborhood", example = "Se")
        String neighborhood,
        @Schema(description = "City", example = "Sao Paulo")
        String city,
        @Schema(description = "State", example = "SP")
        String state
) {
}

package io.github.kbdemiranda.customer.onboarding.controller;

import io.github.kbdemiranda.customer.onboarding.dto.common.ErrorResponse;
import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeResponse;
import io.github.kbdemiranda.customer.onboarding.service.ZipCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/zip-codes")
@Tag(name = "Zip Codes", description = "Zip code lookup operations")
public class ZipCodeController {

    private final ZipCodeService zipCodeService;

    public ZipCodeController(ZipCodeService zipCodeService) {
        this.zipCodeService = zipCodeService;
    }

    @GetMapping("/{zipCode}")
    @Operation(summary = "Search zip code", description = "Searches zip code using WireMock first and ViaCEP as fallback.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zip code found",
                    content = @Content(schema = @Schema(implementation = ZipCodeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid zip code",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"Bad Request\",\"message\":\"zipCode must contain exactly 8 digits\",\"path\":\"/api/v1/zip-codes/123\"}"))),
            @ApiResponse(responseCode = "422", description = "Zip code not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "External provider failure",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ZipCodeResponse> searchZipCode(
            @Parameter(description = "Zip code in formatted or unformatted form", required = true, example = "01001-000")
            @PathVariable String zipCode) {
        return ResponseEntity.ok(zipCodeService.searchZipCode(zipCode));
    }
}

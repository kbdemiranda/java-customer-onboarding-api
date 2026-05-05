package io.github.kbdemiranda.customer.onboarding.controller;

import io.github.kbdemiranda.customer.onboarding.dto.audit.AuditLogResponse;
import io.github.kbdemiranda.customer.onboarding.dto.common.ErrorResponse;
import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.document.DocumentResponse;
import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.CreateOnboardingRequest;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingFilter;
import io.github.kbdemiranda.customer.onboarding.dto.onboarding.OnboardingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.github.kbdemiranda.customer.onboarding.service.CustomerOnboardingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/api/v1/onboardings")
@Tag(name = "Onboardings", description = "Customer onboarding operations")
public class CustomerOnboardingController {

    private final CustomerOnboardingService customerOnboardingService;

    public CustomerOnboardingController(CustomerOnboardingService customerOnboardingService) {
        this.customerOnboardingService = customerOnboardingService;
    }

    @PostMapping
    @Operation(summary = "Create onboarding", description = "Creates a customer onboarding with validated CPF, contact methods, and enriched address data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Onboarding created",
                    content = @Content(schema = @Schema(implementation = OnboardingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "CPF already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Zip code not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "External provider failure",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OnboardingResponse> createOnboarding(@Valid @RequestBody CreateOnboardingRequest request) {
        OnboardingResponse response = customerOnboardingService.createOnboarding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(path = "/{externalId}/documents", consumes = "multipart/form-data")
    @Operation(summary = "Upload onboarding document", description = "Uploads a document file for an existing onboarding using multipart/form-data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document uploaded",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or document type",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Onboarding not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DocumentResponse> uploadDocument(
                                                           @Parameter(description = "Onboarding external identifier", required = true)
                                                           @PathVariable UUID externalId,
                                                           @Parameter(description = "Document file to upload", required = true,
                                                                   content = @Content(mediaType = "multipart/form-data"))
                                                           @RequestPart("file") MultipartFile file,
                                                           @Parameter(description = "Document type. Allowed values: CPF, IDENTITY_REGISTER, DRIVER_LICENSE, PASSPORT, PROOF_OF_ADDRESS", required = true)
                                                           @RequestParam("documentType") DocumentType documentType) {
        DocumentResponse response = customerOnboardingService.uploadDocument(externalId, file, documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/protocol/{protocol}")
    @Operation(summary = "Get onboarding by protocol", description = "Returns onboarding details by user-friendly protocol.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Onboarding found",
                    content = @Content(schema = @Schema(implementation = OnboardingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid protocol format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Onboarding not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OnboardingResponse> getByProtocol(
            @Parameter(description = "Onboarding protocol (14 digits)", required = true, example = "58392017463051")
            @PathVariable @Pattern(regexp = "^\\d{14}$", message = "protocol must contain exactly 14 digits") String protocol) {
        OnboardingResponse response = customerOnboardingService.getByProtocol(protocol);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List onboardings", description = "Returns a paginated list of onboardings filtered by optional cpf and status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Onboardings listed",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or filter parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<OnboardingResponse>> listOnboardings(
            @Parameter(description = "Pagination and filter criteria")
            @Valid @ModelAttribute OnboardingFilter criteria) {
        PageResponse<OnboardingResponse> response = customerOnboardingService.listOnboardings(criteria);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{externalId}/audit-logs")
    @Operation(summary = "List onboarding audit logs", description = "Returns audit log entries for an onboarding ordered by newest first.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit logs listed",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AuditLogResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid externalId format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Onboarding not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @Parameter(description = "Onboarding external identifier", required = true)
            @PathVariable UUID externalId) {
        List<AuditLogResponse> response = customerOnboardingService.getAuditLogs(externalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{externalId}/documents")
    @Operation(summary = "List onboarding documents", description = "Returns uploaded documents for an onboarding ordered by newest first.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents listed",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DocumentResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid externalId format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Onboarding not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @Parameter(description = "Onboarding external identifier", required = true)
            @PathVariable UUID externalId) {
        List<DocumentResponse> response = customerOnboardingService.getDocuments(externalId);
        return ResponseEntity.ok(response);
    }
}

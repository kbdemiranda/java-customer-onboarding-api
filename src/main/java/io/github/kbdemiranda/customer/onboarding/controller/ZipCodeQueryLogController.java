package io.github.kbdemiranda.customer.onboarding.controller;

import io.github.kbdemiranda.customer.onboarding.dto.common.ErrorResponse;
import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeQueryLogFilter;
import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeQueryLogResponse;
import io.github.kbdemiranda.customer.onboarding.service.ZipCodeQueryLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/zip-code-query-logs")
@Tag(name = "Zip Code Query Logs", description = "Zip code query log operations")
public class ZipCodeQueryLogController {

    private final ZipCodeQueryLogService zipCodeQueryLogService;

    public ZipCodeQueryLogController(ZipCodeQueryLogService zipCodeQueryLogService) {
        this.zipCodeQueryLogService = zipCodeQueryLogService;
    }

    @GetMapping
    @Operation(summary = "List zip code query logs", description = "Returns paginated logs with optional filters.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs listed",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<ZipCodeQueryLogResponse>> listLogs(
            @Parameter(description = "Pagination and filter criteria")
            @Valid @ModelAttribute ZipCodeQueryLogFilter filter) {
        return ResponseEntity.ok(zipCodeQueryLogService.listLogs(filter));
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Get zip code query log by externalId", description = "Returns a single zip code query log by public external identifier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Log found",
                    content = @Content(schema = @Schema(implementation = ZipCodeQueryLogResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid externalId format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Log not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ZipCodeQueryLogResponse> getByExternalId(@PathVariable UUID externalId) {
        return ResponseEntity.ok(zipCodeQueryLogService.getByExternalId(externalId));
    }
}

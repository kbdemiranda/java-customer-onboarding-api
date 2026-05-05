package io.github.kbdemiranda.customer.onboarding.controller;

import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeQueryLogResponse;
import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeProvider;
import io.github.kbdemiranda.customer.onboarding.enums.ZipCodeQueryStatus;
import io.github.kbdemiranda.customer.onboarding.exception.GlobalExceptionHandler;
import io.github.kbdemiranda.customer.onboarding.exception.ResourceNotFoundException;
import io.github.kbdemiranda.customer.onboarding.service.ZipCodeQueryLogService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ZipCodeQueryLogController.class)
@Import(GlobalExceptionHandler.class)
class ZipCodeQueryLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ZipCodeQueryLogService zipCodeQueryLogService;

    @Test
    void shouldReturn200WhenListingLogs() throws Exception {
        ZipCodeQueryLogResponse log = new ZipCodeQueryLogResponse(
                UUID.randomUUID(),
                "01001000",
                ZipCodeProvider.WIREMOCK,
                ZipCodeQueryStatus.SUCCESS,
                LocalDateTime.now(),
                "{\"zipCode\":\"01001000\"}",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(zipCodeQueryLogService.listLogs(any())).thenReturn(new PageResponse<>(List.of(log), 0, 10, 1, 1));

        mockMvc.perform(get("/api/v1/zip-code-query-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].zipCode").value("01001000"));
    }

    @Test
    void shouldReturn200WhenGettingLogByExternalId() throws Exception {
        UUID externalId = UUID.randomUUID();
        ZipCodeQueryLogResponse log = new ZipCodeQueryLogResponse(
                externalId,
                "01001000",
                ZipCodeProvider.WIREMOCK,
                ZipCodeQueryStatus.SUCCESS,
                LocalDateTime.now(),
                "{\"zipCode\":\"01001000\"}",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(zipCodeQueryLogService.getByExternalId(externalId)).thenReturn(log);

        mockMvc.perform(get("/api/v1/zip-code-query-logs/{externalId}", externalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(externalId.toString()));
    }

    @Test
    void shouldReturn404WhenLogIsNotFound() throws Exception {
        UUID externalId = UUID.randomUUID();
        when(zipCodeQueryLogService.getByExternalId(externalId))
                .thenThrow(new ResourceNotFoundException("Zip code query log not found"));

        mockMvc.perform(get("/api/v1/zip-code-query-logs/{externalId}", externalId))
                .andExpect(status().isNotFound());
    }
}

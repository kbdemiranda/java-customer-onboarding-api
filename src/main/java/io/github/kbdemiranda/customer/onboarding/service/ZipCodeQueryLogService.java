package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeQueryLogFilter;
import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeQueryLogResponse;
import java.util.UUID;

public interface ZipCodeQueryLogService {

    PageResponse<ZipCodeQueryLogResponse> listLogs(ZipCodeQueryLogFilter filter);

    ZipCodeQueryLogResponse getByExternalId(UUID externalId);
}

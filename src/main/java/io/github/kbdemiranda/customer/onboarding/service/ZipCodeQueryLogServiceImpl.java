package io.github.kbdemiranda.customer.onboarding.service;

import io.github.kbdemiranda.customer.onboarding.dto.common.PageResponse;
import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeQueryLogFilter;
import io.github.kbdemiranda.customer.onboarding.dto.zipcode.ZipCodeQueryLogResponse;
import io.github.kbdemiranda.customer.onboarding.entity.ZipCodeQueryLog;
import io.github.kbdemiranda.customer.onboarding.exception.ResourceNotFoundException;
import io.github.kbdemiranda.customer.onboarding.repository.ZipCodeQueryLogRepository;
import io.github.kbdemiranda.customer.onboarding.repository.specification.ZipCodeQueryLogSpecification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ZipCodeQueryLogServiceImpl implements ZipCodeQueryLogService {

    private final ZipCodeQueryLogRepository zipCodeQueryLogRepository;

    public ZipCodeQueryLogServiceImpl(ZipCodeQueryLogRepository zipCodeQueryLogRepository) {
        this.zipCodeQueryLogRepository = zipCodeQueryLogRepository;
    }

    @Override
    public PageResponse<ZipCodeQueryLogResponse> listLogs(ZipCodeQueryLogFilter filter) {
        String normalizedZipCode = normalizeDigits(filter.zipCode());
        Specification<ZipCodeQueryLog> specification = Specification
                .where(ZipCodeQueryLogSpecification.zipCodeEquals(normalizedZipCode.isBlank() ? null : normalizedZipCode))
                .and(ZipCodeQueryLogSpecification.providerEquals(filter.provider()))
                .and(ZipCodeQueryLogSpecification.statusEquals(filter.status()))
                .and(ZipCodeQueryLogSpecification.requestTimestampFrom(filter.dateFrom()))
                .and(ZipCodeQueryLogSpecification.requestTimestampTo(filter.dateTo()));

        Pageable pageable = PageRequest.of(
                filter.page(),
                filter.size(),
                Sort.by(Sort.Direction.DESC, "requestTimestamp")
        );

        Page<ZipCodeQueryLogResponse> page = zipCodeQueryLogRepository.findAll(specification, pageable)
                .map(this::toResponse);

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public ZipCodeQueryLogResponse getByExternalId(UUID externalId) {
        ZipCodeQueryLog log = zipCodeQueryLogRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Zip code query log not found"));
        return toResponse(log);
    }

    private ZipCodeQueryLogResponse toResponse(ZipCodeQueryLog log) {
        return new ZipCodeQueryLogResponse(
                log.getExternalId(),
                log.getZipCode(),
                log.getProvider(),
                log.getStatus(),
                log.getRequestTimestamp(),
                log.getResponseBody(),
                log.getErrorMessage(),
                log.getCreatedAt(),
                log.getUpdatedAt()
        );
    }

    private String normalizeDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }
}

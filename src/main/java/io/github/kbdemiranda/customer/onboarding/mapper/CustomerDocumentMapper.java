package io.github.kbdemiranda.customer.onboarding.mapper;

import io.github.kbdemiranda.customer.onboarding.dto.document.DocumentResponse;
import io.github.kbdemiranda.customer.onboarding.dto.document.DocumentUploadRequest;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerDocument;
import io.github.kbdemiranda.customer.onboarding.entity.CustomerOnboarding;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CustomerDocumentMapper {

    public CustomerDocument toEntity(DocumentUploadRequest request, CustomerOnboarding onboarding) {
        CustomerDocument entity = new CustomerDocument();
        entity.setOnboarding(onboarding);
        entity.setDocumentType(request.documentType());
        return entity;
    }

    public DocumentResponse toResponse(CustomerDocument entity) {
        return new DocumentResponse(
                entity.getExternalId(),
                entity.getOriginalFileName(),
                entity.getContentType(),
                entity.getFileSize(),
                entity.getDocumentType(),
                entity.getStoragePath(),
                entity.getCreatedAt()
        );
    }

    public List<DocumentResponse> toResponseList(List<CustomerDocument> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toResponse).toList();
    }
}

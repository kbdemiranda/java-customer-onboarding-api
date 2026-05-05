package io.github.kbdemiranda.customer.onboarding.config;

import io.github.kbdemiranda.customer.onboarding.enums.DocumentType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DocumentTypeConverter implements Converter<String, DocumentType> {

    @Override
    public DocumentType convert(String source) {
        return DocumentType.fromValue(source);
    }
}

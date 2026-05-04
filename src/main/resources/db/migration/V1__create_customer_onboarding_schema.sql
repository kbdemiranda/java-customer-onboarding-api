CREATE TABLE customer_onboardings (
    id BIGSERIAL,
    external_id UUID NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_customer_onboardings PRIMARY KEY (id),
    CONSTRAINT uk_customer_onboardings_external_id UNIQUE (external_id),
    CONSTRAINT uk_customer_onboardings_cpf UNIQUE (cpf)
);

CREATE TABLE customer_addresses (
    id BIGSERIAL,
    external_id UUID NOT NULL,
    onboarding_id BIGINT NOT NULL,
    zip_code VARCHAR(9) NOT NULL,
    street VARCHAR(255) NOT NULL,
    number VARCHAR(50) NOT NULL,
    complement VARCHAR(255),
    neighborhood VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    state VARCHAR(2) NOT NULL,
    primary_address BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_customer_addresses PRIMARY KEY (id),
    CONSTRAINT uk_customer_addresses_external_id UNIQUE (external_id),
    CONSTRAINT fk_customer_addresses_onboarding FOREIGN KEY (onboarding_id) REFERENCES customer_onboardings (id)
);

CREATE TABLE customer_emails (
    id BIGSERIAL,
    external_id UUID NOT NULL,
    onboarding_id BIGINT NOT NULL,
    email VARCHAR(320) NOT NULL,
    primary_email BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_customer_emails PRIMARY KEY (id),
    CONSTRAINT uk_customer_emails_external_id UNIQUE (external_id),
    CONSTRAINT fk_customer_emails_onboarding FOREIGN KEY (onboarding_id) REFERENCES customer_onboardings (id)
);

CREATE TABLE customer_phones (
    id BIGSERIAL,
    external_id UUID NOT NULL,
    onboarding_id BIGINT NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    primary_phone BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_customer_phones PRIMARY KEY (id),
    CONSTRAINT uk_customer_phones_external_id UNIQUE (external_id),
    CONSTRAINT fk_customer_phones_onboarding FOREIGN KEY (onboarding_id) REFERENCES customer_onboardings (id)
);

CREATE TABLE customer_documents (
    id BIGSERIAL,
    external_id UUID NOT NULL,
    onboarding_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_customer_documents PRIMARY KEY (id),
    CONSTRAINT uk_customer_documents_external_id UNIQUE (external_id),
    CONSTRAINT fk_customer_documents_onboarding FOREIGN KEY (onboarding_id) REFERENCES customer_onboardings (id)
);

CREATE TABLE onboarding_audit_logs (
    id BIGSERIAL,
    external_id UUID NOT NULL,
    onboarding_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_onboarding_audit_logs PRIMARY KEY (id),
    CONSTRAINT uk_onboarding_audit_logs_external_id UNIQUE (external_id),
    CONSTRAINT fk_onboarding_audit_logs_onboarding FOREIGN KEY (onboarding_id) REFERENCES customer_onboardings (id)
);

CREATE INDEX idx_customer_onboardings_external_id ON customer_onboardings (external_id);
CREATE INDEX idx_customer_onboardings_cpf ON customer_onboardings (cpf);
CREATE INDEX idx_customer_onboardings_status ON customer_onboardings (status);

CREATE INDEX idx_customer_addresses_onboarding_id ON customer_addresses (onboarding_id);
CREATE INDEX idx_customer_addresses_zip_code ON customer_addresses (zip_code);

CREATE INDEX idx_customer_emails_onboarding_id ON customer_emails (onboarding_id);
CREATE INDEX idx_customer_phones_onboarding_id ON customer_phones (onboarding_id);
CREATE INDEX idx_customer_documents_onboarding_id ON customer_documents (onboarding_id);
CREATE INDEX idx_onboarding_audit_logs_onboarding_id ON onboarding_audit_logs (onboarding_id);

CREATE UNIQUE INDEX uidx_customer_addresses_one_primary_per_onboarding
    ON customer_addresses (onboarding_id)
    WHERE primary_address = TRUE;

CREATE UNIQUE INDEX uidx_customer_emails_one_primary_per_onboarding
    ON customer_emails (onboarding_id)
    WHERE primary_email = TRUE;

CREATE UNIQUE INDEX uidx_customer_phones_one_primary_per_onboarding
    ON customer_phones (onboarding_id)
    WHERE primary_phone = TRUE;

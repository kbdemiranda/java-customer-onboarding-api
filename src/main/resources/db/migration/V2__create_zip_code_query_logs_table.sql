CREATE TABLE zip_code_query_logs (
    id BIGSERIAL NOT NULL,
    external_id UUID NOT NULL,
    zip_code VARCHAR(8) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    request_timestamp TIMESTAMP NOT NULL,
    response_body JSONB,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_zip_code_query_logs PRIMARY KEY (id),
    CONSTRAINT uk_zip_code_query_logs_external_id UNIQUE (external_id)
);

CREATE INDEX idx_zip_code_query_logs_external_id ON zip_code_query_logs (external_id);
CREATE INDEX idx_zip_code_query_logs_zip_code ON zip_code_query_logs (zip_code);
CREATE INDEX idx_zip_code_query_logs_provider ON zip_code_query_logs (provider);
CREATE INDEX idx_zip_code_query_logs_status ON zip_code_query_logs (status);
CREATE INDEX idx_zip_code_query_logs_request_timestamp ON zip_code_query_logs (request_timestamp);

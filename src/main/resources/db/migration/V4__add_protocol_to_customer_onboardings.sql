ALTER TABLE customer_onboardings
    ADD COLUMN protocol VARCHAR(10);

UPDATE customer_onboardings
SET protocol = TO_CHAR(created_at, 'YYYYMM') || RIGHT(REGEXP_REPLACE(cpf, '\\D', '', 'g'), 4)
WHERE protocol IS NULL;

ALTER TABLE customer_onboardings
    ALTER COLUMN protocol SET NOT NULL;

CREATE INDEX idx_customer_onboardings_protocol ON customer_onboardings (protocol);

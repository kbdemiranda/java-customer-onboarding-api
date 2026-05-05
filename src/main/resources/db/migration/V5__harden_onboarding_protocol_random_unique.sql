ALTER TABLE customer_onboardings
    ALTER COLUMN protocol TYPE VARCHAR(14);

UPDATE customer_onboardings
SET protocol = LPAD(id::text, 14, '0')
WHERE protocol IS NULL
   OR protocol !~ '^\\d{14}$';

DROP INDEX IF EXISTS idx_customer_onboardings_protocol;

CREATE UNIQUE INDEX uidx_customer_onboardings_protocol ON customer_onboardings (protocol);

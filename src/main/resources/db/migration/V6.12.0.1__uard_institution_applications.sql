-- UaRD intake is separate from Dataverse accounts and published collections.
CREATE TABLE uard_institution_application (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fields TEXT NOT NULL,
    logo BYTEA NOT NULL CHECK (octet_length(logo) BETWEEN 1 AND 2097152),
    logo_type VARCHAR(32) NOT NULL CHECK (logo_type IN ('image/png', 'image/jpeg', 'image/gif')),
    consent_version VARCHAR(32) NOT NULL,
    notification_sent_at TIMESTAMPTZ,
    notification_attempts INTEGER NOT NULL DEFAULT 0,
    notification_next_attempt TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX uard_institution_application_pending
    ON uard_institution_application (notification_next_attempt)
    WHERE notification_sent_at IS NULL;

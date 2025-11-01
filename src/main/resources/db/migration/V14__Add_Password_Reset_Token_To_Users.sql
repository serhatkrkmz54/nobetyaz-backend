ALTER TABLE users
    ADD COLUMN password_reset_token VARCHAR(255) UNIQUE,
    ADD COLUMN token_expiry TIMESTAMP WITH TIME ZONE;

ALTER TABLE users
    DROP COLUMN IF EXISTS needs_password_change;
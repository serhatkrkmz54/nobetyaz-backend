ALTER TABLE users
    ADD COLUMN needs_password_change BOOLEAN NOT NULL DEFAULT false;
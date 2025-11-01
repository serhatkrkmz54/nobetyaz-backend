CREATE TABLE holidays
(
    id           UUID PRIMARY KEY,
    name         VARCHAR(255)             NOT NULL,
    holiday_date DATE                     NOT NULL UNIQUE,
    holiday_type VARCHAR(50)              NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE shift_templates
    DROP COLUMN IF EXISTS days_of_week;

ALTER TABLE shift_requirements
    ADD COLUMN apply_on VARCHAR(50) NOT NULL DEFAULT 'ALL_DAYS';


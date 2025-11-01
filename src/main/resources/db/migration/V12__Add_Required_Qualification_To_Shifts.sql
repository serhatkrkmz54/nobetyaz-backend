ALTER TABLE scheduled_shifts
    ADD COLUMN required_qualification_id UUID;

ALTER TABLE scheduled_shifts
    ADD CONSTRAINT fk_required_qualification
        FOREIGN KEY (required_qualification_id)
            REFERENCES qualifications (id);
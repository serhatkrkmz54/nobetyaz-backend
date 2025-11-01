CREATE TABLE member_preferences
(
    id                UUID PRIMARY KEY,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    member_id         UUID                     NOT NULL REFERENCES members (id),
    shift_template_id UUID                     NOT NULL REFERENCES shift_templates (id),
    day_of_week       INT                      NOT NULL,
    preference_score  INT                      NOT NULL,
    UNIQUE (member_id, shift_template_id, day_of_week)
);
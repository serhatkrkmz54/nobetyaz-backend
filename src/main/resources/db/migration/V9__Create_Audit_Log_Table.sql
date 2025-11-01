CREATE TABLE audit_logs
(
    id          UUID PRIMARY KEY,
    timestamp   TIMESTAMP WITH TIME ZONE NOT NULL,
    username    VARCHAR(100)             NOT NULL,
    action_type VARCHAR(50)              NOT NULL,
    description TEXT                     NOT NULL,
    entity_type VARCHAR(100),
    entity_id   VARCHAR(255)
);
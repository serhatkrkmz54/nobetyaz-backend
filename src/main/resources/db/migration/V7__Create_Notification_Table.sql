CREATE TABLE notifications
(
    id                UUID PRIMARY KEY,
    recipient_user_id UUID                     NOT NULL,
    message           TEXT                     NOT NULL,
    notification_type VARCHAR(50),
    status            VARCHAR(20)              NOT NULL,
    related_entity_id UUID,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_recipient_user FOREIGN KEY (recipient_user_id) REFERENCES users (id) ON DELETE CASCADE
);
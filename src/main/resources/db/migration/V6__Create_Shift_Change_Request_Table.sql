CREATE TABLE shift_change_requests
(
    id                   UUID PRIMARY KEY,

    -- Talebi başlatan personele ve nöbete ait bilgiler
    initiating_shift_id  UUID                     NOT NULL,
    initiating_member_id UUID                     NOT NULL,

    -- Talep edilen (hedef) personele ve nöbete ait bilgiler
    target_shift_id      UUID                     NOT NULL,
    target_member_id     UUID                     NOT NULL,

    -- Talep durumu ve detayları
    status               VARCHAR(50)              NOT NULL, -- PENDING_TARGET_APPROVAL, PENDING_MANAGER_APPROVAL, APPROVED, REJECTED, CANCELLED
    request_reason       TEXT,
    resolution_notes     TEXT,                              -- Yöneticinin onay/red notu

    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_initiating_shift FOREIGN KEY (initiating_shift_id) REFERENCES scheduled_shifts (id) ON DELETE CASCADE,
    CONSTRAINT fk_initiating_member FOREIGN KEY (initiating_member_id) REFERENCES members (id) ON DELETE CASCADE,
    CONSTRAINT fk_target_shift FOREIGN KEY (target_shift_id) REFERENCES scheduled_shifts (id) ON DELETE CASCADE,
    CONSTRAINT fk_target_member FOREIGN KEY (target_member_id) REFERENCES members (id) ON DELETE CASCADE
);
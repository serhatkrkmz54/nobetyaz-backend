CREATE TABLE shift_bids
(
    id         UUID PRIMARY KEY,
    shift_id   UUID                     NOT NULL,
    member_id  UUID                     NOT NULL,
    bid_status VARCHAR(50)              NOT NULL, -- 'ACTIVE', 'RETRACTED', 'AWARDED', 'LOST'
    notes      TEXT,                              -- Personelin talebiyle ilgili notu
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_shift FOREIGN KEY (shift_id) REFERENCES scheduled_shifts (id) ON DELETE CASCADE,
    CONSTRAINT fk_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE,
    UNIQUE (shift_id, member_id)                  -- Bir personel bir nöbete sadece bir kez talip olabilir
);

UPDATE scheduled_shifts
SET status = 'OPEN'
WHERE status = 'UNASSIGNED';
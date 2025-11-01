ALTER TABLE shift_bids ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE;

UPDATE shift_bids SET updated_at = created_at WHERE updated_at IS NULL;

ALTER TABLE shift_bids ALTER COLUMN updated_at SET NOT NULL;
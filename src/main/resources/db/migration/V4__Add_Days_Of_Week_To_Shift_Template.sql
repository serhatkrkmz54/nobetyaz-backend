ALTER TABLE shift_templates ADD COLUMN days_of_week VARCHAR(255);
COMMENT ON COLUMN shift_templates.days_of_week IS 'Nöbetin geçerli olduğu günler (örn: MONDAY,TUESDAY,WEDNESDAY)';
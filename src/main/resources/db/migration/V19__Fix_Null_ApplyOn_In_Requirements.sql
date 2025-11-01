UPDATE shift_requirements
SET apply_on = 'ALL_DAYS'
WHERE apply_on IS NULL;
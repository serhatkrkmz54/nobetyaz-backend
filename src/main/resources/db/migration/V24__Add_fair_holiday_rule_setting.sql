INSERT INTO rule_configurations (rule_key, rule_value, description, updated_at)
VALUES (
           'ENFORCE_FAIR_HOLIDAY_DISTRIBUTION',
           'true',
           'Adil Bayram Dağıtımı Kuralı. (Açık = true, Kapalı = false). Açık olduğunda, son 6 ayda bayramda çalışan personele tekrar bayram nöbeti atanmasını (otomatik veya manuel) engeller.',
           CURRENT_TIMESTAMP
       )
ON CONFLICT (rule_key) DO NOTHING;
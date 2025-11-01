INSERT INTO rule_configurations (
    rule_key,
    rule_value,
    description,
    updated_at
)
VALUES (
           'ALLOW_MEMBER_PREFERENCES',
           'false',
           'Personelin (Profilim sayfası üzerinden) nöbet tercihlerini (istiyorum/istemiyorum) belirlemesine izin ver (true/false).',
           CURRENT_TIMESTAMP
       )
ON CONFLICT (rule_key) DO NOTHING;
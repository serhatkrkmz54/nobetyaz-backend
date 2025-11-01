INSERT INTO rule_configurations (rule_key, rule_value, description,updated_at)
VALUES (
           'ALLOW_SHIFT_BIDDING',
           'true',
           'Nöbet Borsası Özelliği. (Açık = true, Kapalı = false). Kapatıldığında, kullanıcılar boş nöbetlere teklif veremez ve "Nöbet Borsası" sayfası gizlenir.',
           CURRENT_TIMESTAMP
       );
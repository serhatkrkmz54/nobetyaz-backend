package com.paketnobet.nobetyaz;

import com.paketnobet.nobetyaz.modules.organization.model.entity.RuleConfiguration;
import com.paketnobet.nobetyaz.modules.organization.repository.RuleConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RuleConfigurationRepository ruleConfigurationRepository;

    @Override
    public void run(String... args) throws Exception {
        if (ruleConfigurationRepository.findById("MAX_WEEKLY_HOURS").isEmpty()) {
            ruleConfigurationRepository.save(new RuleConfiguration(
                    "MAX_WEEKLY_HOURS", "40.0",
                    "Bir personelin bir hafta içinde çalışabileceği maksimum saat.", Instant.now()
            ));
        }

        if (ruleConfigurationRepository.findById("NIGHT_SHIFT_HOURS_THRESHOLD").isEmpty()) {
            ruleConfigurationRepository.save(new RuleConfiguration(
                    "NIGHT_SHIFT_HOURS_THRESHOLD", "16.0",
                    "Bir gece nöbetinin 'uzun' kabul edilmesi için gereken minimum saat.", Instant.now()
            ));
        }

        if (ruleConfigurationRepository.findById("MANDATORY_REST_HOURS_AFTER_NIGHT_SHIFT").isEmpty()) {
            ruleConfigurationRepository.save(new RuleConfiguration(
                    "MANDATORY_REST_HOURS_AFTER_NIGHT_SHIFT", "24.0",
                    "Uzun bir gece nöbetinden sonra personelin dinlenmesi gereken minimum saat.", Instant.now()
            ));
        }
    }
}

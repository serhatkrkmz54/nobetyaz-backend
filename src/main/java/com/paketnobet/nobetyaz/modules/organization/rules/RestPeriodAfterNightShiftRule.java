package com.paketnobet.nobetyaz.modules.organization.rules;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.RuleConfiguration;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.repository.RuleConfigurationRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ScheduledShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RestPeriodAfterNightShiftRule implements IScheduleValidationRule{

    private final RuleConfigurationRepository ruleConfigRepo;
    private final ScheduledShiftRepository scheduledShiftRepo;

    @Override
    public Optional<String> validate(Member member, ScheduledShift newShift) {
        Optional<RuleConfiguration> thresholdRuleOpt = ruleConfigRepo.findById("NIGHT_SHIFT_HOURS_THRESHOLD");
        Optional<RuleConfiguration> restHoursRuleOpt = ruleConfigRepo.findById("MANDATORY_REST_HOURS_AFTER_NIGHT_SHIFT");

        if (thresholdRuleOpt.isEmpty() || restHoursRuleOpt.isEmpty()) {
            return Optional.empty();
        }

        double nightShiftThreshold = Double.parseDouble(thresholdRuleOpt.get().getRuleValue());
        double mandatoryRestHours = Double.parseDouble(restHoursRuleOpt.get().getRuleValue());

        Optional<ScheduledShift> latestShiftOpt = scheduledShiftRepo
                .findFirstByMemberIdAndStartDatetimeBeforeOrderByStartDatetimeDesc(member.getId(), newShift.getStartDatetime());

        if (latestShiftOpt.isEmpty()) {
            return Optional.empty();
        }

        ScheduledShift latestShift = latestShiftOpt.get();

        boolean isLongNightShift = latestShift.getShiftTemplate().isNightShift() &&
                latestShift.getShiftTemplate().getDurationInHours() >= nightShiftThreshold;

        if (isLongNightShift) {
            Duration restDuration = Duration.between(latestShift.getEndDatetime(), newShift.getStartDatetime());
            double restHours = restDuration.toMinutes() / 60.0;

            if (restHours < mandatoryRestHours) {
                String message = String.format(
                        "Atama yapılamaz! Personel, %.1f saatlik gece nöbeti sonrası en az %.1f saat dinlenmelidir. Mevcut dinlenme süresi: %.1f saat.",
                        latestShift.getShiftTemplate().getDurationInHours(), mandatoryRestHours, restHours
                );
                return Optional.of(message);
            }
        }

        return Optional.empty();
    }
}

package com.paketnobet.nobetyaz.modules.organization.rules;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.RuleConfiguration;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.repository.RuleConfigurationRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ScheduledShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MaxWeeklyHoursRule implements IScheduleValidationRule{

    private final RuleConfigurationRepository ruleConfigurationRepository;
    private final ScheduledShiftRepository scheduledShiftRepository;

    @Override
    public Optional<String> validate(Member member, ScheduledShift newShift) {
        RuleConfiguration rule = ruleConfigurationRepository.findById("MAX_WEEKLY_HOURS").orElse(null);
        if (rule == null) return Optional.empty();

        double maxWeeklyHours = Double.parseDouble(rule.getRuleValue());
        LocalDate shiftDate = newShift.getShiftDate();
        LocalDate weekStart = shiftDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = shiftDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<ScheduledShift> existingShifts = scheduledShiftRepository
                .findByMemberIdAndShiftDateBetween(member.getId(), weekStart, weekEnd);

        double currentHours = existingShifts.stream()
                .mapToDouble(shift -> shift.getShiftTemplate().getDurationInHours())
                .sum();

        double newTotalHours = currentHours + newShift.getShiftTemplate().getDurationInHours();

        if (newTotalHours > maxWeeklyHours) {
            String message = String.format(
                    "Atama yapılamaz! Bu nöbet ile personelin haftalık çalışma süresi (%.2f saat) maksimum limiti (%.2f saat) aşıyor.",
                    newTotalHours, maxWeeklyHours
            );
            return Optional.of(message);
        }

        return Optional.empty();
    }
}

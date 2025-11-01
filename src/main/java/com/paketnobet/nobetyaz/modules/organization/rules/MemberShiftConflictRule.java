package com.paketnobet.nobetyaz.modules.organization.rules;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftStatus;
import com.paketnobet.nobetyaz.modules.organization.repository.ScheduledShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberShiftConflictRule implements IScheduleValidationRule{
    private final ScheduledShiftRepository scheduledShiftRepository;
    @Override
    public Optional<String> validate(Member member, ScheduledShift newShift) {
        Instant newShiftStart = newShift.getStartDatetime();
        Instant newShiftEnd = newShift.getEndDatetime();

        List<ScheduledShift> existingShiftsOnDate = scheduledShiftRepository
                .findByMemberIdAndShiftDateAndStatus(member.getId(), newShift.getShiftDate(), EShiftStatus.CONFIRMED);

        for (ScheduledShift existingShift : existingShiftsOnDate) {
            if (existingShift.getId().equals(newShift.getId())) {
                continue;
            }

            Instant existingStart = existingShift.getStartDatetime();
            Instant existingEnd = existingShift.getEndDatetime();
            if (newShiftStart.isBefore(existingEnd) && newShiftEnd.isAfter(existingStart)) {
                String message = String.format(
                        "Atama yapılamaz! Personel zaten %s tarihinde %s - %s saatleri arasında başka bir nöbete (%s) atanmış.",
                        newShift.getShiftDate(),
                        existingShift.getShiftTemplate().getStartTime(),
                        existingShift.getShiftTemplate().getEndTime(),
                        existingShift.getLocation().getName()
                );
                return Optional.of(message);
            }
        }

        return Optional.empty();
    }
}

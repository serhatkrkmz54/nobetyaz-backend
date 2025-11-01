package com.paketnobet.nobetyaz.modules.organization.rules;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.repository.LeaveRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberOnLeaveRule implements IScheduleValidationRule{

    private final LeaveRecordRepository leaveRecordRepository;

    @Override
    public Optional<String> validate(Member member, ScheduledShift newShift) {
        return leaveRecordRepository.findApprovedLeaveForMemberOnDate(member.getId(), newShift.getShiftDate())
                .map(leaveRecord -> String.format(
                        "Atama yapılamaz! Personel, '%s' nedeniyle bu tarihte izinlidir.",
                        leaveRecord.getLeaveType()
                ));
    }
}

package com.paketnobet.nobetyaz.modules.organization.rules;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;

import java.util.Optional;

public interface IScheduleValidationRule {

    Optional<String> validate(Member member, ScheduledShift newShift);

}

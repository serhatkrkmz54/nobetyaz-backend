package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.RuleViolationException;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.repository.HolidayRepository;
import com.paketnobet.nobetyaz.modules.organization.rules.IScheduleValidationRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleValidatorService {

    private final List<IScheduleValidationRule> validationRules;

    public void validateAssignment(Member member, ScheduledShift newShift) {
        List<String> violations = validationRules.stream()
                .map(rule -> rule.validate(member, newShift))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (!violations.isEmpty()) {
            throw new RuleViolationException(String.join("\n", violations));
        }
    }
}

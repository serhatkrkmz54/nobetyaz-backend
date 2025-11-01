package com.paketnobet.nobetyaz.modules.organization.rules;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Holiday;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.RuleConfiguration;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EDayType;
import com.paketnobet.nobetyaz.modules.organization.repository.HolidayRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.RuleConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FairHolidayDistributionRule implements IScheduleValidationRule {

    private final HolidayRepository holidayRepository;
    private final RuleConfigurationRepository ruleConfigurationRepository;
    private static final int HOLIDAY_MEMORY_MONTHS = 6;
    private static final String RULE_KEY = "ENFORCE_FAIR_HOLIDAY_DISTRIBUTION";

    @Override
    public Optional<String> validate(Member member, ScheduledShift newShift) {
        Optional<RuleConfiguration> ruleOpt = ruleConfigurationRepository.findById(RULE_KEY);
        if (ruleOpt.isEmpty() || !ruleOpt.get().getRuleValue().equalsIgnoreCase("true")) {
            log.trace("Adil Bayram Kuralı kapalı (Ayarlarda 'false' veya bulunamadı). Kural atlanıyor.");
            return Optional.empty();
        }
        LocalDate shiftDate = newShift.getShiftDate();

        Optional<Holiday> holidayOpt = holidayRepository.findByHolidayDate(shiftDate);

        if (holidayOpt.isPresent()) {
            EDayType type = holidayOpt.get().getHolidayType();

            if (type == EDayType.RELIGIOUS_HOLIDAY || type == EDayType.PUBLIC_HOLIDAY) {
                log.debug("Adil Bayram Kuralı: Atama bir bayram gününe ({}) denk geliyor.", shiftDate);

                LocalDate lastHoliday = member.getLastWorkedReligiousHolidayDate();

                if (lastHoliday != null) {

                    LocalDate cutoffDate = YearMonth.now().atDay(1).minusMonths(HOLIDAY_MEMORY_MONTHS);

                    if (lastHoliday.isAfter(cutoffDate)) {
                        log.warn("Kural İhlali (Adil Bayram): Personel {} en son {}'de bayram nöbeti tutmuş.", member.getId(), lastHoliday);

                        String message = String.format(
                                "Atama yapılamaz: %s %s, (son %d ay içinde) zaten bir bayram nöbeti (%s) tutmuş.",
                                member.getFirstName(), member.getLastName(),
                                HOLIDAY_MEMORY_MONTHS, lastHoliday
                        );
                        return Optional.of(message);
                    }
                }
            }
        }

        // 5. Kural ihlali yok, boş döndür
        return Optional.empty();
    }
}

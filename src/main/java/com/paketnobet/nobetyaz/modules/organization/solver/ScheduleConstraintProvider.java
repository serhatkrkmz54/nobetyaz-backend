package com.paketnobet.nobetyaz.modules.organization.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import com.paketnobet.nobetyaz.modules.organization.model.entity.*;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EDayType;
import com.paketnobet.nobetyaz.modules.organization.model.enums.ELeaveStatus;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class ScheduleConstraintProvider implements ConstraintProvider {


    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                requiredQualification(factory),
                memberOnLeave(factory),
                distributeHolidaysFairly(factory),
                distributeWeekendsFairly(factory),
                balanceMonthlyHours(factory),
                respectMemberPreferences(factory),
                fairHolidayDistribution(factory)
        };
    }

    private Constraint respectMemberPreferences(ConstraintFactory factory) {
        return factory
                .forEach(ScheduledShift.class)
                .filter(shift -> shift.getMember() != null)

                .join(factory.forEach(MemberPreference.class),
                        Joiners.equal(ScheduledShift::getMember, MemberPreference::getMember),
                        Joiners.equal(ScheduledShift::getShiftTemplate, MemberPreference::getShiftTemplate),
                        Joiners.equal(
                                (shift) -> shift.getShiftDate().getDayOfWeek().getValue(),
                                MemberPreference::getDayOfWeek
                        )
                )
                .reward(HardSoftScore.ONE_SOFT,
                        (shift, preference) -> preference.getPreferenceScore()
                )
                .asConstraint("Personel tercihleri");
    }

    private Constraint requiredQualification(ConstraintFactory factory) {
        return factory.forEach(ScheduledShift.class)
                .filter(shift -> shift.getRequiredQualification() != null && shift.getMember() != null)
                .filter(shift -> !shift.getMember().getQualifications().contains(shift.getRequiredQualification()))
                .penalize(HardSoftScore.ONE_HARD,
                        (shift) -> 100)
                .asConstraint("Gerekli yetkinlik sağlanmadı");
    }

    private Constraint memberOnLeave(ConstraintFactory factory) {
        return factory.forEach(ScheduledShift.class)
                .filter(shift -> shift.getMember() != null)
                .join(
                        factory.forEach(LeaveRecord.class)
                                .filter(leave -> leave.getStatus() == ELeaveStatus.APPROVED),
                        Joiners.equal(ScheduledShift::getMember, LeaveRecord::getMember),
                        Joiners.greaterThanOrEqual(ScheduledShift::getShiftDate, LeaveRecord::getStartDate),
                        Joiners.lessThanOrEqual(ScheduledShift::getShiftDate, LeaveRecord::getEndDate)
                )
                .penalize(HardSoftScore.ONE_HARD,
                        (shift, leave) -> 100)
                .asConstraint("Personel o gün izinli");
    }

    private Constraint distributeHolidaysFairly(ConstraintFactory factory) {
        return factory
                .forEach(ScheduledShift.class)
                .filter(shift -> shift.getMember() != null &&
                        shift.getApplyOn() == EDayType.RELIGIOUS_HOLIDAY)
                .join(factory.forEach(ScheduledShift.class)
                                .filter(historicalShift -> historicalShift.getMember() != null &&
                                        historicalShift.getApplyOn() == EDayType.RELIGIOUS_HOLIDAY),
                        Joiners.equal(ScheduledShift::getMember, ScheduledShift::getMember)
                )
                .penalize(HardSoftScore.ONE_SOFT,
                        (planShift, historicalShift) -> 100)
                .asConstraint("Arka arkaya bayram nöbeti");
    }

    private Constraint distributeWeekendsFairly(ConstraintFactory factory) {
        return factory
                .forEach(ScheduledShift.class)
                .filter(shift -> shift.getMember() != null &&
                        shift.getApplyOn() == EDayType.WEEKEND)
                .join(factory.forEach(ScheduledShift.class)
                                .filter(historicalShift -> historicalShift.getMember() != null &&
                                        historicalShift.getApplyOn() == EDayType.WEEKEND),
                        Joiners.equal(ScheduledShift::getMember, ScheduledShift::getMember)
                )
                .filter((planShift, historicalShift) -> {
                    long daysBetween = ChronoUnit.DAYS.between(historicalShift.getShiftDate(), planShift.getShiftDate());
                    return daysBetween > 0 && daysBetween <= 8;
                })
                .penalize(HardSoftScore.ONE_SOFT,
                        (planShift, historicalShift) -> 20)
                .asConstraint("Arka arkaya hafta sonu nöbeti");
    }

    private Constraint balanceMonthlyHours(ConstraintFactory factory) {
        return factory.forEach(ScheduledShift.class)
                .filter(shift -> shift.getMember() != null)
                .groupBy(
                        ScheduledShift::getMember,
                        ConstraintCollectors.<ScheduledShift>sum(shift ->
                                (int) shift.getShiftTemplate().getDurationInHours()
                        )
                )
                .penalize(HardSoftScore.ONE_SOFT,
                        (member, totalHours) -> totalHours * totalHours)
                .asConstraint("Aylık saat dengesizliği");
    }

    Constraint fairHolidayDistribution(ConstraintFactory factory) {
        return factory.forEach(Holiday.class)
                .filter(holiday ->
                        holiday.getHolidayType() == EDayType.RELIGIOUS_HOLIDAY ||
                                holiday.getHolidayType() == EDayType.PUBLIC_HOLIDAY
                )
                .join(ScheduledShift.class, Joiners.equal(Holiday::getHolidayDate, ScheduledShift::getShiftDate))
                .join(RuleConfiguration.class,
                        Joiners.equal((holiday, shift) -> "ENFORCE_FAIR_HOLIDAY_DISTRIBUTION", RuleConfiguration::getRuleKey))
                .filter((holiday, shift, rule) -> rule.getRuleValue().equalsIgnoreCase("true"))
                .map((holiday, shift, rule) -> shift.getMember())
                .filter(Objects::nonNull)
                .filter(member ->
                        member.getLastWorkedReligiousHolidayDate() != null &&
                                member.getLastWorkedReligiousHolidayDate().isAfter(YearMonth.now().atDay(1).minusMonths(6))
                )
                .penalize(HardSoftScore.ofSoft(100), (member) -> 100)
                .asConstraint("Adil Bayram Dağıtımı");
    }
}

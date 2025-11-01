package com.paketnobet.nobetyaz.modules.organization.model.entity;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
public class ShiftSchedulePlan {

    @ValueRangeProvider(id = "memberList")
    @ProblemFactCollectionProperty
    private List<Member> memberList;

    @ProblemFactCollectionProperty
    private List<LeaveRecord> leaveRecordList;

    @ProblemFactCollectionProperty
    private List<RuleConfiguration> ruleConfigurationList;

    @ProblemFactCollectionProperty
    private List<ScheduledShift> historicalShifts;


    @ProblemFactCollectionProperty
    private List<MemberPreference> preferenceList;

    @ProblemFactCollectionProperty
    private List<Holiday> holidayList;

    @PlanningEntityCollectionProperty
    private List<ScheduledShift> scheduledShiftList;

    @PlanningScore
    private HardSoftScore score;

    public ShiftSchedulePlan(List<Member> memberList, List<LeaveRecord> leaveRecordList,
                             List<RuleConfiguration> ruleConfigurationList,
                             List<ScheduledShift> scheduledShiftList,
                             List<ScheduledShift> historicalShifts,
                             List<MemberPreference> preferenceList,
                             List<Holiday> allHolidays) {
        this.memberList = memberList;
        this.leaveRecordList = leaveRecordList;
        this.ruleConfigurationList = ruleConfigurationList;
        this.scheduledShiftList = scheduledShiftList;
        this.historicalShifts = historicalShifts;
        this.preferenceList = preferenceList;
        this.holidayList = allHolidays;
    }
}

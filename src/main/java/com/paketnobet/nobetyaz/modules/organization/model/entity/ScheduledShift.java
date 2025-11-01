package com.paketnobet.nobetyaz.modules.organization.model.entity;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.paketnobet.nobetyaz.core.model.entity.BaseEntity;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EDayType;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"member", "requiredQualification", "location", "shiftTemplate"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "scheduled_shifts")
@PlanningEntity
public class ScheduledShift extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @PlanningVariable(valueRangeProviderRefs = "memberList")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Column(name = "start_datetime", nullable = false)
    private Instant startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private Instant endDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EShiftStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_qualification_id")
    private Qualification requiredQualification;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_on")
    private EDayType applyOn;
}

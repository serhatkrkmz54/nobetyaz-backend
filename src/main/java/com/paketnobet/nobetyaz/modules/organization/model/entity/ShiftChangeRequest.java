package com.paketnobet.nobetyaz.modules.organization.model.entity;

import com.paketnobet.nobetyaz.core.model.entity.BaseEntity;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftChangeRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shift_change_requests")
public class ShiftChangeRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne @JoinColumn(name = "initiating_shift_id")
    private ScheduledShift initiatingShift;
    @ManyToOne @JoinColumn(name = "initiating_member_id")
    private Member initiatingMember;
    @ManyToOne @JoinColumn(name = "target_shift_id")
    private ScheduledShift targetShift;
    @ManyToOne @JoinColumn(name = "target_member_id")
    private Member targetMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EShiftChangeRequestStatus status;

    private String requestReason;
    private String resolutionNotes;
}

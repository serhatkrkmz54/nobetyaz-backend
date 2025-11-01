package com.paketnobet.nobetyaz.modules.organization.model.entity;

import com.paketnobet.nobetyaz.core.model.entity.BaseEntity;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftBidStatus;
import jakarta.persistence.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"shift","member"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shift_bids")
public class ShiftBid extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "shift_id")
    private ScheduledShift shift;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EShiftBidStatus bidStatus;

    private String notes;


}

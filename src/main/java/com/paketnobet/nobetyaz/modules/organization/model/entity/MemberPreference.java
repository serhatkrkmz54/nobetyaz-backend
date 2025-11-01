package com.paketnobet.nobetyaz.modules.organization.model.entity;

import com.paketnobet.nobetyaz.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true, exclude = {"member", "shiftTemplate"})
@EqualsAndHashCode(callSuper = true, exclude = {"member", "shiftTemplate"})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "member_preferences", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "shift_template_id", "day_of_week"})
})
public class MemberPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "preference_score", nullable = false)
    private int preferenceScore;

}

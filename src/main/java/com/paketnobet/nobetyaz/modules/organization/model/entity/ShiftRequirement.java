package com.paketnobet.nobetyaz.modules.organization.model.entity;

import com.paketnobet.nobetyaz.core.model.entity.BaseEntity;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EDayType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true, exclude = {"location", "shiftTemplate", "qualification"})
@EqualsAndHashCode(callSuper = true, exclude = {"location", "shiftTemplate", "qualification"})
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shift_requirements")
public class ShiftRequirement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qualification_id")
    private Qualification qualification;

    @Column(name = "required_member_count", nullable = false)
    private int requiredMemberCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "apply_on", nullable = false)
    @Builder.Default
    private EDayType applyOn = EDayType.ALL_DAYS;

}

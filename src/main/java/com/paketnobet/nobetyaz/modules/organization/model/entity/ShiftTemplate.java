package com.paketnobet.nobetyaz.modules.organization.model.entity;

import com.paketnobet.nobetyaz.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalTime;
import java.util.UUID;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shift_templates")
public class ShiftTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_in_hours", nullable = false)
    private double durationInHours;

    @Column(name = "is_night_shift")
    private boolean isNightShift = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

}

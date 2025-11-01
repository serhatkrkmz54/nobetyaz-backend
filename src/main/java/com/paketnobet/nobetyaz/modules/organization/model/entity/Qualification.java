package com.paketnobet.nobetyaz.modules.organization.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paketnobet.nobetyaz.core.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"members"})
@ToString(callSuper = true, exclude = {"members"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "qualifications")
public class Qualification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @ManyToMany(mappedBy = "qualifications", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Member> members = new HashSet<>();
}

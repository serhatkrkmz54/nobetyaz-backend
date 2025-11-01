package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, UUID> {
    List<ShiftTemplate> findAllByIsActive(boolean isActive);
}

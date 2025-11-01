package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    List<Location> findByIsActiveTrueOrderByNameAsc();
    List<Location> findAllByOrderByNameAsc();
}

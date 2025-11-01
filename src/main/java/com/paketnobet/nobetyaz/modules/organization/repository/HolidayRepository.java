package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    List<Holiday> findByHolidayDateBetween(LocalDate startDate, LocalDate endDate);
    List<Holiday> findAllByOrderByHolidayDateAsc();
    Optional<Holiday> findByHolidayDate(LocalDate date);
}

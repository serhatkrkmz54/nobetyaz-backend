package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ScheduledShiftRepository extends JpaRepository<ScheduledShift, UUID> {

    List<ScheduledShift> findByShiftDateBetween(LocalDate startDate, LocalDate endDate);
    List<ScheduledShift> findByMemberIdAndShiftDateBetween(UUID memberId, LocalDate startDate, LocalDate endDate);
    Optional<ScheduledShift> findFirstByMemberIdAndStartDatetimeBeforeOrderByStartDatetimeDesc(UUID memberId, Instant startDatetime);
    List<ScheduledShift> findByMemberIdAndShiftDateAndStatus(UUID memberId, LocalDate shiftDate, EShiftStatus status);
    List<ScheduledShift> findByLocationIdAndShiftTemplateIdAndShiftDateAndRequiredQualificationId(
            UUID locationId,
            UUID shiftTemplateId,
            LocalDate shiftDate,
            UUID qualificationId
    );

    List<ScheduledShift> findByLocationIdAndShiftTemplateIdAndShiftDateAndRequiredQualificationIdIsNull(
            UUID locationId,
            UUID shiftTemplateId,
            LocalDate shiftDate
    );

    @Query("SELECT ss FROM ScheduledShift ss " +
            "LEFT JOIN FETCH ss.requiredQualification " +
            "LEFT JOIN FETCH ss.shiftTemplate " +
            "WHERE ss.shiftDate BETWEEN :startDate AND :endDate AND ss.member IS NULL")
    List<ScheduledShift> findEmptyShiftsForSolver(LocalDate startDate, LocalDate endDate);

    @Query("SELECT ss FROM ScheduledShift ss " +
            "LEFT JOIN FETCH ss.member " +
            "LEFT JOIN FETCH ss.shiftTemplate " +
            "LEFT JOIN FETCH ss.location " +
            "WHERE ss.shiftDate BETWEEN :startDate AND :endDate AND ss.member IS NOT NULL")
    List<ScheduledShift> findHistoricalShiftsForSolver(LocalDate startDate, LocalDate endDate);

    List<ScheduledShift> findByMemberIdAndShiftDateBetweenAndStatusNotIn(
            UUID memberId,
            LocalDate startDate,
            LocalDate endDate,
            Set<EShiftStatus> statuses
    );

    List<ScheduledShift> findByStatus(EShiftStatus status);

    List<ScheduledShift> findByShiftDateBetweenOrderByShiftDateAscShiftTemplateStartTimeAsc(LocalDate startDate, LocalDate endDate);

    long countByStatusAndShiftDateBetween(EShiftStatus status, LocalDate shiftDateAfter, LocalDate shiftDateAfter1);
}
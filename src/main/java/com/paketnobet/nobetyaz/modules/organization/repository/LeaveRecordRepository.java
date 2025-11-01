package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.LeaveRecord;
import com.paketnobet.nobetyaz.modules.organization.model.enums.ELeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveRecordRepository extends JpaRepository<LeaveRecord, UUID> {
    @Query("SELECT lr FROM LeaveRecord lr WHERE lr.member.id = :memberId AND lr.status = 'APPROVED' AND :date BETWEEN lr.startDate AND lr.endDate")
    Optional<LeaveRecord> findApprovedLeaveForMemberOnDate(@Param("memberId") UUID memberId, @Param("date") LocalDate date);

    @Query("SELECT lr FROM LeaveRecord lr WHERE lr.member.id = :memberId AND lr.status = 'APPROVED' AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRecord> findApprovedLeavesForMemberInPeriod(@Param("memberId") UUID memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<LeaveRecord> findByMemberIdOrderByStartDateDesc(UUID memberId);

    List<LeaveRecord> findByStatusOrderByStartDateAsc(ELeaveStatus status);

    @Query("SELECT lr FROM LeaveRecord lr JOIN FETCH lr.member " +
            "WHERE lr.status = :status ORDER BY lr.startDate ASC")
    List<LeaveRecord> findByStatusWithMember(ELeaveStatus status);

    @Query("SELECT lr FROM LeaveRecord lr JOIN FETCH lr.member ORDER BY lr.startDate DESC")
    List<LeaveRecord> findAllWithMemberOrderByStartDateDesc();

    @Query("SELECT lr FROM LeaveRecord lr JOIN FETCH lr.member " +
            "WHERE lr.status = :status AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRecord> findApprovedLeavesByDateRangeWithMember(
            LocalDate startDate,
            LocalDate endDate,
            ELeaveStatus status
    );

    Optional<LeaveRecord> findByIdAndMemberId(UUID leaveId, UUID memberId);
    @Query("SELECT lr FROM LeaveRecord lr JOIN FETCH lr.member m WHERE m.id = :memberId ORDER BY lr.startDate DESC")
    List<LeaveRecord> findByMemberIdWithMemberOrderByStartDateDesc(UUID memberId);


    long countByStatus(ELeaveStatus status);
}

package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftBid;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftBidStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShiftBidRepository extends JpaRepository <ShiftBid, UUID> {
    List<ShiftBid> findByShiftId(UUID shiftId);
    boolean existsByShiftIdAndMemberId(UUID shiftId, UUID memberId);
    List<ShiftBid> findByMemberIdOrderByCreatedAtDesc(UUID memberId);
    List<ShiftBid> findByShiftIdAndBidStatus(UUID shiftId, EShiftBidStatus status);

    long countByBidStatus(EShiftBidStatus eShiftBidStatus);
}

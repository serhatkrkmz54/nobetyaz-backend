package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftChangeRequest;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftChangeRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftChangeRequestRepository extends JpaRepository<ShiftChangeRequest, UUID> {
    List<ShiftChangeRequest> findByInitiatingMemberIdOrTargetMemberId(UUID initiatingMemberId, UUID targetMemberId);

    long countByStatus(EShiftChangeRequestStatus status);
}

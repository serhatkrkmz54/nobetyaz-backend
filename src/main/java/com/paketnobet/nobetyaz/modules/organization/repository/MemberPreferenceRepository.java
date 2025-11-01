package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.MemberPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MemberPreferenceRepository extends JpaRepository<MemberPreference, UUID> {

    @Query("SELECT mp FROM MemberPreference mp " +
            "LEFT JOIN FETCH mp.shiftTemplate " +
            "WHERE mp.member.id = :memberId " +
            "ORDER BY mp.dayOfWeek ASC, mp.shiftTemplate.startTime ASC")
    List<MemberPreference> findByMemberIdWithDetails(UUID memberId);

    @Query("SELECT mp FROM MemberPreference mp " +
            "LEFT JOIN FETCH mp.member " +
            "LEFT JOIN FETCH mp.shiftTemplate")
    List<MemberPreference> findAllWithDetails();

}

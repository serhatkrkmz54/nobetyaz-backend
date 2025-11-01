package com.paketnobet.nobetyaz.modules.organization.repository;

import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByUserId(UUID userId);
    boolean existsByEmployeeId(String employeeId);
    @Query("SELECT m FROM Member m " +
            "LEFT JOIN FETCH m.qualifications " +
            "LEFT JOIN FETCH m.user " +
            "WHERE m.isActive = :isActive")
    List<Member> findAllByIsActiveWithQualificationsAndUser(boolean isActive);

    long countByIsActive(boolean isActive);
}

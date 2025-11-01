package com.paketnobet.nobetyaz.core.repository;

import com.paketnobet.nobetyaz.core.model.entity.Role;
import com.paketnobet.nobetyaz.core.model.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}

package com.paketnobet.nobetyaz.core.repository;

import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.core.model.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    List<User> findByRoles_Name(ERole roleName);

    Optional<User> findByPasswordResetToken(String token);
}

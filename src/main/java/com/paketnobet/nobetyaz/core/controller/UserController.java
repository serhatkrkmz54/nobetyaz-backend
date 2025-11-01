package com.paketnobet.nobetyaz.core.controller;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.core.repository.UserRepository;
import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.auth.service.AuthService;
import com.paketnobet.nobetyaz.modules.dto.JwtResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JwtResponse> getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(authService.getUserProfile(userDetails));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JwtResponse> updateUserProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        JwtResponse updatedUser = authService.updateUserProfile(userDetails.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

}

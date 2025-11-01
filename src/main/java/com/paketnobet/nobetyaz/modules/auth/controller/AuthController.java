package com.paketnobet.nobetyaz.modules.auth.controller;

import com.paketnobet.nobetyaz.core.security.jwt.services.SecurityService;
import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.auth.service.AuthService;
import com.paketnobet.nobetyaz.modules.dto.ChangePasswordRequest;
import com.paketnobet.nobetyaz.modules.dto.JwtResponse;
import com.paketnobet.nobetyaz.modules.dto.LoginRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ActivateAccountRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.SetPasswordRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.UserFromTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityService securityService;
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/activate-account")
    public ResponseEntity<?> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
        authService.activateAccount(request);
        return ResponseEntity.ok("Hesabınız başarıyla aktifleştirildi. Şimdi giriş yapabilirsiniz.");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(currentUser, request);
        return ResponseEntity.ok("Şifre başarıyla güncellendi.");
    }

    @GetMapping("/feature-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> getFeatureStatus(@RequestParam String key) {
        return ResponseEntity.ok(securityService.isFeatureEnabled(key));
    }
}

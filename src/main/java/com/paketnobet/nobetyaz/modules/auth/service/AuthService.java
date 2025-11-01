package com.paketnobet.nobetyaz.modules.auth.service;

import com.paketnobet.nobetyaz.core.exception.DuplicateResourceException;
import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.core.exception.RuleViolationException;
import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.core.repository.UserRepository;
import com.paketnobet.nobetyaz.core.security.jwt.JwtUtils;
import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.dto.ChangePasswordRequest;
import com.paketnobet.nobetyaz.modules.dto.JwtResponse;
import com.paketnobet.nobetyaz.modules.dto.LoginRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ActivateAccountRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.SetPasswordRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.UserFromTokenResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        boolean showOnboarding = false;
        boolean isAdmin = roles.contains("ROLE_ADMIN");

        if (isAdmin) {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userDetails.getUsername()));

            if (!user.isHasCompletedOnboarding()) {
                showOnboarding = true;
                user.setHasCompletedOnboarding(true);
                userRepository.save(user);
            }
        }

        return new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFirstName(),
                userDetails.getLastName(),
                userDetails.getEmail(),
                userDetails.getPhoneNumber(),
                roles,
                showOnboarding
        );
    }

        @Transactional(readOnly = true)
        public JwtResponse getUserProfile(UserDetailsImpl userDetails) {
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            boolean hasCompletedOnboarding = userRepository.findById(userDetails.getId())
                    .map(User::isHasCompletedOnboarding)
                    .orElse(true);

            boolean showOnboarding = false;
            if (roles.contains("ROLE_ADMIN") && !hasCompletedOnboarding) {
                showOnboarding = true;
            }

            return new JwtResponse(
                    null,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getEmail(),
                    userDetails.getPhoneNumber(),
                    roles,
                    showOnboarding
            );
        }

    @Transactional
    public JwtResponse updateUserProfile(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı"));

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Bu email adresi zaten başka bir kullanıcı tarafından kullanılıyor.");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());

        User updatedUser = userRepository.save(user);

        List<String> roles = updatedUser.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new JwtResponse(
                null,
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getEmail(),
                updatedUser.getPhoneNumber(),
                roles,
                updatedUser.isHasCompletedOnboarding()
        );
    }

    @Transactional
    public void changePassword(UserDetailsImpl currentUser, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuleViolationException("Mevcut şifreniz yanlış.");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new RuleViolationException("Yeni şifreniz mevcut şifrenizle aynı olamaz.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);
    }

    @Transactional
    public void activateAccount(ActivateAccountRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı adı veya PIN hatalı."));

        if (user.getPasswordResetToken() == null) {
            throw new RuleViolationException("Bu PIN zaten kullanılmış veya geçersiz.");
        }

        if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(Instant.now())) {
            userRepository.delete(user);
            throw new RuleViolationException("Aktivasyon kodunun süresi dolmuş. Lütfen yöneticinizden yeni bir hesap isteyin.");
        }

        if (!user.getPasswordResetToken().equals(request.pin())) {
            throw new RuleViolationException("Kullanıcı adı veya PIN hatalı.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetToken(null);
        user.setTokenExpiry(null);
        user.setActive(true);

        userRepository.save(user);
        log.info("Hesap başarıyla aktifleştirildi: {}", user.getUsername());
    }
}

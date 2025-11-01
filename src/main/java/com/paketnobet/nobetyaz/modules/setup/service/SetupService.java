package com.paketnobet.nobetyaz.modules.setup.service;

import com.paketnobet.nobetyaz.core.model.entity.Role;
import com.paketnobet.nobetyaz.core.model.entity.SystemSetting;
import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.core.model.enums.ERole;
import com.paketnobet.nobetyaz.core.repository.RoleRepository;
import com.paketnobet.nobetyaz.core.repository.SystemSettingRepository;
import com.paketnobet.nobetyaz.core.repository.UserRepository;
import com.paketnobet.nobetyaz.modules.dto.SetupRequest;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SetupService {

    private final SystemSettingRepository systemSettingRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    public static final String SETUP_COMPLETE_KEY = "SETUP_COMPLETE";
    public static final String INDUSTRY_PROFILE_KEY = "INDUSTRY_PROFILE";

    public boolean isSetupComplete() {
        return systemSettingRepository.findById(SETUP_COMPLETE_KEY).isPresent();
    }

    @Transactional
    public void performSetup(SetupRequest request) {
        if (isSetupComplete()) {
            throw new IllegalStateException("Kurulum zaten tamamlanmış. Bu işlem tekrar yapılamaz.");
        }

        if (roleRepository.count() == 0) {
            Set<Role> roles = Arrays.stream(ERole.values())
                    .map(eRole -> new Role(null, eRole))
                    .collect(Collectors.toSet());
            roleRepository.saveAll(roles);
        }

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Hata: ROLE_ADMIN bulunamadı."));

        User adminUser = User.builder()
                .username(request.getAdminUsername())
                .email(request.getAdminEmail())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .phoneNumber(request.getAdminPhoneNumber())
                .isActive(true)
                .roles(Set.of(adminRole))
                .build();
        userRepository.save(adminUser);

        Member adminMember = Member.builder()
                .user(adminUser)
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .phoneNumber(request.getAdminPhoneNumber())
                .isActive(true)
                .build();
        memberRepository.save(adminMember);

        SystemSetting profileSetting = new SystemSetting(INDUSTRY_PROFILE_KEY, request.getIndustryProfile(), Instant.now());
        SystemSetting setupCompleteSetting = new SystemSetting(SETUP_COMPLETE_KEY, "true", Instant.now());
        systemSettingRepository.saveAll(Arrays.asList(profileSetting, setupCompleteSetting));
    }
}

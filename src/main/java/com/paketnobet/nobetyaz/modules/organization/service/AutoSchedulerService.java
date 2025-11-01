package com.paketnobet.nobetyaz.modules.organization.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.paketnobet.nobetyaz.core.exception.RuleViolationException;
import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.core.model.enums.ERole;
import com.paketnobet.nobetyaz.core.repository.UserRepository;
import com.paketnobet.nobetyaz.modules.organization.model.entity.*;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EDayType;
import com.paketnobet.nobetyaz.modules.organization.model.enums.ELeaveStatus;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftStatus;
import com.paketnobet.nobetyaz.modules.organization.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoSchedulerService {
    private final SolverManager<ShiftSchedulePlan, UUID> solverManager;

    private final ScheduledShiftRepository scheduledShiftRepository;
    private final MemberRepository memberRepository;
    private final LeaveRecordRepository leaveRecordRepository;
    private final RuleConfigurationRepository ruleConfigRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final MemberPreferenceRepository preferenceRepository;
    private final HolidayRepository holidayRepository;
    private static final int HISTORY_LOOKBACK_MONTHS = 2;

    @Transactional
    public UUID solve(LocalDate startDate, LocalDate endDate) {
        log.info("Otomatik Planlama başlatılıyor: {} - {}", startDate, endDate);

        List<ScheduledShift> shiftsToPlan = scheduledShiftRepository.findEmptyShiftsForSolver(startDate, endDate);
        log.info("{} adet boş nöbet (ve detayları) planlanmak üzere bulundu.", shiftsToPlan.size());
        if (shiftsToPlan.isEmpty()) {
            throw new RuleViolationException("Planlanacak boş nöbet bulunamadı. Lütfen önce 'Boş Nöbetleri Oluştur'u çalıştırın.");
        }

        List<Member> allMembers = memberRepository.findAllByIsActiveWithQualificationsAndUser(true);
        log.info("{} adet aktif personel (ve tüm detayları) bulundu.", allMembers.size());

        List<LeaveRecord> allLeaves = leaveRecordRepository.findByStatusWithMember(ELeaveStatus.APPROVED);
        log.info("{} adet onaylı izin kaydı (personel bilgisiyle) bulundu.", allLeaves.size());

        List<RuleConfiguration> allRules = ruleConfigRepository.findAll();
        log.info("{} adet sistem kuralı bulundu.", allRules.size());

        List<Holiday> allHolidays = holidayRepository.findByHolidayDateBetween(
                startDate.minusMonths(6),
                endDate
        );
        log.info("{} adet tatil kaydı (Bayram kontrolü için) bulundu.", allHolidays.size());

        List<MemberPreference> preferences = new ArrayList<>();

        boolean preferencesEnabled = allRules.stream()
                .filter(rule -> rule.getRuleKey().equals("ALLOW_MEMBER_PREFERENCES"))
                .findFirst()
                .map(config -> config.getRuleValue().equalsIgnoreCase("true"))
                .orElse(false);

        if (preferencesEnabled) {
            log.info("Personel Tercihleri özelliği AÇIK. Tercihler çözücüye ekleniyor.");
            preferences = preferenceRepository.findAllWithDetails();
        } else {
            log.info("Personel Tercihleri özelliği KAPALI. Yok sayılıyor.");
        }

        LocalDate historyStartDate = startDate.minusMonths(HISTORY_LOOKBACK_MONTHS).withDayOfMonth(1);
        LocalDate historyEndDate = startDate.minusDays(1);
        List<ScheduledShift> historicalShifts = scheduledShiftRepository
                .findHistoricalShiftsForSolver(historyStartDate, historyEndDate);
        log.info("{} adet geçmiş nöbet kaydı (ve detayları) hafıza olarak yüklendi.", historicalShifts.size());

        ShiftSchedulePlan problem = new ShiftSchedulePlan(
                allMembers, allLeaves, allRules,
                shiftsToPlan,
                historicalShifts,
                preferences,
                allHolidays
        );

        UUID problemId = UUID.randomUUID();

        solverManager.solveBuilder()
                .withProblemId(problemId)
                .withProblemFinder((id) -> problem)
                .withFinalBestSolutionConsumer(this::onSolutionFound)
                .withExceptionHandler(this::onSolutionFailed)
                .run();

        log.info("Çözücü başlatıldı. Takip ID: {}", problemId);
        return problemId;
    }

    @Transactional
    protected void onSolutionFound(ShiftSchedulePlan solution) {
        log.info("Planlama BAŞARILI! Skor: {}. Atamalar kaydediliyor...", solution.getScore());

        if (solution.getScore().isFeasible()) {
            boolean isHolidayRuleEnabled = solution.getRuleConfigurationList().stream()
                    .filter(rule -> rule.getRuleKey().equals("ENFORCE_FAIR_HOLIDAY_DISTRIBUTION"))
                    .findFirst()
                    .map(config -> config.getRuleValue().equalsIgnoreCase("true"))
                    .orElse(false);

            Map<LocalDate, EDayType> holidayMap = solution.getHolidayList().stream()
                    .collect(Collectors.toMap(Holiday::getHolidayDate, Holiday::getHolidayType, (tip1, tip2) -> tip1));

            List<Member> membersToUpdate = new ArrayList<>();

            for (ScheduledShift shift : solution.getScheduledShiftList()) {
                if (shift.getMember() != null) {
                    shift.setStatus(EShiftStatus.CONFIRMED);

                    if (isHolidayRuleEnabled && holidayMap.containsKey(shift.getShiftDate())) {
                        EDayType type = holidayMap.get(shift.getShiftDate());
                        if (type == EDayType.RELIGIOUS_HOLIDAY || type == EDayType.PUBLIC_HOLIDAY) {
                            Member member = shift.getMember();
                            member.setLastWorkedReligiousHolidayDate(shift.getShiftDate());
                            if (!membersToUpdate.contains(member)) {
                                membersToUpdate.add(member);
                            }
                        }
                    }
                }
            }

            if (!membersToUpdate.isEmpty()) {
                log.info("{} personelin 'son çalışılan bayram' hafızası güncelleniyor.", membersToUpdate.size());
                memberRepository.saveAll(membersToUpdate);
            }

            scheduledShiftRepository.saveAll(solution.getScheduledShiftList());
            log.info("{} adet nöbet başarıyla atandı.", solution.getScheduledShiftList().size());

            sendNotificationToAdmins(
                    "Otomatik Planlama Tamamlandı",
                    "Çizelge başarıyla oluşturuldu ve nöbetler atandı.",
                    "AUTO_SCHEDULE_SUCCESS",
                    null
            );
        } else {
            log.error("Planlama BAŞARISIZ! Çözüm bulunamadı. Skor: {}. Sert kurallar ihlal ediliyor.", solution.getScore());

            sendNotificationToAdmins(
                    "Otomatik Planlama Başarısız",
                    "Çözüm bulunamadı. Kurallar çok kısıtlayıcı veya personel yetersiz.",
                    "AUTO_SCHEDULE_FAILED",
                    null
            );
        }
    }

    protected void onSolutionFailed(UUID problemId, Throwable exception) {
        log.error("Planlama sırasında beklenmedik bir hata oluştu! Problem ID: {}", problemId, exception);

        sendNotificationToAdmins(
                "Otomatik Planlama KESİLDİ",
                "Sistem, planlama sırasında kritik bir hata ile karşılaştı. Lütfen sistem loglarını kontrol edin.",
                "AUTO_SCHEDULE_CRASHED",
                problemId.toString()
        );
    }


    public SolverStatus getSolverStatus(UUID problemId) {
        return solverManager.getSolverStatus(problemId);
    }

    private void sendNotificationToAdmins(String title, String message, String type, String relatedEntityId) {
        try {
            List<User> admins = userRepository.findByRoles_Name(ERole.ROLE_ADMIN);
            List<User> schedulers = userRepository.findByRoles_Name(ERole.ROLE_SCHEDULER);

            Set<User> recipients = Stream.concat(admins.stream(), schedulers.stream())
                    .collect(Collectors.toSet());

            log.info("{} yöneticiye ('{}') bildirimi gönderiliyor...", recipients.size(), type);

            for (User adminUser : recipients) {
                notificationService.createAndSendNotification(
                        adminUser,
                        message,
                        type,
                        relatedEntityId != null ? UUID.fromString(relatedEntityId) : null
                    );
            }
        } catch (Exception e) {
            log.error("Bildirim gönderme servisinde hata oluştu!", e);
        }
    }
}

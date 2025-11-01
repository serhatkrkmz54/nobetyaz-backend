package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.core.exception.RuleViolationException;
import com.paketnobet.nobetyaz.core.model.entity.User;
import com.paketnobet.nobetyaz.core.model.enums.ERole;
import com.paketnobet.nobetyaz.core.repository.UserRepository;
import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.audit.aop.annotation.Auditable;
import com.paketnobet.nobetyaz.modules.organization.dto.MyBidResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.ScheduledShiftResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftBidResponse;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ShiftBid;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Member;
import com.paketnobet.nobetyaz.modules.organization.model.entity.ScheduledShift;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftBidStatus;
import com.paketnobet.nobetyaz.modules.organization.model.enums.EShiftStatus;
import com.paketnobet.nobetyaz.modules.organization.repository.MemberRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ScheduledShiftRepository;
import com.paketnobet.nobetyaz.modules.organization.repository.ShiftBidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftBiddingService {

    private final ShiftBidRepository shiftBidRepository;
    private final ScheduledShiftRepository scheduledShiftRepository;
    private final MemberRepository memberRepository;
    private final ScheduleValidatorService scheduleValidator;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ScheduleService scheduleService;


    @Transactional(readOnly = true)
    public List<MyBidResponse> findMyBids(UserDetailsImpl currentUser) {
        Member member = memberRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giriş yapan kullanıcıya ait personel kaydı bulunamadı."));

        List<ShiftBid> bids = shiftBidRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());

        return bids.stream()
                .map(this::toMyBidResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduledShiftResponse> findOpenBiddingShifts() {
        log.info("Borsadaki (BIDDING) nöbetler listeleniyor...");
        List<ScheduledShift> openShifts = scheduledShiftRepository.findByStatus(EShiftStatus.BIDDING);

        return openShifts.stream()
                .map(scheduleService::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void retractBid(UUID bidId, UserDetailsImpl currentUser) {
        Member member = memberRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Personel kaydı bulunamadı."));

        ShiftBid bid = shiftBidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı."));

        if (!bid.getMember().getId().equals(member.getId())) {
            throw new SecurityException("Sadece kendi talebinizi geri çekebilirsiniz.");
        }

        if (bid.getBidStatus() != EShiftBidStatus.ACTIVE) {
            throw new RuleViolationException("Sadece 'Aktif' durumdaki talepler geri çekilebilir.");
        }

        bid.setBidStatus(EShiftBidStatus.RETRACTED);
        shiftBidRepository.save(bid);
    }

    @Transactional
    public ScheduledShiftResponse postShiftToBidding(UUID shiftId) {
        ScheduledShift shift = scheduledShiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Nöbet bulunamadı: " + shiftId));
        if (shift.getStatus() != EShiftStatus.CONFIRMED) {
            throw new RuleViolationException("Sadece atanmış ('CONFIRMED') durumdaki nöbetler tekrar borsaya açılabilir. Mevcut durum: " + shift.getStatus());
        }
        shift.setMember(null);
        shift.setStatus(EShiftStatus.BIDDING);
        scheduledShiftRepository.save(shift);
        return scheduleService.toResponse(shift);
    }

    @Transactional
    public ShiftBidResponse placeBid(UUID shiftId, UserDetailsImpl currentUser, String notes) {
        ScheduledShift shift = scheduledShiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Nöbet bulunamadı: " + shiftId));
        if (shift.getStatus() != EShiftStatus.BIDDING) {
            throw new RuleViolationException("Bu nöbet taleplere açık değil.");
        }
        Member member = memberRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bu kullanıcıya ait bir personel kaydı bulunamadı."));

        if (shiftBidRepository.existsByShiftIdAndMemberId(shiftId, member.getId())) {
            throw new RuleViolationException("Bu nöbete zaten talip oldunuz.");
        }

        scheduleValidator.validateAssignment(member, shift);

        ShiftBid bid = ShiftBid.builder()
                .shift(shift)
                .member(member)
                .bidStatus(EShiftBidStatus.ACTIVE)
                .notes(notes)
                .build();
        shiftBidRepository.save(bid);

        Set<User> managers = new HashSet<>(userRepository.findByRoles_Name(ERole.ROLE_ADMIN));
        managers.addAll(userRepository.findByRoles_Name(ERole.ROLE_SCHEDULER));
        managers.forEach(manager ->
                notificationService.createAndSendNotification(
                        manager,
                        member.getFirstName() + " " + member.getLastName() + ", " + shift.getShiftDate() + " tarihli nöbete talip oldu.",
                        "SHIFT_BID_NEW",
                        shift.getId()
                )
        );

        return toBidResponse(bid);
    }

    public List<ShiftBidResponse> listBidsForShift(UUID shiftId) {
        return shiftBidRepository.findByShiftId(shiftId).stream()
                .filter(bid -> bid.getBidStatus() == EShiftBidStatus.ACTIVE)
                .map(this::toBidResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(actionType = "AWARD_SHIFT_BID")
    public ScheduledShiftResponse awardShift(UUID shiftId, UUID bidId) {
        ShiftBid winningBid = shiftBidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep bulunamadı: " + bidId));

        if (!winningBid.getShift().getId().equals(shiftId)) {
            throw new RuleViolationException("Bu talep, belirtilen nöbete ait değil.");
        }
        if (winningBid.getBidStatus() != EShiftBidStatus.ACTIVE) {
            throw new RuleViolationException("Bu talep artık aktif değil.");
        }

        ScheduledShift shift = winningBid.getShift();
        if (shift.getStatus() != EShiftStatus.BIDDING) {
            throw new RuleViolationException("Bu nöbet artık talep toplama aşamasında değil.");
        }

        scheduleValidator.validateAssignment(winningBid.getMember(), shift);

        shift.setMember(winningBid.getMember());
        shift.setStatus(EShiftStatus.CONFIRMED);
        scheduledShiftRepository.save(shift);

        List<ShiftBid> activeBids = shiftBidRepository.findByShiftIdAndBidStatus(shiftId, EShiftBidStatus.ACTIVE);
        for (ShiftBid bid : activeBids) {
            if (bid.getId().equals(bidId)) {
                bid.setBidStatus(EShiftBidStatus.AWARDED);
            } else {
                bid.setBidStatus(EShiftBidStatus.LOST);
                notificationService.createAndSendNotification(
                        bid.getMember().getUser(),
                        shift.getShiftDate() + " tarihli nöbet talebiniz onaylanmadı.",
                        "SHIFT_BID_LOST",
                        shift.getId()
                );
            }
        }
        shiftBidRepository.saveAll(activeBids);

        notificationService.createAndSendNotification(
                winningBid.getMember().getUser(),
                shift.getShiftDate() + " tarihli nöbet talebiniz ONAYLANDI.",
                "SHIFT_BID_AWARDED",
                shift.getId()
        );
        log.info("Nöbet {} Borsa yoluyla {} personeline atandı.", shiftId, winningBid.getMember().getId());
        return scheduleService.toResponse(shift);
    }

    private ShiftBidResponse toBidResponse(ShiftBid bid) {
        String memberFullName = bid.getMember().getFirstName() + " " + bid.getMember().getLastName();
        return new ShiftBidResponse(bid.getId(), bid.getMember().getId(), memberFullName, bid.getBidStatus().name(), bid.getNotes());
    }

    private MyBidResponse toMyBidResponse(ShiftBid bid) {
        ScheduledShift shift = bid.getShift();
        return new MyBidResponse(
                bid.getId(),
                bid.getBidStatus().name(),
                bid.getNotes(),
                shift.getId(),
                shift.getLocation().getName(),
                shift.getShiftDate(),
                shift.getShiftTemplate().getStartTime(),
                shift.getShiftTemplate().getEndTime()
        );
    }
}


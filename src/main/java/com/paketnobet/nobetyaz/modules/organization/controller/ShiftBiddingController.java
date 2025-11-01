package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.core.security.jwt.services.UserDetailsImpl;
import com.paketnobet.nobetyaz.modules.organization.dto.MyBidResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.ScheduledShiftResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftBidCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.ShiftBidResponse;
import com.paketnobet.nobetyaz.modules.organization.service.ShiftBiddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bidding")
@RequiredArgsConstructor
public class ShiftBiddingController {

    private final ShiftBiddingService biddingService;

    @GetMapping("/my-bids")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MyBidResponse>> getMyBids(
                                                          @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(biddingService.findMyBids(currentUser));
    }

    @GetMapping("/open-shifts")
    @PreAuthorize("isAuthenticated() AND @securityService.isFeatureEnabled('ALLOW_SHIFT_BIDDING')")
    public ResponseEntity<List<ScheduledShiftResponse>> getOpenBiddingShifts() {
        return ResponseEntity.ok(biddingService.findOpenBiddingShifts());
    }

    @PutMapping("/bids/{bidId}/retract")
    @PreAuthorize("isAuthenticated() AND @securityService.isFeatureEnabled('ALLOW_SHIFT_BIDDING')")
    public ResponseEntity<Void> retractBid(
            @PathVariable UUID bidId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        biddingService.retractBid(bidId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/shifts/{shiftId}/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER') AND @securityService.isFeatureEnabled('ALLOW_SHIFT_BIDDING')")
    public ResponseEntity<ScheduledShiftResponse> postShiftToBidding(@PathVariable UUID shiftId) {
        return ResponseEntity.ok(biddingService.postShiftToBidding(shiftId));
    }

    @PostMapping("/shifts/{shiftId}/bids")
    @PreAuthorize("isAuthenticated() AND @securityService.isFeatureEnabled('ALLOW_SHIFT_BIDDING')")
    public ResponseEntity<ShiftBidResponse> placeBid(
            @PathVariable UUID shiftId,
            @RequestBody(required = false) ShiftBidCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        String notes = (request != null) ? request.notes() : null;
        return new ResponseEntity<>(biddingService.placeBid(shiftId, currentUser, notes), HttpStatus.CREATED);
    }

    @GetMapping("/shifts/{shiftId}/bids")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER') AND @securityService.isFeatureEnabled('ALLOW_SHIFT_BIDDING')")
    public ResponseEntity<List<ShiftBidResponse>> listBidsForShift(@PathVariable UUID shiftId) {
        return ResponseEntity.ok(biddingService.listBidsForShift(shiftId));
    }

    @PostMapping("/shifts/{shiftId}/bids/{bidId}/award")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER') AND @securityService.isFeatureEnabled('ALLOW_SHIFT_BIDDING')")
    public ResponseEntity<ScheduledShiftResponse> awardShift(
            @PathVariable UUID shiftId,
            @PathVariable UUID bidId) {
        return ResponseEntity.ok(biddingService.awardShift(shiftId, bidId));
    }

}

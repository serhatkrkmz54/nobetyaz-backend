package com.paketnobet.nobetyaz.modules.setup.controller;

import com.paketnobet.nobetyaz.modules.dto.SetupRequest;
import com.paketnobet.nobetyaz.modules.dto.SetupStatusResponse;
import com.paketnobet.nobetyaz.modules.setup.service.SetupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {

    private final SetupService setupService;

    @GetMapping("/status")
    public ResponseEntity<SetupStatusResponse> getSetupStatus() {
        boolean isComplete = setupService.isSetupComplete();
        return ResponseEntity.ok(new SetupStatusResponse(isComplete));
    }

    @PostMapping
    public ResponseEntity<?> executeSetup(@Valid @RequestBody SetupRequest request) {
        setupService.performSetup(request);
        return ResponseEntity.ok("Kurulum başarıyla tamamlandı.");
    }
}

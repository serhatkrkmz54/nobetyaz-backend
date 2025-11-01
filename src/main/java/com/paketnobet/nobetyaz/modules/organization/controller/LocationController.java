package com.paketnobet.nobetyaz.modules.organization.controller;

import com.paketnobet.nobetyaz.modules.organization.dto.LocationCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.LocationResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.LocationUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationCreateRequest request) {
        LocationResponse response = locationService.create(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<List<LocationResponse>> getAllLocations(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive
    ) {
        List<LocationResponse> locations;
        if (includeInactive) {
            locations = locationService.findAllIncludingInactive();
        } else {
            locations = locationService.findAllActive();
        }
        return ResponseEntity.ok(locations);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody LocationUpdateRequest request
    ) {
        LocationResponse updatedLocation = locationService.update(id, request);
        return ResponseEntity.ok(updatedLocation);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable UUID id) {
        return ResponseEntity.ok(locationService.findById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHEDULER')")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

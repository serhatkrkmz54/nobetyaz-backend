package com.paketnobet.nobetyaz.modules.organization.service;

import com.paketnobet.nobetyaz.core.exception.ResourceNotFoundException;
import com.paketnobet.nobetyaz.modules.organization.dto.LocationCreateRequest;
import com.paketnobet.nobetyaz.modules.organization.dto.LocationResponse;
import com.paketnobet.nobetyaz.modules.organization.dto.LocationUpdateRequest;
import com.paketnobet.nobetyaz.modules.organization.model.entity.Location;
import com.paketnobet.nobetyaz.modules.organization.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    @Transactional
    public LocationResponse create(LocationCreateRequest request) {
        Location location = Location.builder()
                .name(request.name())
                .description(request.description())
                .isActive(true)
                .build();
        locationRepository.save(location);
        return toResponse(location);
    }

    public LocationResponse findById(UUID id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        return toResponse(location);
    }

    @Transactional
    public LocationResponse update(UUID id, LocationUpdateRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        location.setName(request.name());
        location.setDescription(request.description());
        location.setActive(request.isActive());

        locationRepository.save(location);
        return toResponse(location);
    }

    @Transactional
    public void delete(UUID id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location not found with id: " + id);
        }
        locationRepository.deleteById(id);
    }

    public List<LocationResponse> findAll() {
        return locationRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<LocationResponse> findAllActive() {
        return locationRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<LocationResponse> findAllIncludingInactive() {
        return locationRepository.findAllByOrderByNameAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private LocationResponse toResponse(Location location) {
        return new LocationResponse(location.getId(), location.getName(), location.getDescription(), location.isActive());
    }

}

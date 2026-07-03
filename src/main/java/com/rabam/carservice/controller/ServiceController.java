package com.rabam.carservice.controller;

import com.rabam.carservice.dto.ServiceRequestDto;
import com.rabam.carservice.dto.ServiceResponseDto;
import com.rabam.carservice.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ServiceController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    public ResponseEntity<Page<ServiceResponseDto>> getAllServices(
            @RequestParam(required = false) Long carId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(maintenanceService.getAllServices(carId, status, pageable));
    }

    @PostMapping
    public ResponseEntity<ServiceResponseDto> createService(@Valid @RequestBody ServiceRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.createService(requestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponseDto> updateService(
            @PathVariable Long id,
            @RequestBody ServiceRequestDto requestDto) {
        return ResponseEntity.ok(maintenanceService.updateService(id, requestDto));
    }
}
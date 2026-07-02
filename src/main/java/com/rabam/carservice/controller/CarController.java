package com.rabam.carservice.controller;

import com.rabam.carservice.dto.CarRequestDto;
import com.rabam.carservice.dto.CarResponseDto;
import com.rabam.carservice.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows communication with Frontend
public class CarController {

    private final CarService carService;

    @GetMapping
    public ResponseEntity<Page<CarResponseDto>> getAllCars(Pageable pageable) {
        return ResponseEntity.ok(carService.getAllCars(pageable));
    }

    @PostMapping
    public ResponseEntity<CarResponseDto> createCar(@Valid @RequestBody CarRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.createCar(requestDto));
    }

    public ResponseEntity<CarResponseDto> updateCar(@PathVariable Long id, @Valid @RequestBody CarRequestDto requestDto) {
        return ResponseEntity.ok(carService.updateCar(id, requestDto));
    }
}
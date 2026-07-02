package com.rabam.carservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarRequestDto {
    @NotBlank(message = "License plate is required")
    // Simple alphanumeric with optional hyphens/spaces validation
    @Pattern(regexp = "^[A-Z0-9\\s-]{5,10}$", message = "Invalid license plate format")
    private String licensePlate;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Brand is required")
    private String brand;
}
package com.rabam.carservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CarResponseDto {
    private Long id;
    private String licensePlate;
    private String model;
    private String brand;
}
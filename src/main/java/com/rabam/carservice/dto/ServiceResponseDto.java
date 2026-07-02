package com.rabam.carservice.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ServiceResponseDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private Long carId;
    private String carLicensePlate;
    private Long version;
}
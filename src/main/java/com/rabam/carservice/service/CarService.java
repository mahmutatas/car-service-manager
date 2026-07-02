package com.rabam.carservice.service;

import com.rabam.carservice.config.RabbitMQConfig;
import com.rabam.carservice.dto.AuditEventDto;
import com.rabam.carservice.dto.CarRequestDto;
import com.rabam.carservice.dto.CarResponseDto;
import com.rabam.carservice.entity.Car;
import com.rabam.carservice.exception.ResourceNotFoundException;
import com.rabam.carservice.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(readOnly = true)
    public Page<CarResponseDto> getAllCars(Pageable pageable) {
        return carRepository.findAll(pageable).map(this::mapToResponseDto);
    }

    @Transactional
    public CarResponseDto createCar(CarRequestDto requestDto) {
        // Case Requirement 2b: Unique license plate check
        if (carRepository.existsByLicensePlate(requestDto.getLicensePlate())) {
            // Will be caught by GlobalExceptionHandler to return 409 Conflict
            throw new IllegalStateException("CONFLICT: License plate already exists");
        }

        Car car = new Car();
        car.setLicensePlate(requestDto.getLicensePlate());
        car.setModel(requestDto.getModel());
        car.setBrand(requestDto.getBrand());

        Car savedCar = carRepository.save(car);
        CarResponseDto responseDto = mapToResponseDto(savedCar);

        // Case Requirement 6: Publish domain event to RabbitMQ
        publishEvent("CAR_CREATED", "Car", savedCar.getId(), responseDto);

        return responseDto;
    }

    @Transactional
    public CarResponseDto updateCar(Long id, CarRequestDto requestDto) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));

        // Check uniqueness if license plate is changing
        if (!car.getLicensePlate().equals(requestDto.getLicensePlate()) && 
            carRepository.existsByLicensePlate(requestDto.getLicensePlate())) {
            throw new IllegalStateException("CONFLICT: License plate already exists");
        }

        car.setBrand(requestDto.getBrand());
        car.setModel(requestDto.getModel());
        car.setLicensePlate(requestDto.getLicensePlate());

        Car updatedCar = carRepository.save(car);
        CarResponseDto responseDto = mapToResponseDto(updatedCar);

        publishEvent("CAR_UPDATED", "Car", updatedCar.getId(), responseDto);

        return responseDto;
    }

    private void publishEvent(String eventType, String entityType, Long entityId, Object payload) {
        AuditEventDto event = new AuditEventDto(eventType, entityType, entityId, LocalDateTime.now(), payload);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
    }

    private CarResponseDto mapToResponseDto(Car car) {
        CarResponseDto dto = new CarResponseDto();
        dto.setId(car.getId());
        dto.setLicensePlate(car.getLicensePlate());
        dto.setModel(car.getModel());
        dto.setBrand(car.getBrand());
        return dto;
    }
}
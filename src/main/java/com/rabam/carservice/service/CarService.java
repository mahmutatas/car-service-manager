package com.rabam.carservice.service;

import com.rabam.carservice.config.RabbitMQConfig;
import com.rabam.carservice.dto.AuditEventDto;
import com.rabam.carservice.dto.CarRequestDto;
import com.rabam.carservice.dto.CarResponseDto;
import com.rabam.carservice.entity.Car;
import com.rabam.carservice.exception.DuplicateLicensePlateException;
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
        // Fast-path check: covers the common case with a clear message.
        if (carRepository.existsByLicensePlate(requestDto.getLicensePlate())) {
            throw new DuplicateLicensePlateException(requestDto.getLicensePlate());
        }

        Car car = new Car();
        car.setLicensePlate(requestDto.getLicensePlate());
        car.setModel(requestDto.getModel());
        car.setBrand(requestDto.getBrand());

        Car savedCar;
        try {
            // The DB-level unique constraint on licensePlate is the real source of truth:
            // it protects against the race where two requests with the same plate both
            // pass the existsByLicensePlate() check above at nearly the same time.
            // saveAndFlush forces the constraint check to happen here, inside the try block,
            // instead of silently at transaction commit after this method has returned.
            savedCar = carRepository.saveAndFlush(car);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new DuplicateLicensePlateException(requestDto.getLicensePlate());
        }

        CarResponseDto responseDto = mapToResponseDto(savedCar);
        publishEvent("CAR_CREATED", "Car", savedCar.getId(), responseDto);
        return responseDto;
    }


    @Transactional
    public CarResponseDto updateCar(Long id, CarRequestDto requestDto) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + id));

        if (!car.getLicensePlate().equals(requestDto.getLicensePlate()) &&
            carRepository.existsByLicensePlate(requestDto.getLicensePlate())) {
            throw new DuplicateLicensePlateException(requestDto.getLicensePlate());
        }

        car.setBrand(requestDto.getBrand());
        car.setModel(requestDto.getModel());
        car.setLicensePlate(requestDto.getLicensePlate());

        Car updatedCar;
        try {
            updatedCar = carRepository.saveAndFlush(car);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new DuplicateLicensePlateException(requestDto.getLicensePlate());
        }

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
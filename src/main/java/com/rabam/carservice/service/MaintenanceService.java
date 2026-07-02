package com.rabam.carservice.service;

import com.rabam.carservice.config.RabbitMQConfig;
import com.rabam.carservice.dto.AuditEventDto;
import com.rabam.carservice.dto.ServiceRequestDto;
import com.rabam.carservice.dto.ServiceResponseDto;
import com.rabam.carservice.entity.Car;
import com.rabam.carservice.entity.Service;
import com.rabam.carservice.entity.ServiceStatus;
import com.rabam.carservice.exception.ResourceNotFoundException;
import com.rabam.carservice.repository.CarRepository;
import com.rabam.carservice.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final ServiceRepository serviceRepository;
    private final CarRepository carRepository;
    private final ServiceManager serviceManager;
    private final RabbitTemplate rabbitTemplate;

    @Transactional(readOnly = true)
    public Page<ServiceResponseDto> getAllServices(Long carId, String status, Pageable pageable) {
        ServiceStatus serviceStatus = null;
        
        // Safely convert string status to Enum if provided
        if (status != null && !status.isBlank()) {
            try {
                serviceStatus = ServiceStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // If an invalid status string is passed, we can handle it or let it return empty
                throw new com.rabam.carservice.exception.InvalidTransitionException("Invalid status filter provided: " + status);
            }
        }

        // Fetch from database with structural pagination intact, then map directly to DTO
        return serviceRepository.findByCarIdAndStatus(carId, serviceStatus, pageable)
                .map(this::mapToResponseDto);
    }

    @Transactional
    public ServiceResponseDto createService(ServiceRequestDto requestDto) {
        Car car = carRepository.findById(requestDto.getCarId())
                .orElseThrow(() -> new ResourceNotFoundException("Car not found with id: " + requestDto.getCarId()));

        Service service = new Service();
        service.setTitle(requestDto.getTitle());
        service.setDescription(requestDto.getDescription());
        service.setStatus(ServiceStatus.PENDING); // Initial state is always PENDING
        service.setCar(car);

        Service savedService = serviceRepository.save(service);
        ServiceResponseDto responseDto = mapToResponseDto(savedService);

        publishEvent("SERVICE_CREATED", "Service", savedService.getId(), responseDto);
        return responseDto;
    }

    @Transactional
    public ServiceResponseDto updateService(Long id, ServiceRequestDto requestDto) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        if (requestDto.getTitle() != null) {
            service.setTitle(requestDto.getTitle());
        }
        if (requestDto.getDescription() != null) {
            service.setDescription(requestDto.getDescription());
        }

        if (requestDto.getStatus() != null) {
            ServiceStatus newStatus = ServiceStatus.valueOf(requestDto.getStatus().toUpperCase());
            // Triggers state machine validation and concurrency checks
            serviceManager.validateAndTransition(service, newStatus);
        }

        // Optimistic locking via @Version triggers here on save
        Service updatedService = serviceRepository.save(service);
        ServiceResponseDto responseDto = mapToResponseDto(updatedService);

        publishEvent("SERVICE_UPDATED", "Service", updatedService.getId(), responseDto);
        return responseDto;
    }

    private void publishEvent(String eventType, String entityType, Long entityId, Object payload) {
        AuditEventDto event = new AuditEventDto(eventType, entityType, entityId, LocalDateTime.now(), payload);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, event);
    }

    private ServiceResponseDto mapToResponseDto(Service service) {
        ServiceResponseDto dto = new ServiceResponseDto();
        dto.setId(service.getId());
        dto.setTitle(service.getTitle());
        dto.setDescription(service.getDescription());
        dto.setStatus(service.getStatus().name());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setCarId(service.getCar().getId());
        dto.setCarLicensePlate(service.getCar().getLicensePlate());
        dto.setVersion(service.getVersion());
        return dto;
    }
}
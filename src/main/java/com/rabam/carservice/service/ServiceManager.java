package com.rabam.carservice.service;

import com.rabam.carservice.entity.Service;
import com.rabam.carservice.entity.ServiceStatus;
import com.rabam.carservice.exception.InvalidTransitionException;
import com.rabam.carservice.exception.MaxActiveServicesException;
import com.rabam.carservice.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceManager {

    private final ServiceRepository serviceRepository;

    /**
     * Case Requirement 3: Centralized State Machine for Service Transitions.
     * Valid transitions: PENDING -> IN_PROGRESS -> DONE (forward only, no skipping,
     * no going backward, no re-entering the same state).
     *
     * NOTE: the max-2-active-per-car check below only prevents the race condition
     * if the caller has already acquired a pessimistic lock on the parent Car row
     * (see CarRepository.findByIdForUpdate). This method assumes it is called
     * within that locked transaction.
     */
    public void validateAndTransition(Service service, ServiceStatus newStatus) {
        ServiceStatus currentStatus = service.getStatus();

        boolean isValid = false;

        switch (currentStatus) {
            case PENDING:
                if (newStatus == ServiceStatus.IN_PROGRESS) {
                    isValid = true;
                }
                break;
            case IN_PROGRESS:
                if (newStatus == ServiceStatus.DONE) {
                    isValid = true;
                }
                break;
            case DONE:
                // DONE is a terminal state, no further transitions allowed
                break;
        }

        if (!isValid) {
            throw new InvalidTransitionException(
                String.format(
                    "Invalid status transition attempted: %s -> %s. " +
                    "Only forward transitions are allowed (PENDING -> IN_PROGRESS -> DONE), " +
                    "with no skipping, no going backward, and no re-entering the same state.",
                    currentStatus, newStatus
                )
            );
        }

        if (newStatus == ServiceStatus.IN_PROGRESS) {
            long activeCount = serviceRepository.countByCarIdAndStatus(
                    service.getCar().getId(), ServiceStatus.IN_PROGRESS);
            if (activeCount >= 2) {
                throw new MaxActiveServicesException(
                    "Cannot activate service. This car already has 2 active services (IN_PROGRESS)."
                );
            }
        }

        service.setStatus(newStatus);
    }
}
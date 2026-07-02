package com.rabam.carservice.service;

import com.rabam.carservice.entity.Car;
import com.rabam.carservice.entity.Service;
import com.rabam.carservice.entity.ServiceStatus;
import com.rabam.carservice.exception.InvalidTransitionException;
import com.rabam.carservice.exception.MaxActiveServicesException;
import com.rabam.carservice.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceManagerTest {

    private ServiceRepository serviceRepository;
    private ServiceManager serviceManager;
    private Service service;

    @BeforeEach
    void setUp() {
        serviceRepository = mock(ServiceRepository.class);
        serviceManager = new ServiceManager(serviceRepository);

        Car car = new Car(1L, "34ABC123", "Corolla", "Toyota");
        service = new Service();
        service.setId(1L);
        service.setCar(car);
    }

    @Test
    void allowsPendingToInProgress() {
        service.setStatus(ServiceStatus.PENDING);
        when(serviceRepository.countByCarIdAndStatus(1L, ServiceStatus.IN_PROGRESS)).thenReturn(0L);

        serviceManager.validateAndTransition(service, ServiceStatus.IN_PROGRESS);

        assertThat(service.getStatus()).isEqualTo(ServiceStatus.IN_PROGRESS);
    }

    @Test
    void allowsInProgressToDone() {
        service.setStatus(ServiceStatus.IN_PROGRESS);

        serviceManager.validateAndTransition(service, ServiceStatus.DONE);

        assertThat(service.getStatus()).isEqualTo(ServiceStatus.DONE);
    }

    @Test
    void rejectsSkippingPendingDirectlyToDone() {
        service.setStatus(ServiceStatus.PENDING);

        assertThatThrownBy(() -> serviceManager.validateAndTransition(service, ServiceStatus.DONE))
                .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    void rejectsGoingBackward() {
        service.setStatus(ServiceStatus.IN_PROGRESS);

        assertThatThrownBy(() -> serviceManager.validateAndTransition(service, ServiceStatus.PENDING))
                .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    void rejectsReEnteringTheSameState() {
        service.setStatus(ServiceStatus.IN_PROGRESS);

        assertThatThrownBy(() -> serviceManager.validateAndTransition(service, ServiceStatus.IN_PROGRESS))
                .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    void rejectsAnyTransitionOutOfDone() {
        service.setStatus(ServiceStatus.DONE);

        assertThatThrownBy(() -> serviceManager.validateAndTransition(service, ServiceStatus.IN_PROGRESS))
                .isInstanceOf(InvalidTransitionException.class);
    }

    @Test
    void rejectsThirdActiveServiceOnSameCar() {
        service.setStatus(ServiceStatus.PENDING);
        when(serviceRepository.countByCarIdAndStatus(1L, ServiceStatus.IN_PROGRESS)).thenReturn(2L);

        assertThatThrownBy(() -> serviceManager.validateAndTransition(service, ServiceStatus.IN_PROGRESS))
                .isInstanceOf(MaxActiveServicesException.class);
    }
}
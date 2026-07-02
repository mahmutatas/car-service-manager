package com.rabam.carservice.concurrency;

import com.rabam.carservice.entity.Car;
import com.rabam.carservice.entity.Service;
import com.rabam.carservice.entity.ServiceStatus;
import com.rabam.carservice.repository.CarRepository;
import com.rabam.carservice.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class OptimisticLockingConcurrencyTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Case Requirement 4b: load an entity, update it twice from two different
     * "sessions"/copies, and assert the second write is rejected.
     */
    @Test
    void secondStaleWriteIsRejectedByOptimisticLocking() {
        Car car = carRepository.saveAndFlush(new Car(null, "34OPT001", "Corolla", "Toyota"));

        Service service = new Service();
        service.setTitle("Oil change");
        service.setStatus(ServiceStatus.PENDING);
        service.setCar(car);
        service = serviceRepository.saveAndFlush(service);
        Long serviceId = service.getId();

        // Two independent "sessions" load the same row (same version at this point)
        Service session1 = serviceRepository.findById(serviceId).orElseThrow();
        Service session2 = serviceRepository.findById(serviceId).orElseThrow();

        assertThat(session1.getVersion()).isEqualTo(session2.getVersion());

        // Session 1 updates and saves first -> succeeds, version is bumped in the DB
        session1.setDescription("Updated by session 1");
        serviceRepository.saveAndFlush(session1);

        // Session 2 still holds the OLD version -> its write must be rejected, not silently applied
        session2.setDescription("Updated by session 2 (stale)");
        assertThatThrownBy(() -> serviceRepository.saveAndFlush(session2))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        // The persisted state must reflect session 1's change only
        Service finalState = serviceRepository.findById(serviceId).orElseThrow();
        assertThat(finalState.getDescription()).isEqualTo("Updated by session 1");
    }
}
package com.rabam.carservice.concurrency;

import com.rabam.carservice.entity.Car;
import com.rabam.carservice.entity.Service;
import com.rabam.carservice.entity.ServiceStatus;
import com.rabam.carservice.exception.MaxActiveServicesException;
import com.rabam.carservice.repository.CarRepository;
import com.rabam.carservice.repository.ServiceRepository;
import com.rabam.carservice.service.ServiceManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MaxActiveServicesRaceConditionTest {

    @Autowired private CarRepository carRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private ServiceManager serviceManager;
    @Autowired private PlatformTransactionManager transactionManager;

    /**
     * Case Requirement 5: two PUT requests trying to move a service into IN_PROGRESS
     * for the SAME car at (nearly) the same time must not both succeed if that would
     * push the active count above 2.
     */
    @Test
    void concurrentActivationsNeverExceedTwoActiveServicesPerCar() throws Exception {
        Car car = carRepository.saveAndFlush(new Car(null, "34RACE01", "Civic", "Honda"));

        // Car already has 1 active service, occupying one of the two allowed slots.
        serviceRepository.saveAndFlush(newService(car, ServiceStatus.IN_PROGRESS));

        // Two PENDING services competing for the single remaining slot.
        Service candidateA = serviceRepository.saveAndFlush(newService(car, ServiceStatus.PENDING));
        Service candidateB = serviceRepository.saveAndFlush(newService(car, ServiceStatus.PENDING));

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();

        List<Callable<Void>> tasks = List.of(
                () -> activate(candidateA.getId(), car.getId(), transactionTemplate, startLatch, successCount, rejectedCount),
                () -> activate(candidateB.getId(), car.getId(), transactionTemplate, startLatch, successCount, rejectedCount)
        );

        List<Future<Void>> futures = new ArrayList<>();
        for (Callable<Void> task : tasks) {
            futures.add(executor.submit(task));
        }
        startLatch.countDown(); // release both threads at (nearly) the same instant

        for (Future<Void> f : futures) {
            f.get(10, TimeUnit.SECONDS);
        }
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(rejectedCount.get()).isEqualTo(1);

        long finalActiveCount = serviceRepository.countByCarIdAndStatus(car.getId(), ServiceStatus.IN_PROGRESS);
        assertThat(finalActiveCount).isEqualTo(2); // never 3
    }

    private Void activate(Long serviceId, Long carId, TransactionTemplate tx, CountDownLatch startLatch,
                           AtomicInteger successCount, AtomicInteger rejectedCount) {
        try {
            startLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            tx.executeWithoutResult(status -> {
                carRepository.findByIdForUpdate(carId).orElseThrow();
                Service service = serviceRepository.findById(serviceId).orElseThrow();
                serviceManager.validateAndTransition(service, ServiceStatus.IN_PROGRESS);
                serviceRepository.saveAndFlush(service);
            });
            successCount.incrementAndGet();
        } catch (MaxActiveServicesException e) {
            rejectedCount.incrementAndGet();
        }
        return null;
    }

    private Service newService(Car car, ServiceStatus status) {
        Service service = new Service();
        service.setTitle("Inspection");
        service.setStatus(status);
        service.setCar(car);
        return service;
    }
}
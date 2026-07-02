package com.rabam.carservice.repository;

import com.rabam.carservice.entity.Car;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    boolean existsByLicensePlate(String licensePlate); // For uniqueness checking

    /**
     * Case Requirement 5: locks the Car row for the duration of the transaction
     * (SELECT ... FOR UPDATE). Any concurrent request trying to move another
     * service of the same car into IN_PROGRESS will block here until the first
     * transaction commits or rolls back, which is what makes the max-2-active
     * check in ServiceManager race-condition safe.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Car c where c.id = :carId")
    Optional<Car> findByIdForUpdate(@Param("carId") Long carId);
}
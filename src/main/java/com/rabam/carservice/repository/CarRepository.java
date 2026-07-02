package com.rabam.carservice.repository;

import com.rabam.carservice.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByLicensePlate(String licensePlate); // For uniqueness checking [cite: 43]
}
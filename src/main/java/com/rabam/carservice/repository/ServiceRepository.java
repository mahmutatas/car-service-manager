package com.rabam.carservice.repository;

import com.rabam.carservice.entity.Service;
import com.rabam.carservice.entity.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    // Case 5a: To find the number of services a vehicle has in the IN_PROGRESS state [cite: 54]
    @Query("SELECT COUNT(s) FROM Service s WHERE s.car.id = :carId AND s.status = :status")
    long countByCarIdAndStatus(@Param("carId") Long carId, @Param("status") ServiceStatus status);
}
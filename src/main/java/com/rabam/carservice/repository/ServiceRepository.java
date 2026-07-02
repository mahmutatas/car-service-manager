package com.rabam.carservice.repository;

import com.rabam.carservice.entity.Service;
import com.rabam.carservice.entity.ServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    // Count active (IN_PROGRESS) services for a specific car to enforce the max-2 rule
    @Query("SELECT COUNT(s) FROM Service s WHERE s.car.id = :carId AND s.status = :status")
    long countByCarIdAndStatus(@Param("carId") Long carId, @Param("status") ServiceStatus status);

    // Dynamic filtering at database level to ensure Page structure remains intact
    @Query("SELECT s FROM Service s WHERE " +
           "(:carId IS NULL OR s.car.id = :carId) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<Service> findByCarIdAndStatus(
            @Param("carId") Long carId, 
            @Param("status") ServiceStatus status, 
            Pageable pageable);
}
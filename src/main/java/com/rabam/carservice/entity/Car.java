package com.rabam.carservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cars")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Case Requirement 2b: License plates must be unique
    @Column(unique = true, nullable = false) 
    private String licensePlate;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String brand;
}
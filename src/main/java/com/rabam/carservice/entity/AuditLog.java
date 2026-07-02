package com.rabam.carservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType; // e.g., CAR_CREATED, SERVICE_UPDATED

    @Column(nullable = false)
    private String entityType; // e.g., Car, Service

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Lob // For large JSON payload
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String payload;
}
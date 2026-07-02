package com.rabam.carservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventDto {
    private String eventType;
    private String entityType;
    private Long entityId;
    private LocalDateTime timestamp;
    private Object payload; // Dynamic payload (Car or Service details)
}
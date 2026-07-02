package com.rabam.carservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabam.carservice.config.RabbitMQConfig;
import com.rabam.carservice.dto.AuditEventDto;
import com.rabam.carservice.entity.AuditLog;
import com.rabam.carservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogConsumer {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeAuditEvent(AuditEventDto event) {
        // Case Requirement 6: Standardized application log entry
        log.info("Received Domain Event - Type: {}, Entity: {}, ID: {}", 
                event.getEventType(), event.getEntityType(), event.getEntityId());

        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEventType(event.getEventType());
            auditLog.setEntityType(event.getEntityType());
            auditLog.setEntityId(event.getEntityId());
            auditLog.setTimestamp(event.getTimestamp());
            
            // Serialize payload object to JSON String
            String jsonPayload = objectMapper.writeValueAsString(event.getPayload());
            auditLog.setPayload(jsonPayload);

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to persist audit log to database", e);
        }
    }
}
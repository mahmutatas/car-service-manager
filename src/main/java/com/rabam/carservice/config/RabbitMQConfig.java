package com.rabam.carservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "car-service-audit-queue";
    public static final String EXCHANGE_NAME = "car-service-exchange";
    public static final String ROUTING_KEY = "car.service.audit.routingKey";

    @Bean
    public Queue auditQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(auditQueue).to(auditExchange).with(ROUTING_KEY);
    }

    // Crucial for sending messages as JSON objects automatically
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
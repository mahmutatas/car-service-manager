package com.rabam.carservice.exception;

public class MaxActiveServicesException extends RuntimeException {
    public MaxActiveServicesException(String message) {
        super(message);
    }
}
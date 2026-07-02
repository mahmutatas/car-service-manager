package com.rabam.carservice.exception;

public class DuplicateLicensePlateException extends RuntimeException {
    public DuplicateLicensePlateException(String licensePlate) {
        super("A car with license plate '" + licensePlate + "' already exists");
    }
}
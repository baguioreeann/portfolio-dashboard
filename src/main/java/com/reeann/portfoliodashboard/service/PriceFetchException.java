package com.reeann.portfoliodashboard.service;

public class PriceFetchException extends RuntimeException {
    public PriceFetchException(String message, Throwable cause) {
        super(message, cause);
    }

    public PriceFetchException(String message) {
        super(message);
    }
}

package com.financetracker.exception;

/** Thrown when a business rule is violated (e.g. duplicate email, expired token). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}

package com.financetracker.exception;

/** Thrown when a requested resource does not exist or is not owned by the current user. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

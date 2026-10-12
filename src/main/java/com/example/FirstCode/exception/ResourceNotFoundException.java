package com.example.FirstCode.exception;

/**
 * Exception thrown when a requested resource (e.g. Student) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

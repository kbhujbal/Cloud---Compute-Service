package com.cloud.compute.exception;

public class VMException extends RuntimeException {
    public VMException(String message) {
        super(message);
    }

    public VMException(String message, Throwable cause) {
        super(message, cause);
    }
} 
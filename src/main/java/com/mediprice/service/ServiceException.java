package com.mediprice.service;

/**
 * Checked exception thrown by service layer for business logic errors.
 */
public class ServiceException extends Exception {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

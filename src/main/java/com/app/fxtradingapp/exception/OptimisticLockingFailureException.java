package com.app.fxtradingapp.exception;

public class OptimisticLockingFailureException extends RuntimeException {
    public OptimisticLockingFailureException(String message) {
        super(message);
    }
}

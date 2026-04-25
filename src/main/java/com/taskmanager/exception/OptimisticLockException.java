package com.taskmanager.exception;

public class OptimisticLockException extends RuntimeException{
    public OptimisticLockException(String message) {
        super(message);
    }
}

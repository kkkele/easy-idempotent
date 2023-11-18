package com.superkele.idempotent.exception;

public class RepeatUsingTypeException extends RuntimeException{

    public RepeatUsingTypeException() {
    }

    public RepeatUsingTypeException(String message) {
        super(message);
    }

    public RepeatUsingTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepeatUsingTypeException(Throwable cause) {
        super(cause);
    }
}

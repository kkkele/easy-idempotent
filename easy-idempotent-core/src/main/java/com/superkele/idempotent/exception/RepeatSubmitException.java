package com.superkele.idempotent.exception;

public class RepeatSubmitException extends RuntimeException {
    public RepeatSubmitException() {
        super("请求重复提交");
    }

    public RepeatSubmitException(String message) {
        super(message);
    }

    public RepeatSubmitException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepeatSubmitException(Throwable cause) {
        super(cause);
    }
}

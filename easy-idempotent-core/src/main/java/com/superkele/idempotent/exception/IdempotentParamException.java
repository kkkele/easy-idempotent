package com.superkele.idempotent.exception;

public class IdempotentParamException extends RuntimeException{

    public IdempotentParamException() {
        super("方法参数设置错误");
    }

    public IdempotentParamException(String message) {
        super(message);
    }
}

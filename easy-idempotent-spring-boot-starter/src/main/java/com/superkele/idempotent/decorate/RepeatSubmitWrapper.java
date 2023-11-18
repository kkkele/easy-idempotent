package com.superkele.idempotent.decorate;

import com.superkele.idempotent.annotations.Idempotent;
import org.aspectj.lang.JoinPoint;

public class RepeatSubmitWrapper {

    /**
     * 获取注解
     */
    private Idempotent idempotent;


    /**
     * 获取参数
     */
    private JoinPoint joinPoint;

    public RepeatSubmitWrapper(Idempotent idempotent, JoinPoint joinPoint) {
        this.idempotent = idempotent;
        this.joinPoint = joinPoint;
    }

    public Idempotent getIdempotent() {
        return idempotent;
    }

    public JoinPoint getJoinPoint() {
        return joinPoint;
    }
}

package com.superkele.idempotent.aspect;

import com.superkele.idempotent.annotations.Idempotent;
import org.aspectj.lang.ProceedingJoinPoint;

public interface ImpotentInterface {

    Object idempotentHandler(ProceedingJoinPoint joinPoint, Idempotent idempotent);
}

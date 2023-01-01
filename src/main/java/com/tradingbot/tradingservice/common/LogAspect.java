package com.tradingbot.tradingservice.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LogAspect {
    @Around("execution(* *..controller..*.*(..))")
    public Object controllerLogging(ProceedingJoinPoint joinPoint) throws Throwable{

        String methodName = joinPoint.getSignature().toShortString();
        try {
            log.debug(methodName+" is start");
            return joinPoint.proceed();
        }finally {
            log.debug(methodName + " is finish");
        }
    }
}

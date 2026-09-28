package com.seongmo.community;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TimeLoggingAspect {

    // Service 어노테이션에 대해서 로깅
    @Around("@within(org.springframework.stereotype.Service)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("시작: " + joinPoint.getSignature());

        try {
            return joinPoint.proceed();
        } finally {
            log.info("종료: " + joinPoint.getSignature());
        }
    }
}

package rnd.poc.multi.threading.service;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
@Slf4j
public class ExecutionTimeMonitor {

    @Pointcut("@annotation(rnd.poc.multi.threading.service.ExecutionTimeLoggable)")
    public void executionTimeLoggablePointcut() {}

    @Around("executionTimeLoggablePointcut()")
    public Object executeJoinPoint(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Instant start = Instant.now();
        Object result = proceedingJoinPoint.proceed();
        Instant end = Instant.now();

        log.info("Method {} has been executed in {}", proceedingJoinPoint, Duration.between(start, end));

        return result;
    }

}

package com.master.Restful_Blog_Api.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /// Match All Methods inside Any Class under Controller Package
    @Pointcut("execution(* com.master.Restful_Blog_Api.controller.*.*(..))")
    public void controllerMethods(){}

    /// Match All Methods inside Any Class under Service Package
    @Pointcut("execution(* com.master.Restful_Blog_Api.service.*.*(..))")
    public void serviceMethods(){}


    @Around("controllerMethods()")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        // get method signature
        String methodName = joinPoint.getSignature().toShortString();

        // calculate time execution
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // calls controller method
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.info("[CONTROLLER] {} -> {}ms",
                    methodName, stopWatch.getTotalTimeMillis());
            return result;
        }
        catch(Exception ex) {
            stopWatch.stop();
            log.info("[CONTROLLER] Something Wrong {} -> {}ms | Exception: {}",
                    methodName, stopWatch.getTotalTimeMillis(), ex.getMessage());
            // rethrow exception
            throw ex;
        }
    }

    @Around("serviceMethods()")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        // get method signature
        String methodName = joinPoint.getSignature().toShortString();

        // calculate time execution
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // call service method
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.error("[SERVICE] {} -> {}ms",
                    methodName, stopWatch.getTotalTimeMillis());
            return result;
        }
        catch(Exception ex) {
            stopWatch.stop();
            log.error("[SERVICE] Something Wrong {} -> {}ms | Execution: {}",
                    methodName, stopWatch.getTotalTimeMillis(), ex.getMessage());
            // rethrow exception
            throw ex;
        }
    }


}

package com.aurea.testgenerator.config

import groovy.util.logging.Log4j2
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.util.StopWatch

import java.util.concurrent.ConcurrentHashMap

@Aspect
@Log4j2
class StopWatchAspect {

    Map<String, SynchronizedStopWatch> watches = new ConcurrentHashMap<>()

    @Pointcut(value = "execution(* *(..))")
    void anyPublicMethod() {}

    @Around("anyPublicMethod() && @annotation(ExecutionTime)")
    Object profileExecuteMethod(ProceedingJoinPoint jointPoint) throws Throwable {
        String targetClass = jointPoint.getTarget().getClass().getSimpleName()
        MethodSignature signature = (MethodSignature) jointPoint.getSignature()
        String methodName = signature.toShortString()

        SynchronizedStopWatch stopWatch = watches.computeIfAbsent(targetClass, { new SynchronizedStopWatch() })
        stopWatch.start(methodName)

        Object result = jointPoint.proceed()

        stopWatch.stop()
        log.info("{} executed {} times. Last call: {} ms. Total time: {} ms",
                methodName,
                stopWatch.getTaskCount(),
                stopWatch.getLastTaskTimeMillis(),
                stopWatch.getTotalTimeMillis())

        return result
    }

    static class SynchronizedStopWatch {

        private final StopWatch stopWatch

        SynchronizedStopWatch() {
            this.stopWatch = new StopWatch()
        }

        int getTaskCount() {
            return stopWatch.getTaskCount()
        }

        long getTotalTimeMillis() {
            return stopWatch.getTotalTimeMillis()
        }

        long getLastTaskTimeMillis() {
            return stopWatch.getLastTaskTimeMillis()
        }

        synchronized void start(String methodName) {
            if (!stopWatch.isRunning()) {
                stopWatch.start(methodName)
            }
        }

        synchronized void stop() {
            if (stopWatch.isRunning()) {
                stopWatch.stop()
            }
        }
    }
}
package ru.tpu.hostel.schedules.aspect.amqp;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import ru.tpu.hostel.schedules.exception.ServiceException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.tpu.hostel.schedules.aspect.amqp.Message.FINISH_SERVICE_METHOD_EXECUTION;
import static ru.tpu.hostel.schedules.aspect.amqp.Message.FINISH_SERVICE_METHOD_EXECUTION_WITH_RESULT;
import static ru.tpu.hostel.schedules.aspect.amqp.Message.SERVICE_METHOD_EXECUTION_EXCEPTION;
import static ru.tpu.hostel.schedules.aspect.amqp.Message.START_SERVICE_METHOD_EXECUTION;
import static ru.tpu.hostel.schedules.aspect.amqp.Message.START_SERVICE_METHOD_EXECUTION_WITH_PARAMETERS;
import static ru.tpu.hostel.schedules.utils.TimeNow.getLocalDateTimeStingFromMillis;

/**
 * Аспект для логирования сервисных методов
 */
@Aspect
@Component
@Slf4j
public class ServiceLoggingFilter {

    @Around("execution(public * ru.tpu.hostel.schedules.service..*(..)) && !execution(* *..ApplicationRunner.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class<?> clazz = methodSignature.getDeclaringType();
        String serviceName = clazz.getSimpleName();

        String methodName = methodSignature.getName();
        Method method = clazz.getMethod(methodName, methodSignature.getParameterTypes());
        LogFilter logFilter = getLogFilter(method);

        boolean methodLogging = logFilter == null || logFilter.enableMethodLogging();
        boolean paramsLogging = logFilter == null || logFilter.enableParamsLogging();
        boolean resultLogging = logFilter == null || logFilter.enableResultLogging();

        Parameter[] parameters = method.getParameters();
        Map<String, Object> paramsMap = new LinkedHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getAnnotation(SecretArgument.class) != null) {
                continue;
            }
            paramsMap.put(parameters[i].getName(), joinPoint.getArgs()[i]);
        }

        String args = paramsMap.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(", "));

        logStart(serviceName, methodName, args, methodLogging, paramsLogging);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            logFinish(serviceName, methodName, startTime, result, methodLogging, resultLogging);
            return result;
        } catch (Throwable throwable) {
            logException(serviceName, methodName, startTime, throwable);
            throw throwable;
        }
    }

    private LogFilter getLogFilter(Method method) {
        LogFilter annotation = AnnotationUtils.findAnnotation(method, LogFilter.class);
        return annotation == null
                ? AnnotationUtils.findAnnotation(method.getDeclaringClass(), LogFilter.class)
                : annotation;
    }

    private void logStart(
            String serviceName,
            String methodName,
            String arguments,
            boolean methodLogging,
            boolean paramsLogging
    ) {
        if (methodLogging && paramsLogging) {
            log.info(
                    START_SERVICE_METHOD_EXECUTION_WITH_PARAMETERS,
                    serviceName,
                    methodName,
                    arguments
            );
        } else if (methodLogging) {
            log.info(
                    START_SERVICE_METHOD_EXECUTION,
                    serviceName,
                    methodName
            );
        }
    }

    private void logFinish(
            String serviceName,
            String methodName,
            long startTimeMillis,
            Object result,
            boolean methodLogging,
            boolean resultLogging
    ) {
        long finishTime = System.currentTimeMillis() - startTimeMillis;

        if (methodLogging && resultLogging && result != null) {
            log.info(
                    FINISH_SERVICE_METHOD_EXECUTION_WITH_RESULT,
                    serviceName,
                    methodName,
                    result,
                    finishTime
            );
        } else if (methodLogging) {
            log.info(
                    FINISH_SERVICE_METHOD_EXECUTION,
                    serviceName,
                    methodName,
                    finishTime
            );
        }
    }

    private void logException(
            String serviceName,
            String methodName,
            long startTimeMillis,
            Throwable throwable
    ) {
        long finishTime = System.currentTimeMillis() - startTimeMillis;

        if (throwable instanceof ServiceException serviceException) {
            log.error(
                    SERVICE_METHOD_EXECUTION_EXCEPTION,
                    serviceName,
                    methodName,
                    getLocalDateTimeStingFromMillis(startTimeMillis),
                    finishTime,
                    serviceException.getMessage()
            );
        }
    }

}

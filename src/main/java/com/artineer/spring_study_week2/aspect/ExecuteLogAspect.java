package com.artineer.spring_study_week2.aspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Component
@Aspect
public class ExecuteLogAspect {
    @SuppressWarnings("unchecked")
    @Around(value = "@annotation(ExecuteLog)")
    public <T> Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        // 작업 시작 시간을 구합니다.
        long start = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Class<T> clazzType = this.classType(signature.getMethod().getAnnotations());

        // 위빙된 객체의 작업을 진행합니다.
        final T result = (T) joinPoint.proceed();

        String methodName = signature.getName();
        String input = Arrays.toString(signature.getParameterNames()) + Arrays.toString(joinPoint.getArgs());

        String output = this.toString(result);

        log.info("Method Name : {}, Input : {}, Output : {}, Execute Time : {}", methodName, input, output, (System.currentTimeMillis() - start) + " ms");

        return result;
    }

    private <T> String toString(T result) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(Field field : result.getClass().getDeclaredFields()) {
            if(Strings.isBlank(field.getName())) {
                continue;
            }

            field.setAccessible(true);
            sb.append(field.getName()).append("=").append(field.get(result)).append(", ");
        }
        sb.append("]");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> classType(Annotation[] annotations) throws Throwable {
        Annotation executeLogAnnotation = Arrays.stream(annotations)
                .filter(a -> a.annotationType().getCanonicalName().equals(ExecuteLog.class.getCanonicalName()))
                .findFirst().orElseThrow(() -> new RuntimeException("ExecuteLog Annotation is not existed..."));

        String typeMethodName = "type";
        Method typeMethod = Arrays.stream(executeLogAnnotation.annotationType().getDeclaredMethods())
                .filter(m -> m.getName().equals(typeMethodName))
                .findFirst().orElseThrow(() -> new RuntimeException("type() of ExecuteLog is not existed..."));

        return (Class<T>) typeMethod.invoke(executeLogAnnotation);
    }
}
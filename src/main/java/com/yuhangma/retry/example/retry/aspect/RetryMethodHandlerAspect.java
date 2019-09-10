package com.yuhangma.retry.example.retry.aspect;

import com.yuhangma.retry.example.retry.annotation.Retry;
import com.yuhangma.retry.example.retry.service.AutoRetryService;
import com.yuhangma.retry.example.retry.task.AbstractRetryTask;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 重试方法处理切面
 * Note：对使用了 {@link Retry} 注解的方法进行环绕切面，封装成 {@link AbstractRetryTask} 的子类交给重试 service 进行处理。
 *
 * @author Moore
 * @since 2019/08/30
 */
@Aspect
@Slf4j
@Component
public class RetryMethodHandlerAspect {

    @Around("@annotation(com.yuhangma.retry.example.retry.annotation.Retry)")
    public Object handle(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();

        Retry retry = method.getAnnotation(Retry.class);
        AbstractRetryTask<Object> task = new AbstractRetryTask<Object>(retry, method, 0, LocalDateTime.now()) {
            @Override
            public Object execute() {
                try {
                    return proceedingJoinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return AutoRetryService.executeRetry(task);
    }

}

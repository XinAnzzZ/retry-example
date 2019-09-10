package com.yuhangma.retry.example.retry.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重试任务注解
 * <p>
 * Examples:
 * //    @Retry(value = {OneException.class, TwoException.class},
 * //           interval = {2000, 3000, 4000})
 * //    public void testRetry() {
 * //        do something...
 * //    }
 * 上面的使用示例代表着，当 {@code testRetry()} 方法遇到 {@code OneException} 或者 {@code TwoException} 的时候，
 * 会自动重试三次，重试的时间间隔分别是 2s、3s、4s。
 *
 * @author Moore
 * @since 2019/08/30
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {

    /**
     * 数组元素表示每次重试的间隔，单位毫秒，数组的长度表示重试次数，默认情况下，如果失败，5s 后重试一次。
     */
    int[] interval() default {5000};

    @AliasFor("include")
    Class<? extends Throwable>[] value() default {};

    /**
     * 只有当出现以下异常时进行重试
     */
    @AliasFor("value")
    Class<? extends Throwable>[] include() default {};

    /**
     * 当出现以下异常时不再进行重试
     */
    Class<? extends Throwable>[] exclude() default {};
}

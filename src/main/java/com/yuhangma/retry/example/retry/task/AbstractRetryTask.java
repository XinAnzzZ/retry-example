package com.yuhangma.retry.example.retry.task;

import com.yuhangma.retry.example.retry.annotation.Retry;
import com.yuhangma.retry.example.retry.service.AutoRetryService;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 基础重试任务类，封装重试任务相关信息
 *
 * @author Moore
 * @since 2019/08/30
 */
@Data
@AllArgsConstructor
public abstract class AbstractRetryTask<T> implements Runnable, Comparable<AbstractRetryTask> {

    /**
     * 注解中包含的重试信息
     */
    private Retry retryInfo;

    /**
     * 需要重试的目标方法
     */
    private Method target;

    /**
     * 重试次数，第 N 次
     */
    private int retryTimes;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecuteTime;

    /**
     * 任务执行方法
     *
     * @return the return
     */
    public abstract T execute();

    @Override
    public void run() {
        AutoRetryService.executeRetry(this);
    }

    /**
     * 比较两者执行时间的先后
     *
     * @param other 另外一个任务
     * @return 如果当前任务的下次执行时间先与另外一个，则返回 1，否则返回 -1。
     * 即，如果当前任务的下次执行时间比 the other 早，则优先执行当前任务，否则执行另外一个任务。
     */
    @Override
    public int compareTo(AbstractRetryTask other) {
        return this.nextExecuteTime.isBefore(other.nextExecuteTime) ? 1 : -1;
    }

    public String methodToString() {
        return String.format("%s.%s()", target.getDeclaringClass().getSimpleName(), target.getName());
    }
}

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
 * @author 心安
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
     * @return the result
     */
    public abstract T execute();

    /**
     * 实现了 {@code Runnable#run()} 方法，执行的操作是调用重试，然后将自己传进去。
     * <p>
     * 当次任务执行失败时，会被加入到 {@link AutoRetryService#RETRY_TASK_QUEUE} 队列中，等待被定时任务取出执行。
     * 定时任务会将此任务放到 {@link AutoRetryService} 的线程池中，线程池会调用 task 的 run 方法，也就是此方法，执行重试。
     */
    @Override
    public void run() {
        AutoRetryService.executeRetry(this);
    }

    /**
     * 比较两者执行时间的先后
     *
     * @param other the other task
     * @return 如果当前任务的下次执行时间先与另外一个，则返回 1，否则返回 -1。
     * 即，如果当前任务的下次执行时间比 the other task 早，则优先执行当前任务，否则执行另外一个任务。
     */
    @Override
    public int compareTo(AbstractRetryTask other) {
        return this.nextExecuteTime.isBefore(other.nextExecuteTime) ? 1 : -1;
    }

    /**
     * 获取目标方法格式化后的名称
     *
     * @return the target method's formatted name
     */
    public String getTargetMethodName() {
        return String.format("%s.%s()", target.getDeclaringClass().getSimpleName(), target.getName());
    }
}

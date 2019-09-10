package com.yuhangma.retry.example.retry.service;

import com.yuhangma.retry.example.retry.task.AbstractRetryTask;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Moore
 * @since 2019/08/30
 */
@Slf4j
public class AutoRetryService {

    /**
     * 重试任务队列，执行失败的任务将会加入到此队列中，等待被重试。
     * 这是一个优先队列，泛型是 {@link AbstractRetryTask}。
     * 这个类实现了 {@link Comparable#compareTo(Object)} 方法，比较方式是下次执行时间，
     * 这也就意味着，这个队列中的任务，下次执行时间 {@link AbstractRetryTask#getNextExecuteTime()} 较早的优先执行。
     *
     * @see AbstractRetryTask#compareTo(AbstractRetryTask)
     */
    public static final PriorityBlockingQueue<AbstractRetryTask> RETRY_TASK_QUEUE = new PriorityBlockingQueue<>();

    /*** 核心数 */
    private static final int PROCESSORS_COUNT = Runtime.getRuntime().availableProcessors();

    private static final int CORE_POOL_SIZE = PROCESSORS_COUNT * 2;

    private static final int MAX_POOL_SIZE = PROCESSORS_COUNT * 3;

    private static final long KEEP_ALIVE_TIME = 60L;

    private static final String RETRY_TASK_THREAD_NAME_FORMAT = "retry-task-thread-%d";

    private static final LinkedBlockingDeque<Runnable> WORKER_QUEUE = new LinkedBlockingDeque<>(1024);

    /*** 重试任务线程池 */
    private static ExecutorService retryTaskExecutor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            WORKER_QUEUE,
            threadFactory(),
            rejectedExecutionHandler()
    );

    public static <T> T executeRetry(AbstractRetryTask<T> task) {
        T result = null;
        int[] interval = task.getRetryInfo().interval();
        long start = System.currentTimeMillis();

        try {
            result = task.execute();
            log.info(String.format("{%s} 方法第 {%s} 次重试执行成功 ,耗时 {%s}",
                    task.getMethodName(), task.getRetryTimes(), getInterval(start)));
        } catch (RuntimeException e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            Class<? extends Throwable> exClass = cause.getClass();

            int retryTimes = task.getRetryTimes();
            // 重试次数大于等于注解中标明的重试次数，结束重试
            if (retryTimes >= task.getRetryInfo().interval().length) {
                log.error(String.format("{%s} 方法重试次数达到上限 {%s} 次，结束重试。Cause: %s(%s)",
                        task.getMethodName(),
                        retryTimes,
                        exClass.getSimpleName(),
                        cause.getMessage()));
                throw e;
            }

            Set<Class<? extends Throwable>> includeExceptionClasses = new HashSet<>(
                    Arrays.asList(task.getRetryInfo().include()));
            Set<Class<? extends Throwable>> excludeExceptionClasses = new HashSet<>(
                    Arrays.asList(task.getRetryInfo().exclude()));

            excludeExceptionClasses.forEach(excludeExceptionClass -> {
                if (exClass.isAssignableFrom(excludeExceptionClass)) {
                    log.error(String.format("{%s} 方法第 {%s} 次重试出现 exclude 异常：{%s} ,耗时 {%s}",
                            task.getMethodName(),
                            retryTimes,
                            exClass.getSimpleName(),
                            getInterval(start)));
                    throw e;
                }
            });

            boolean notMatch = true;
            for (Class<? extends Throwable> includeExceptionClass : includeExceptionClasses) {
                // 如果是需要重试的异常类型，将任务加入到队列
                if (exClass.isAssignableFrom(includeExceptionClass)) {
                    // 改任务已被匹配，加入到重试队列
                    notMatch = false;
                    // 重试次数 + 1
                    task.setRetryTimes(retryTimes + 1);
                    // 下次执行时间等于当前时间 + 第 n 次重试的间隔时间
                    task.setNextExecuteTime(LocalDateTime.now().plusNanos(interval[retryTimes] * 1_000_000L));
                    RETRY_TASK_QUEUE.add(task);
                    break;
                }
            }

            if (notMatch) {
                throw e;
            }
        }

        return result;
    }

    /**
     * 获取方法耗时
     */
    private static long getInterval(long start) {
        return System.currentTimeMillis() - start;
    }

    /*** 线程名称自增数字 */
    private static final AtomicLong COUNT = new AtomicLong(0L);

    /**
     * 根据传入的名称格式创建一个自定义名称的线程工厂
     * <p>
     * 示例：
     * //      AutoRetryService.threadFactory("my-thread-d%", COUNT);
     * 则创建的线程名字为：
     * //      my-thread-0
     * //      my-thread-1
     * //      my-thread-3
     * //      ...
     *
     * @return 线程工厂
     */
    private static ThreadFactory threadFactory() {
        return runnable -> new Thread(runnable, String.format(RETRY_TASK_THREAD_NAME_FORMAT, COUNT.getAndIncrement()));
    }

    /**
     * 拒绝策略：打印日志，记录数据
     *
     * @return 拒绝策略
     */
    private static RejectedExecutionHandler rejectedExecutionHandler() {
        return (runnable, executor) -> {
            // 将被拒绝的任务入库，或者其他操作。
            log.error("Task " + runnable.toString() + " rejected from " + executor.toString());
        };
    }

    /**
     * 提交任务
     *
     * @param task 重试的任务
     */
    public static void submitTask(AbstractRetryTask task) {
        retryTaskExecutor.execute(task);
    }
}

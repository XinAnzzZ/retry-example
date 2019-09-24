package com.yuhangma.retry.example.job;

import com.yuhangma.retry.example.retry.service.AutoRetryService;
import com.yuhangma.retry.example.retry.task.AbstractRetryTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author 心安
 * @since 2019/09/06
 */
@Slf4j
@Component
public class RetryTaskJobService {

    private static final int MAX_QUEUE_SIZE = 500;

    @Scheduled(fixedDelay = 500L)
    public void takeRetrySchedule() {
        try {
            AbstractRetryTask task = AutoRetryService.RETRY_TASK_QUEUE.take();
            // 如果当前时间还没到任务的下次执行时间，并且队列容量还未到达最大值，则可以重试，否则，放回队列。
            boolean canRetry = task.getNextExecuteTime().isBefore(LocalDateTime.now())
                    && AutoRetryService.RETRY_TASK_QUEUE.size() < MAX_QUEUE_SIZE;
            if (canRetry) {
                AutoRetryService.submitTask(task);
            } else {
                AutoRetryService.RETRY_TASK_QUEUE.add(task);
            }
        } catch (InterruptedException e) {
            log.error("重试定时任务出现异常!", e);
        }
    }
}

package com.yuhangma.retry.example.service;

import com.yuhangma.retry.example.exception.OneException;
import com.yuhangma.retry.example.exception.ThreeException;
import com.yuhangma.retry.example.exception.TwoException;
import com.yuhangma.retry.example.retry.annotation.Retry;
import org.springframework.stereotype.Service;

/**
 * @author 心安
 * @since 2019/08/30
 */
@Service
public class UserService {

    public void testRetry(Integer retryTimes) {
        switch (retryTimes) {
            case 1:
                retryOneTimes();
                break;
            case 2:
                retryTwoTimes();
                break;
            case 3:
                retryThreeTimes();
                break;
            default:
                dontRetry();
                break;
        }
    }

    @Retry(value = OneException.class, interval = {1000})
    private void retryOneTimes() {
        // 随机抛出异常
        if (System.currentTimeMillis() % 3 == 0) {
            throw new OneException();
        }
    }

    @Retry(value = OneException.class, interval = {1000, 5000})
    private void retryTwoTimes() {

    }

    @Retry(value = {OneException.class, TwoException.class, ThreeException.class},
            interval = {2000, 3000, 4000})
    private void retryThreeTimes() {

    }

    private void dontRetry() {

    }
}

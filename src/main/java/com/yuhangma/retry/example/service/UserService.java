package com.yuhangma.retry.example.service;

import com.yuhangma.retry.example.exception.OneException;
import com.yuhangma.retry.example.exception.ThreeException;
import com.yuhangma.retry.example.exception.TwoException;
import com.yuhangma.retry.example.retry.annotation.Retry;
import org.springframework.stereotype.Service;

/**
 * @author Moore
 * @since 2019/08/30
 */
@Service
public class UserService {

    @Retry(value = {OneException.class, TwoException.class, ThreeException.class},
            interval = {2000, 3000, 4000})
    public void testRetry() {

    }
}

package com.yuhangma.retry.example;

import com.yuhangma.retry.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

/**
 * @author 心安
 * @since 2019/08/30
 */
@RestController
@RequestMapping(produces = APPLICATION_JSON_UTF8_VALUE)
@SpringBootApplication
public class RetryExampleApplication {

    private UserService userService;

    @Autowired
    public RetryExampleApplication(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{retryTimes}")
    public void test(@PathVariable Integer retryTimes) {
        userService.testRetry(retryTimes);
    }

    public static void main(String[] args) {
        SpringApplication.run(RetryExampleApplication.class, args);
    }

}

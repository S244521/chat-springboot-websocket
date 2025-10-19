package com.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    @Bean(name = "fileUploadExecutor")
    public ExecutorService fileUploadExecutor() {
        return new ThreadPoolExecutor(
                5,           //  核心线程数
                20,                     //  最大线程数
                60,                     //  线程空闲时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),    // 任务队列
                new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
        );
    }
}

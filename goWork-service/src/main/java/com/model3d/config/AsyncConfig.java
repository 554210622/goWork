package com.model3d.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 3D模型生成异步任务线程池
     */
    @Bean("model3dTaskExecutor")
    public Executor model3dTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：同时处理的3D生成任务数
        executor.setCorePoolSize(5);
        
        // 最大线程数：高峰期最多处理的任务数
        executor.setMaxPoolSize(10);
        
        // 队列容量：等待队列中的任务数
        executor.setQueueCapacity(50);
        
        // 线程名前缀
        executor.setThreadNamePrefix("Model3D-Async-");
        
        // 拒绝策略：队列满时让调用者线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);
        
        // 等待任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("3D模型生成异步线程池初始化完成: 核心线程数={}, 最大线程数={}, 队列容量={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}
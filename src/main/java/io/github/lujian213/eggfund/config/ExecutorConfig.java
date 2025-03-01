package io.github.lujian213.eggfund.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ExecutorConfig {
    @Value("${fundvalue.retrieve.threadpool.size}")
    private int fundValueRetrievePoolSize;

    @Bean(name = "fundValueRetrieveExecutor")
    public Executor fundValueRetrieveExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(fundValueRetrievePoolSize);
        taskExecutor.setMaxPoolSize(fundValueRetrievePoolSize);
        taskExecutor.setQueueCapacity(Integer.MAX_VALUE);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        taskExecutor.setThreadNamePrefix("fundValueRetrieve-");
        taskExecutor.setThreadGroupName("fundValueRetrieve");
        taskExecutor.initialize();
        return taskExecutor;
    }
}
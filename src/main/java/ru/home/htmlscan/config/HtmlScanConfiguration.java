package ru.home.htmlscan.config;

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAsync
@EnableScheduling
public class HtmlScanConfiguration {
    private final static int corePoolSize = 10, maxPoolSize = 10, queueCapacity = 600;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name="htmlExecutor")
    public TaskExecutor htmlExecutor() {
        val taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("html-");
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Bean(name="mailExecutor")
    public TaskExecutor mailExecutor() {
        val taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("mail-");
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.initialize();
        return taskExecutor;
    }
}

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
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean(name="htmlExecutor")
    public TaskExecutor htmlExecutor() {
        val taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("html-");
        taskExecutor.setCorePoolSize(3);
        taskExecutor.setMaxPoolSize(3);
        taskExecutor.setQueueCapacity(600);
        taskExecutor.afterPropertiesSet();
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean(name="mailExecutor")
    public TaskExecutor mailExecutor() {
        val taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("mail-");
        taskExecutor.setCorePoolSize(3);
        taskExecutor.setMaxPoolSize(3);
        taskExecutor.setQueueCapacity(600);
        taskExecutor.afterPropertiesSet();
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }
}

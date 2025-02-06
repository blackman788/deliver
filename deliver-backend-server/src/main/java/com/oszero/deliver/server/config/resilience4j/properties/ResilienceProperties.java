package com.oszero.deliver.server.config.resilience4j.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "resilience")
public class ResilienceProperties {
    
    private Map<String, CircuitBreakerProps> circuitBreaker = new HashMap<>();
    private Map<String, RateLimiterProps> rateLimiter = new HashMap<>();
    private Map<String, TimeLimiterProps> timeLimiter = new HashMap<>();

    @Data
    public static class CircuitBreakerProps {
        private int failureRateThreshold = 50;     // 失败率阈值(%)
        private int slidingWindowSize = 10;        // 滑动窗口大小
        private int waitDurationSeconds = 10;      // 等待时间(秒)
        private int minimumNumberOfCalls = 5;      // 最小调用次数
        private int slowCallSeconds = 2;           // 慢调用时间阈值(秒)
        private float slowCallRateThreshold = 50;  // 慢调用比例阈值(%)
    }

    @Data
    public static class RateLimiterProps {
        private int limitForPeriod = 50;            // 限流数(每秒请求数)
        private int limitRefreshPeriodSeconds = 1;  // 限流刷新周期(秒)
        private int timeoutMillis = 500;           // 等待超时时间(毫秒)
    }

    @Data
    public static class TimeLimiterProps {
        private int timeoutSeconds = 5;           // 超时时间(秒)
        private boolean cancelRunningFuture = true; // 是否取消运行的Future
    }
} 
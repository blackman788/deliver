package com.oszero.deliver.server.config.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.Getter;
import lombok.AllArgsConstructor;
import java.time.Duration;

@Getter
@AllArgsConstructor
public enum CircuitBreakerProperties {
    
    MAIL(50, 10, 10, 5, 2, 50.0f),  // 失败率阈值,滑动窗口,等待时间,最小调用,慢调用阈值(秒),慢调用比例
    SMS(40, 10, 5, 5, 1, 40.0f),
    CALL(50, 10, 5, 5, 1, 50.0f),
    DING(50, 10, 5, 5, 2, 50.0f),
    WECHAT(50, 10, 5, 5, 2, 50.0f),
    FEISHU(50, 10, 5, 5, 2, 50.0f);

    private final int failureRateThreshold;     // 失败率阈值(%)
    private final int slidingWindowSize;        // 滑动窗口大小
    private final int waitDurationSeconds;      // 等待时间(秒)
    private final int minimumNumberOfCalls;     // 最小调用次数
    private final int slowCallSeconds;          // 慢调用时间阈值(秒)
    private final float slowCallRateThreshold;  // 慢调用比例阈值(%)

    public int getPermittedNumberOfCallsInHalfOpenState() {
        return Math.max(1, slidingWindowSize / 2);
    }

    public CircuitBreakerConfig toConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofSeconds(waitDurationSeconds))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(slidingWindowSize)
                .minimumNumberOfCalls(minimumNumberOfCalls)
                .permittedNumberOfCallsInHalfOpenState(getPermittedNumberOfCallsInHalfOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(RuntimeException.class, Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .slowCallRateThreshold(slowCallRateThreshold)
                .slowCallDurationThreshold(Duration.ofSeconds(slowCallSeconds))
                .build();
    }
} 
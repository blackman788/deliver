package com.oszero.deliver.server.config.resilience4j;


import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum RateLimiterProperties {
    
    MAIL(100, 1, 500),  // 限流数,刷新周期(秒),超时(毫秒)
    SMS(50, 1, 500),
    CALL(20, 1, 500),
    DING(20, 1, 500),
    WECHAT(20, 1, 500),
    FEISHU(20, 1, 500);

    private final int limitForPeriod;           // 限流数(每秒请求数)
    private final int limitRefreshPeriodSeconds; // 限流刷新周期(秒)
    private final int timeoutMillis;            // 等待超时时间(毫秒)


} 
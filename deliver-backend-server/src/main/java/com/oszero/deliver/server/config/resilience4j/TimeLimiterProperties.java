package com.oszero.deliver.server.config.resilience4j;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum TimeLimiterProperties {
    
    MAIL(10, true),  // 超时时间(秒),是否取消运行的Future
    SMS(5, true),
    CALL(5, true),
    DING(5, true),
    WECHAT(5, true),
    FEISHU(5, true);

    private final int timeoutSeconds;           // 超时时间(秒)
    private final boolean cancelRunningFuture;  // 是否取消运行的Future
} 
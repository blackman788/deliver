package com.oszero.deliver.admin.model.dto.request.FlowContorlConfig;

import lombok.Data;

/**
 * @author: Black788
 * @date: 2025/2/2 20:38
 */
@Data
public class FlowControlConfigDTO {

    private RateLimiterConfigDTO rateLimiter;

    private CircuitBreakerConfigDTO circuitBreaker;

    private TimeLimiterConfigDTO timeLimiter;

    private Integer code;
}

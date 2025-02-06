package com.oszero.deliver.admin.model.dto.request.FlowContorlConfig;

import lombok.Data;

/**
 * @author: Black788
 * @date: 2025/2/2 20:39
 */
@Data
public class CircuitBreakerConfigDTO {

    private String failureRateThreshold;
    private String slowCallRateThreshold;
    private String waitDurationSeconds;
    private String slowCallSeconds;
    private String minimumNumberOfCalls;
    private String slidingWindowSize;

}

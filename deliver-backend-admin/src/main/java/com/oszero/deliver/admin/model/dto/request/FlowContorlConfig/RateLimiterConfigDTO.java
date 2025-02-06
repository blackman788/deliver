package com.oszero.deliver.admin.model.dto.request.FlowContorlConfig;

import lombok.Data;

/**
 * @author: Black788
 * @date: 2025/2/2 20:39
 */
@Data
public class RateLimiterConfigDTO {

    private String limitForPeriod;

    private String limitRefreshPeriodSeconds;

    private String timeoutMillis;

}

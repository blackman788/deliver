package com.oszero.deliver.admin.model.dto.request.FlowContorlConfig;

import lombok.Data;

/**
 * @author: Black788
 * @date: 2025/2/2 20:40
 */
@Data
public class TimeLimiterConfigDTO {

    private boolean cancelRunningFuture;

    private String timeoutSeconds;
}

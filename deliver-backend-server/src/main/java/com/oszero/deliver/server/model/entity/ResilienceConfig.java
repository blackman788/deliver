package com.oszero.deliver.server.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * Resilience4j配置表
 * </p>
 *
 * @author black788
 * @since 2025-02-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("resilience_config")
public class ResilienceConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 渠道编码,参考ChannelTypeEnum
     */
    private Integer channelCode;

    /**
     * 配置类型:circuit_breaker, rate_limiter, time_limiter
     */
    private String configType;

    /**
     * 配置项的键
     */
    private String configKey;

    /**
     * 配置项的值
     */
    private String configValue;

    /**
     * 创建时间
     */
    private String createdAt;

    /**
     * 更新时间
     */
    private String updatedAt;


}

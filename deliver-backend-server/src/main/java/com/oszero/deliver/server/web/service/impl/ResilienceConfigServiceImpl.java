package com.oszero.deliver.server.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.oszero.deliver.server.model.entity.ResilienceConfig;

import com.oszero.deliver.server.web.mapper.ResilienceConfigMapper;
import com.oszero.deliver.server.web.service.ResilienceConfigService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Resilience4j配置表 服务实现类
 * </p>
 *
 * @author black788
 * @since 2025-02-01
 */
@Service
public class ResilienceConfigServiceImpl extends ServiceImpl<ResilienceConfigMapper, ResilienceConfig> implements ResilienceConfigService {

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oszero.deliver.admin.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.oszero.deliver.admin.enums.ChannelTypeEnum;
import com.oszero.deliver.admin.exception.BusinessException;
import com.oszero.deliver.admin.model.common.CommonResult;
import com.oszero.deliver.admin.model.dto.request.FlowContorlConfig.FlowControlConfigDTO;
import com.oszero.deliver.admin.model.dto.request.FlowControlConfigByCodeDto;
import com.oszero.deliver.admin.model.dto.request.SendTestRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 流控规则控制器
 *
 * @author black788
 * @version 1.0.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/flowControl")
@RequiredArgsConstructor
public class FlowControlController {

    private final StringRedisTemplate redisTemplate;

    @Value("${deliver-server.update_config_url}")
    private String updateConfigUrl;

    /**
     * 根据 ChannelTypeEnum 中的code获取对应的name
     * 根据 name 拼接 resilience:config: 去redis中获取
     */
    @PostMapping("/getFlowControlConfigByCode")
    public CommonResult<JSONObject> getFlowControlConfigByCode(@RequestBody FlowControlConfigByCodeDto dto) {
        String name = ChannelTypeEnum.getInstanceByCode(dto.getCode()).getName();
        String cacheKey = "resilience:config:" + name;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            String value = redisTemplate.opsForValue().get(cacheKey);
            return CommonResult.success(JSONUtil.parseObj(value));
        }
        return CommonResult.fail();
    }

    @PostMapping("/updateFlowControlConfig")
    public CommonResult<JSONObject> updateFlowControlConfigByCode(@RequestBody FlowControlConfigDTO dto) {
        try{
            String name = ChannelTypeEnum.getInstanceByCode(dto.getCode()).getName();
            String cacheKey = "resilience:config:" + name;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
                redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(dto),24, TimeUnit.HOURS);
                SendUpdateConfigState(dto.getCode());
                return CommonResult.success();
            }
        }catch (Exception e){
            log.error("更新配置失败", e);
        }
        return CommonResult.fail();
    }

    public void SendUpdateConfigState(int code) {
        CommonResult<?> commonResult;
        try (HttpResponse response = HttpRequest.post(updateConfigUrl+"/"+code).execute()) {
            commonResult = JSONUtil.toBean(response.body(), CommonResult.class);
        } catch (Exception e) {
            throw new BusinessException("测试发送接口失败，服务端异常，请检查！！！");
        }
        if (Objects.isNull(commonResult)) {
            throw new BusinessException("测试发送接口失败，服务端异常，请检查！！！");
        }
        if (!commonResult.getCode().equals(200)) {
            throw new BusinessException(commonResult.getErrorMessage());
        }
    }

    /**
     * 获取指定渠道的监控指标数据
     */
    @PostMapping("/getMetrics/{code}")
    public CommonResult<JSONObject> getMetrics(@PathVariable Integer code) {
        try {
            String channelName = ChannelTypeEnum.getInstanceByCode(code).getName();
            
            JSONObject metricsData = new JSONObject();
            
            // 获取断路器指标
            String circuitBreakerKey = String.format("resilience:metrics:%s:circuit_breaker", channelName);
            Map<Object, Object> circuitBreakerMap = redisTemplate.opsForHash().entries(circuitBreakerKey);
            if (!circuitBreakerMap.isEmpty()) {
                JSONObject circuitBreakerMetrics = new JSONObject();
                circuitBreakerMetrics.set("failureRate", circuitBreakerMap.get("failureRate"));
                circuitBreakerMetrics.set("slowCallRate", circuitBreakerMap.get("slowCallRate"));
                circuitBreakerMetrics.set("numberOfFailedCalls", circuitBreakerMap.get("numberOfFailedCalls"));
                circuitBreakerMetrics.set("numberOfSlowCalls", circuitBreakerMap.get("numberOfSlowCalls"));
                circuitBreakerMetrics.set("numberOfSuccessfulCalls", circuitBreakerMap.get("numberOfSuccessfulCalls"));
                circuitBreakerMetrics.set("state", circuitBreakerMap.get("state"));
                metricsData.set("circuitBreaker", circuitBreakerMetrics);
            }
            
            // 获取限流器指标
            String rateLimiterKey = String.format("resilience:metrics:%s:rate_limiter", channelName);
            Map<Object, Object> rateLimiterMap = redisTemplate.opsForHash().entries(rateLimiterKey);
            if (!rateLimiterMap.isEmpty()) {
                JSONObject rateLimiterMetrics = new JSONObject();
                rateLimiterMetrics.set("availablePermissions", rateLimiterMap.get("availablePermissions"));
                rateLimiterMetrics.set("numberOfWaitingThreads", rateLimiterMap.get("numberOfWaitingThreads"));
                metricsData.set("rateLimiter", rateLimiterMetrics);
            }
            
            // 获取超时器指标
            String timeLimiterKey = String.format("resilience:metrics:%s:time_limiter", channelName);
            Map<Object, Object> timeLimiterMap = redisTemplate.opsForHash().entries(timeLimiterKey);
            if (!timeLimiterMap.isEmpty()) {
                JSONObject timeLimiterMetrics = new JSONObject();
                timeLimiterMetrics.set("totalCount", timeLimiterMap.get("totalCount"));
                timeLimiterMetrics.set("avgResponseTime", timeLimiterMap.get("avgResponseTime"));
                timeLimiterMetrics.set("p95ResponseTime", timeLimiterMap.get("p95ResponseTime"));
                timeLimiterMetrics.set("p99ResponseTime", timeLimiterMap.get("p99ResponseTime"));
                timeLimiterMetrics.set("maxResponseTime", timeLimiterMap.get("maxResponseTime"));
                metricsData.set("timeLimiter", timeLimiterMetrics);
            }
            
            // 获取异常统计指标
            String exceptionsKey = String.format("resilience:metrics:%s:exceptions", channelName);
            Map<Object, Object> exceptionsMap = redisTemplate.opsForHash().entries(exceptionsKey);
            if (!exceptionsMap.isEmpty()) {
                JSONObject exceptionsMetrics = new JSONObject();
                exceptionsMap.forEach((key, value) -> {
                    if (!"total".equals(key)) { // 排除total计数
                        exceptionsMetrics.set(key.toString(), value);
                    }
                });
                metricsData.set("exceptions", exceptionsMetrics);
            }
            
            return CommonResult.success(metricsData);
        } catch (Exception e) {
            log.error("Failed to get metrics for channel code: {}", code, e);
            return CommonResult.fail();
        }
    }

}

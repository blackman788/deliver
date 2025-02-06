package com.oszero.deliver.server.config.resilience4j;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oszero.deliver.server.config.resilience4j.properties.ResilienceProperties;
import com.oszero.deliver.server.enums.ChannelTypeEnum;
import com.oszero.deliver.server.model.entity.ResilienceConfig;
import com.oszero.deliver.server.web.mapper.ResilienceConfigMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import com.alibaba.fastjson.JSONObject;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResilienceConfigManager {
    
    private final ResilienceConfigMapper configMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, CircuitBreakerRegistry> circuitBreakerRegistries;
    private final Map<String, RateLimiterRegistry> rateLimiterRegistries;
    private final Map<String, TimeLimiterRegistry> timeLimiterRegistries;
    private final ResilienceProperties resilienceProperties;
    
    private static final String CONFIG_CACHE_KEY_PREFIX = "resilience:config:";
    private static final long CONFIG_CACHE_TIMEOUT = 24 * 60 * 60; // 24小时
    
    private static final String CIRCUIT_BREAKER_KEY = "resilience:circuit_breaker:%s";
    private static final String RATE_LIMITER_KEY = "resilience:rate_limiter:%s";
    private static final String TIME_LIMITER_KEY = "resilience:time_limiter:%s";

    @PostConstruct
    public void loadConfigToCache() {
        log.info("Loading resilience config to cache...");
        
        for (ChannelTypeEnum channel : ChannelTypeEnum.values()) {
            String cacheKey = CONFIG_CACHE_KEY_PREFIX + channel.getName();
            
            Map<String, Object> configMap = new HashMap<>();
            
            // 加载熔断器配置
            List<ResilienceConfig> circuitBreakerConfigs = 
                    configMapper.selectByChannelAndType(channel.getCode(), "circuit_breaker");
            if (circuitBreakerConfigs.isEmpty()) {
                // 数据库中没有配置,使用默认配置并入库
                CircuitBreakerConfig defaultConfig = getDefaultCircuitBreakerConfig(channel);
                Map<String, Object> defaultConfigMap = convertCircuitBreakerConfigToMap(defaultConfig);
                configMap.put("circuitBreaker", defaultConfigMap);
                saveConfigToDb(channel.getCode(), "circuit_breaker", defaultConfigMap);
            } else {
                Map<String, String> circuitBreakerConfigMap = circuitBreakerConfigs.stream()
                        .collect(Collectors.toMap(ResilienceConfig::getConfigKey, ResilienceConfig::getConfigValue));
                configMap.put("circuitBreaker", circuitBreakerConfigMap);
            }
            
            // 加载限流器配置
            List<ResilienceConfig> rateLimiterConfigs =
                    configMapper.selectByChannelAndType(channel.getCode(), "rate_limiter");
            if (rateLimiterConfigs.isEmpty()) {
                // 数据库中没有配置,使用默认配置并入库  
                RateLimiterConfig defaultConfig = getDefaultRateLimiterConfig(channel);
                Map<String, Object> defaultConfigMap = convertRateLimiterConfigToMap(defaultConfig);
                configMap.put("rateLimiter", defaultConfigMap);
                saveConfigToDb(channel.getCode(), "rate_limiter", defaultConfigMap);
            } else {
                Map<String, String> rateLimiterConfigMap = rateLimiterConfigs.stream()
                        .collect(Collectors.toMap(ResilienceConfig::getConfigKey, ResilienceConfig::getConfigValue));
                configMap.put("rateLimiter", rateLimiterConfigMap);
            }
            
            // 加载超时器配置
            List<ResilienceConfig> timeLimiterConfigs =
                    configMapper.selectByChannelAndType(channel.getCode(), "time_limiter");
            Map<String, Object> timeLimiterConfigMap;
            if (timeLimiterConfigs.isEmpty()) {
                // 数据库中没有配置,使用默认配置并入库
                TimeLimiterConfig defaultConfig = getDefaultTimeLimiterConfig(channel);
                timeLimiterConfigMap = convertTimeLimiterConfigToMap(defaultConfig);
                saveConfigToDb(channel.getCode(), "time_limiter", timeLimiterConfigMap);
            } else {
                timeLimiterConfigMap = timeLimiterConfigs.stream()
                        .collect(Collectors.toMap(ResilienceConfig::getConfigKey, ResilienceConfig::getConfigValue));
            }
            if(timeLimiterConfigMap.containsKey("cancelRunningFuture")){
                timeLimiterConfigMap.put("cancelRunningFuture", Boolean.parseBoolean((String) timeLimiterConfigMap.get("cancelRunningFuture")));
            }
            configMap.put("timeLimiter", timeLimiterConfigMap);

            // 将配置存入Redis缓存,超时时间为24小时
            redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(configMap), CONFIG_CACHE_TIMEOUT, TimeUnit.SECONDS);
        }
        
        log.info("Resilience config loaded to cache.");
    }

    // 将配置保存到数据库
    private void saveConfigToDb(Integer channelCode, String configType, Map<String, Object> configMap) {
        configMap.forEach((key, value) -> {
            ResilienceConfig config = new ResilienceConfig();
            config.setChannelCode(channelCode);
            config.setConfigType(configType);
            config.setConfigKey(key);
            config.setConfigValue(String.valueOf(value));
            configMapper.insert(config);
        });
    }
    
    // 将CircuitBreakerConfig转换为Map
    private Map<String, Object> convertCircuitBreakerConfigToMap(CircuitBreakerConfig config) {
        Map<String, Object> map = new HashMap<>();
        map.put("failureRateThreshold", String.valueOf(config.getFailureRateThreshold()));
        map.put("slidingWindowSize", String.valueOf(config.getSlidingWindowSize()));
        map.put("minimumNumberOfCalls", String.valueOf(config.getMinimumNumberOfCalls()));
        map.put("waitDurationSeconds", String.valueOf(config.getWaitDurationInOpenState().getSeconds()));
        map.put("slowCallRateThreshold", String.valueOf(config.getSlowCallRateThreshold()));
        map.put("slowCallSeconds", String.valueOf(config.getSlowCallDurationThreshold().getSeconds()));
        return map;
    }
    
    // 将RateLimiterConfig转换为Map
    private Map<String, Object> convertRateLimiterConfigToMap(RateLimiterConfig config) {
        Map<String, Object> map = new HashMap<>();
        map.put("limitForPeriod", String.valueOf(config.getLimitForPeriod()));
        map.put("limitRefreshPeriodSeconds", String.valueOf(config.getLimitRefreshPeriod().getSeconds()));
        map.put("timeoutMillis", String.valueOf(config.getTimeoutDuration().toMillis()));
        return map;
    }
    
    // 将TimeLimiterConfig转换为Map  
    private Map<String, Object> convertTimeLimiterConfigToMap(TimeLimiterConfig config) {
        Map<String, Object> map = new HashMap<>();
        map.put("timeoutSeconds", String.valueOf(config.getTimeoutDuration().getSeconds()));
        map.put("cancelRunningFuture", String.valueOf(config.shouldCancelRunningFuture()));
        return map;
    }

    // CircuitBreaker配置获取
    public CircuitBreakerConfig getCircuitBreakerConfig(Integer channelCode) {
        try {
            ChannelTypeEnum channel = ChannelTypeEnum.getInstanceByCode(channelCode);
            String cacheKey = CONFIG_CACHE_KEY_PREFIX + channel.getName();
            
            String configJson = redisTemplate.opsForValue().get(cacheKey);
            if (configJson != null) {
                JSONObject configMap = JSON.parseObject(configJson);
                JSONObject circuitBreakerMap = configMap.getJSONObject("circuitBreaker");
                
                CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom();
                for (Map.Entry<String, Object> entry : circuitBreakerMap.entrySet()) {
                    String configKey = entry.getKey();
                    String configValue = entry.getValue().toString();
                    switch (configKey) {
                        case "failureRateThreshold":
                            builder.failureRateThreshold(Float.parseFloat(configValue));
                            break;
                        case "slidingWindowSize":
                            builder.slidingWindowSize(Integer.parseInt(configValue));
                            break;
                        case "minimumNumberOfCalls":
                            builder.minimumNumberOfCalls(Integer.parseInt(configValue));
                            break;
                        case "waitDurationSeconds":
                            builder.waitDurationInOpenState(Duration.ofSeconds(Long.parseLong(configValue)));
                            break;
                        case "slowCallRateThreshold":
                            builder.slowCallRateThreshold(Float.parseFloat(configValue));
                            break;
                        case "slowCallSeconds":
                            builder.slowCallDurationThreshold(Duration.ofSeconds(Long.parseLong(configValue)));
                            break;
                    }
                }
                
                // 设置默认值
                builder.slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                        .permittedNumberOfCallsInHalfOpenState(circuitBreakerMap.getIntValue("slidingWindowSize") / 2)
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .recordExceptions(RuntimeException.class, Exception.class)
                        .ignoreExceptions(IllegalArgumentException.class);
                
                return builder.build();
            }
        } catch (Exception e) {
            log.error("Failed to get circuit breaker config for channel: {}", channelCode, e);
        }
        return CircuitBreakerConfig.ofDefaults();
    }

    // RateLimiter配置获取
    public RateLimiterConfig getRateLimiterConfig(Integer channelCode) {
        try {
            ChannelTypeEnum channel = ChannelTypeEnum.getInstanceByCode(channelCode);
            String cacheKey = CONFIG_CACHE_KEY_PREFIX + channel.getName();
            
            String configJson = redisTemplate.opsForValue().get(cacheKey);
            if (configJson != null) {
                JSONObject configMap = JSON.parseObject(configJson);
                JSONObject rateLimiterMap = configMap.getJSONObject("rateLimiter");
                
                RateLimiterConfig.Builder builder = RateLimiterConfig.custom();
                for (Map.Entry<String, Object> entry : rateLimiterMap.entrySet()) {
                    String configKey = entry.getKey();
                    String configValue = entry.getValue().toString();
                    switch (configKey) {
                        case "limitForPeriod":
                            builder.limitForPeriod(Integer.parseInt(configValue));
                            break;
                        case "limitRefreshPeriodSeconds":
                            builder.limitRefreshPeriod(Duration.ofSeconds(Long.parseLong(configValue)));
                            break;
                        case "timeoutMillis":
                            builder.timeoutDuration(Duration.ofMillis(Long.parseLong(configValue)));
                            break;
                    }
                }
                
                return builder.build();
            }
        } catch (Exception e) {
            log.error("Failed to get rate limiter config for channel: {}", channelCode, e);
        }
        return RateLimiterConfig.ofDefaults();
    }

    // TimeLimiter配置获取
    public TimeLimiterConfig getTimeLimiterConfig(Integer channelCode) {
        try {
            ChannelTypeEnum channel = ChannelTypeEnum.getInstanceByCode(channelCode);
            String cacheKey = CONFIG_CACHE_KEY_PREFIX + channel.getName();
            
            String configJson = redisTemplate.opsForValue().get(cacheKey);
            if (configJson != null) {
                JSONObject configMap = JSON.parseObject(configJson);
                JSONObject timeLimiterMap = configMap.getJSONObject("timeLimiter");
                
                TimeLimiterConfig.Builder builder = TimeLimiterConfig.custom();
                for (Map.Entry<String, Object> entry : timeLimiterMap.entrySet()) {
                    String configKey = entry.getKey();
                    String configValue = entry.getValue().toString();
                    switch (configKey) {
                        case "timeoutSeconds":
                            builder.timeoutDuration(Duration.ofSeconds(Long.parseLong(configValue)));
                            break;
                        case "cancelRunningFuture":
                            builder.cancelRunningFuture(Boolean.parseBoolean(configValue));
                            break;
                    }
                }
                
                return builder.build();
            }
        } catch (Exception e) {
            log.error("Failed to get time limiter config for channel: {}", channelCode, e);
        }
        return TimeLimiterConfig.ofDefaults();
    }

    /**
     * 更新指定渠道的Resilience4j配置
     */
    public void updateResilienceConfig(Integer channelCode) {
        try {
            ChannelTypeEnum channel = ChannelTypeEnum.getInstanceByCode(channelCode);
            String cacheKey = CONFIG_CACHE_KEY_PREFIX + channel.getName();
            
            String configJson = redisTemplate.opsForValue().get(cacheKey);

            JSONObject configMap = JSON.parseObject(configJson);
            
            // 1. 更新数据库配置
            // 更新熔断器配置
            JSONObject circuitBreakerMap = configMap.getJSONObject("circuitBreaker");
            updateConfigInDb(channelCode, "circuit_breaker", circuitBreakerMap);
            
            // 更新限流器配置
            JSONObject rateLimiterMap = configMap.getJSONObject("rateLimiter");
            updateConfigInDb(channelCode, "rate_limiter", rateLimiterMap);
            
            // 更新超时器配置
            JSONObject timeLimiterMap = configMap.getJSONObject("timeLimiter");
            updateConfigInDb(channelCode, "time_limiter", timeLimiterMap);
            
            // 2. 更新运行时配置
            // 更新熔断器
            CircuitBreakerConfig circuitBreakerConfig = convertMapToCircuitBreakerConfig(circuitBreakerMap);
            updateCircuitBreaker(channelCode, channel.getName(), circuitBreakerConfig);
            
            // 更新限流器
            RateLimiterConfig rateLimiterConfig = convertMapToRateLimiterConfig(rateLimiterMap);
            updateRateLimiter(channelCode, channel.getName(), rateLimiterConfig);
            
            // 更新超时器
            TimeLimiterConfig timeLimiterConfig = convertMapToTimeLimiterConfig(timeLimiterMap);
            updateTimeLimiter(channelCode, channel.getName(), timeLimiterConfig);
            
        } catch (Exception e) {
            log.error("Failed to update resilience config for channel: {}", channelCode, e);
            throw e;
        }
    }

    /**
     * 更新数据库中的配置
     * 如果配置不存在则新增,存在且值发生变化才更新
     */
    private void updateConfigInDb(Integer channelCode, String configType, JSONObject configMap) {
        // 先查询该渠道该类型的所有配置
        List<ResilienceConfig> existingConfigs = configMapper.selectByChannelAndType(channelCode, configType);
        Map<String, ResilienceConfig> existingConfigMap = existingConfigs.stream()
                .collect(Collectors.toMap(ResilienceConfig::getConfigKey, config -> config));
        
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String configKey = entry.getKey();
            String configValue = entry.getValue().toString();
            
            ResilienceConfig existingConfig = existingConfigMap.get(configKey);
            if (existingConfig == null) {
                // 不存在,新增配置
                ResilienceConfig config = new ResilienceConfig();
                config.setChannelCode(channelCode);
                config.setConfigType(configType);
                config.setConfigKey(configKey);
                config.setConfigValue(configValue);
                configMapper.insert(config);
            } else if (!configValue.equals(existingConfig.getConfigValue())) {
                // 存在且值发生变化,才更新配置
                existingConfig.setConfigValue(configValue);
                configMapper.updateById(existingConfig);
            }
            // 值相同则不做任何操作
        }
    }
    
    private void updateCircuitBreaker(Integer channelCode, String channelName, CircuitBreakerConfig config) {
        CircuitBreakerRegistry registry = circuitBreakerRegistries.get(channelCode);
        if (registry != null) {
            registry.circuitBreaker(channelName, config);
        }
    }
    
    private void updateRateLimiter(Integer channelCode, String channelName, RateLimiterConfig config) {
        RateLimiterRegistry registry = rateLimiterRegistries.get(channelCode);
        if (registry != null) {
            registry.rateLimiter(channelName, config);
        }
    }
    
    private void updateTimeLimiter(Integer channelCode, String channelName, TimeLimiterConfig config) {
        TimeLimiterRegistry registry = timeLimiterRegistries.get(channelCode);
        if (registry != null) {
            registry.timeLimiter(channelName, config);
        }
    }
    
    // 将Map转换为CircuitBreakerConfig
    private CircuitBreakerConfig convertMapToCircuitBreakerConfig(Map<String, Object> map) {
        CircuitBreakerConfig.Builder builder = CircuitBreakerConfig.custom();
        
        if (map.containsKey("failureRateThreshold")) {
            builder.failureRateThreshold(Float.parseFloat(map.get("failureRateThreshold").toString()));
        }
        if (map.containsKey("slidingWindowSize")) {
            builder.slidingWindowSize(Integer.parseInt(map.get("slidingWindowSize").toString()));
        }
        if (map.containsKey("minimumNumberOfCalls")) {
            builder.minimumNumberOfCalls(Integer.parseInt(map.get("minimumNumberOfCalls").toString()));
        }
        if (map.containsKey("waitDurationSeconds")) {
            builder.waitDurationInOpenState(Duration.ofSeconds(Long.parseLong(map.get("waitDurationSeconds").toString())));
        }
        if (map.containsKey("slowCallRateThreshold")) {
            builder.slowCallRateThreshold(Float.parseFloat(map.get("slowCallRateThreshold").toString()));
        }
        if (map.containsKey("slowCallSeconds")) {
            builder.slowCallDurationThreshold(Duration.ofSeconds(Long.parseLong(map.get("slowCallSeconds").toString())));
        }
        
        return builder.build();
    }
    
    // 将Map转换为RateLimiterConfig  
    private RateLimiterConfig convertMapToRateLimiterConfig(Map<String, Object> map) {
        RateLimiterConfig.Builder builder = RateLimiterConfig.custom();
        
        if (map.containsKey("limitForPeriod")) {
            builder.limitForPeriod(Integer.parseInt(map.get("limitForPeriod").toString()));
        }
        if (map.containsKey("limitRefreshPeriodSeconds")) {
            builder.limitRefreshPeriod(Duration.ofSeconds(Long.parseLong(map.get("limitRefreshPeriodSeconds").toString())));
        }
        if (map.containsKey("timeoutMillis")) {
            builder.timeoutDuration(Duration.ofMillis(Long.parseLong(map.get("timeoutMillis").toString())));
        }
        
        return builder.build();
    }
    
    // 将Map转换为TimeLimiterConfig
    private TimeLimiterConfig convertMapToTimeLimiterConfig(Map<String, Object> map) {
        TimeLimiterConfig.Builder builder = TimeLimiterConfig.custom();
        
        if (map.containsKey("timeoutSeconds")) {
            builder.timeoutDuration(Duration.ofSeconds(Long.parseLong(map.get("timeoutSeconds").toString())));
        }
        if (map.containsKey("cancelRunningFuture")) {
            builder.cancelRunningFuture(Boolean.parseBoolean(map.get("cancelRunningFuture").toString()));
        }
        
        return builder.build();
    }

    // 获取默认配置
    private CircuitBreakerConfig getDefaultCircuitBreakerConfig(ChannelTypeEnum channel) {
        ResilienceProperties.CircuitBreakerProps props = resilienceProperties.getCircuitBreaker().get(channel.getName());
        if (props == null) {
            log.warn("No circuit breaker properties found for channel: {}, using default", channel);
            props = new ResilienceProperties.CircuitBreakerProps();
        }
        
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(props.getFailureRateThreshold())
                .waitDurationInOpenState(Duration.ofSeconds(props.getWaitDurationSeconds()))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(props.getSlidingWindowSize())
                .minimumNumberOfCalls(props.getMinimumNumberOfCalls())
                .permittedNumberOfCallsInHalfOpenState(props.getSlidingWindowSize() / 2)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(RuntimeException.class, Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .slowCallRateThreshold(props.getSlowCallRateThreshold())
                .slowCallDurationThreshold(Duration.ofSeconds(props.getSlowCallSeconds()))
                .build();
    }

    private RateLimiterConfig getDefaultRateLimiterConfig(ChannelTypeEnum channel) {
        ResilienceProperties.RateLimiterProps props = resilienceProperties.getRateLimiter().get(channel.getName());
        if (props == null) {
            log.warn("No rate limiter properties found for channel: {}, using default", channel);
            props = new ResilienceProperties.RateLimiterProps();
        }

        return RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(props.getLimitRefreshPeriodSeconds()))
                .limitForPeriod(props.getLimitForPeriod())
                .timeoutDuration(Duration.ofMillis(props.getTimeoutMillis()))
                .build();
    }

    private TimeLimiterConfig getDefaultTimeLimiterConfig(ChannelTypeEnum channel) {
        ResilienceProperties.TimeLimiterProps props = resilienceProperties.getTimeLimiter().get(channel.getName());
        if (props == null) {
            log.warn("No time limiter properties found for channel: {}, using default", channel);
            props = new ResilienceProperties.TimeLimiterProps();
        }

        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(props.getTimeoutSeconds()))
                .cancelRunningFuture(props.isCancelRunningFuture())
                .build();
    }
} 
package com.oszero.deliver.server.config;

import com.oszero.deliver.server.config.resilience4j.RateLimiterProperties;
import com.oszero.deliver.server.config.resilience4j.ResilienceConfigManager;
import com.oszero.deliver.server.config.resilience4j.TimeLimiterProperties;
import com.oszero.deliver.server.enums.ChannelTypeEnum;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: Black788
 * @date: 2025/1/30 17:31
 */
@Configuration
@RequiredArgsConstructor
public class Resilience4jConfig {

    private final MeterRegistry meterRegistry;
    private final ResilienceConfigManager configManager;

    @Bean
    public Map<Integer, RateLimiterRegistry> rateLimiterRegistries() {
        return Arrays.stream(ChannelTypeEnum.values())
                .collect(Collectors.toMap(
                        ChannelTypeEnum::getCode,
                        channel -> {
                            RateLimiterConfig config = configManager.getRateLimiterConfig(channel.getCode());
                            RateLimiterRegistry registry = RateLimiterRegistry.of(config);
                            TaggedRateLimiterMetrics.ofRateLimiterRegistry(registry).bindTo(meterRegistry);
                            return registry;
                        }
                ));
    }

    @Bean
    public Map<Integer, CircuitBreakerRegistry> circuitBreakerRegistries() {
        return Arrays.stream(ChannelTypeEnum.values())
                .collect(Collectors.toMap(
                        ChannelTypeEnum::getCode,
                        channel -> {
                            CircuitBreakerConfig config = configManager.getCircuitBreakerConfig(channel.getCode());
                            CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
                            TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(meterRegistry);
                            return registry;
                        }
                ));
    }

    @Bean
    public Map<Integer, TimeLimiterRegistry> timeLimiterRegistries() {
        Map<Integer, TimeLimiterRegistry> collect = Arrays.stream(ChannelTypeEnum.values())
                .collect(Collectors.toMap(
                        ChannelTypeEnum::getCode,
                        channel -> {
                            TimeLimiterConfig config = configManager.getTimeLimiterConfig(channel.getCode());
                            TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);
                            TaggedTimeLimiterMetrics.ofTimeLimiterRegistry(registry).bindTo(meterRegistry);
                            return registry;
                        }
                ));
        return collect;
    }
}

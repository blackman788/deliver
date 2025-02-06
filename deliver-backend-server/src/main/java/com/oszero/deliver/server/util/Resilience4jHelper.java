package com.oszero.deliver.server.util;

import com.oszero.deliver.server.exception.MessageException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oszero.deliver.server.enums.ChannelTypeEnum;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class Resilience4jHelper {

    private final Map<Integer, CircuitBreakerRegistry> circuitBreakerRegistries;
    private final Map<Integer, RateLimiterRegistry> rateLimiterRegistries;
    private final Map<Integer, TimeLimiterRegistry> timeLimiterRegistries;
    private final StringRedisTemplate redisTemplate;
    
    private static final String METRICS_KEY_PREFIX = "resilience:metrics:%s:%s";  // channel:type
    private static final int METRICS_TTL = 7;  // 指标保存7天
    private static final int METRICS_SAMPLE_RATE = 10; // 采样率:每10次请求采集1次完整指标
    private static final String REQUEST_COUNTER_KEY = "resilience:request_counter";
    
    // 使用ThreadPoolExecutor替代ScheduledExecutorService,便于管理线程池
    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            30,                     // 核心线程数
            60,                    // 最大线程数
            60L,                   // 空闲线程存活时间
            TimeUnit.SECONDS,      // 时间单位
            new LinkedBlockingQueue<>(1000),  // 工作队列
            new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略
    );

    private final Random random = new Random();
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    /**
     * 执行带有熔断、限流和超时保护的操作
     */
    public <T> T executeWithResilience(ChannelTypeEnum channelType, Supplier<T> operation) {
        String channelName = channelType.getName();
        Integer channelCode = channelType.getCode();
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(channelCode,channelName);
        RateLimiter rateLimiter = getOrCreateRateLimiter(channelCode,channelName);
        TimeLimiter timeLimiter = getOrCreateTimeLimiter(channelCode,channelName);

        // 使用原子计数器进行采样,到达最大值时重置
        int currentCount = requestCounter.getAndIncrement();
        if (currentCount > 1000000) {
            requestCounter.compareAndSet(currentCount, 0);
        }
        
        // 每METRICS_SAMPLE_RATE次采集一次完整指标
        if (currentCount % METRICS_SAMPLE_RATE == 0) {
            CompletableFuture.runAsync(() -> {
                collectCircuitBreakerMetrics(channelName, circuitBreaker);
                collectRateLimiterMetrics(channelName, rateLimiter);
            }, executorService);
        }

        try {
            Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker,
                    RateLimiter.decorateSupplier(rateLimiter, operation));

            Supplier<CompletableFuture<T>> futureSupplier = () -> 
                    CompletableFuture.supplyAsync(decoratedSupplier, executorService);
            
            long startTime = System.currentTimeMillis();
            T result = timeLimiter.executeFutureSupplier(futureSupplier);
            
            // 异步收集响应时间
            long responseTime = System.currentTimeMillis() - startTime;
            CompletableFuture.runAsync(() ->
                    collectTimeLimiterMetrics(channelName, responseTime), executorService);
            
            return result;
        } catch (Exception e) {
            // 异步收集异常指标
            CompletableFuture.runAsync(() -> 
                collectExceptionMetrics(channelName, e), executorService);
            throw handleException(channelName, e);
        }
    }

    /**
     * 获取或创建断路器
     */
    private CircuitBreaker getOrCreateCircuitBreaker(Integer channelCode,String channelName) {
        CircuitBreakerRegistry registry = circuitBreakerRegistries.get(channelCode);
        if (registry == null) {
            throw new IllegalArgumentException("No circuit breaker registry found for channel: " + channelName);
        }
        return registry.circuitBreaker(channelName);
    }

    /**
     * 获取或创建限流器
     */
    private RateLimiter getOrCreateRateLimiter(Integer channelCode,String channelName) {
        RateLimiterRegistry registry = rateLimiterRegistries.get(channelCode);
        if (registry == null) {
            throw new IllegalArgumentException("No rate limiter registry found for channel: " + channelName);
        }
        return registry.rateLimiter(channelName);
    }

    /**
     * 获取或创建超时器
     */
    private TimeLimiter getOrCreateTimeLimiter(Integer channelCode,String channelName) {
        TimeLimiterRegistry registry = timeLimiterRegistries.get(channelCode);
        if (registry == null) {
            throw new IllegalArgumentException("No time limiter registry found for channel: " + channelName);
        }
        return registry.timeLimiter(channelName);
    }

    /**
     * 收集熔断器指标
     * 包含以下指标:
     * - failureRate: 失败率(百分比)
     * - slowCallRate: 慢调用比例(百分比)
     * - numberOfFailedCalls: 失败调用次数
     * - numberOfSlowCalls: 慢调用次数
     * - numberOfSuccessfulCalls: 成功调用次数
     * - state: 熔断器状态(CLOSED:关闭, OPEN:打开, HALF_OPEN:半开)
     */
    private void collectCircuitBreakerMetrics(String channel, CircuitBreaker circuitBreaker) {
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        Map<String, String> metricsMap = new HashMap<>();

        // 失败率: 失败调用次数/总调用次数 * 100%
        float failureRate = metrics.getFailureRate();
        metricsMap.put("failureRate", failureRate == -1.0f ? "0.0" : String.valueOf(failureRate));
        // 慢调用比例: 慢调用次数/总调用次数 * 100%
        float slowCallRate = metrics.getSlowCallRate();
        metricsMap.put("slowCallRate", slowCallRate == -1.0f ? "0.0" : String.valueOf(slowCallRate));
        // 失败调用总次数
        metricsMap.put("numberOfFailedCalls", String.valueOf(metrics.getNumberOfFailedCalls()));
        // 慢调用总次数(响应时间超过配置阈值的调用)
        metricsMap.put("numberOfSlowCalls", String.valueOf(metrics.getNumberOfSlowCalls()));
        // 成功调用总次数
        metricsMap.put("numberOfSuccessfulCalls", String.valueOf(metrics.getNumberOfSuccessfulCalls()));
        // 熔断器当前状态
        metricsMap.put("state", circuitBreaker.getState().name());
        
        String key = String.format(METRICS_KEY_PREFIX, channel, "circuit_breaker");
        saveMetricsBatch(key, metricsMap);
    }

    /**
     * 收集限流器指标
     * 包含以下指标:
     * - availablePermissions: 当前可用许可数
     * - numberOfWaitingThreads: 当前等待获取许可的线程数
     */
    private void collectRateLimiterMetrics(String channel, RateLimiter rateLimiter) {
        RateLimiter.Metrics metrics = rateLimiter.getMetrics();
        Map<String, String> metricsMap = new HashMap<>();
        
        // 当前剩余的可用许可数量
        metricsMap.put("availablePermissions", String.valueOf(metrics.getAvailablePermissions()));
        // 当前正在等待获取许可的线程数量
        metricsMap.put("numberOfWaitingThreads", String.valueOf(metrics.getNumberOfWaitingThreads()));
        
        String key = String.format(METRICS_KEY_PREFIX, channel, "rate_limiter");
        saveMetricsBatch(key, metricsMap);
    }

    /**
     * 收集响应时间指标(简化版)
     */
    private void collectResponseTimeMetrics(String channel, long responseTime) {
        try {
            String key = String.format(METRICS_KEY_PREFIX, channel, "response_time");
            String timeKey = String.valueOf(System.currentTimeMillis());
            redisTemplate.opsForZSet().add(key, timeKey, responseTime);
            
            // 异步清理过期数据
            if (random.nextInt(100) == 0) { // 1%概率执行清理
                CompletableFuture.runAsync(() -> {
                    redisTemplate.opsForZSet().removeRange(key, 0, -1001);
                    redisTemplate.expire(key, METRICS_TTL, TimeUnit.DAYS);
                }, executorService);
            }
        } catch (Exception e) {
            log.error("Failed to collect response time metrics", e);
        }
    }

    /**
     * 收集超时器指标
     * 包含以下指标:
     * - timeoutCount: 超时次数,直观反映系统超时情况
     * - timeoutRate: 超时比例(超时次数/总调用次数),可以评估系统的稳定性
     * - avgResponseTime: 平均响应时间(毫秒),反映系统整体性能
     * - p95ResponseTime: 95分位响应时间(毫秒),反映大多数用户的体验
     * - p99ResponseTime: 99分位响应时间(毫秒),发现长尾请求
     * - maxResponseTime: 最大响应时间(毫秒),发现极端情况
     *
     * 告警设置
     * 可以基于这些指标设置告警阈值
     * 比如p95超过某个值就告警
     *
     */
    private void collectTimeLimiterMetrics(String channel, long responseTime) {
        String key = String.format(METRICS_KEY_PREFIX, channel, "time_limiter");
        try {
            Map<String, String> metrics = new HashMap<>();
            
            // 更新总调用次数
            String totalKey = "totalCount";
            String total = (String) redisTemplate.opsForHash().get(key, totalKey);
            long totalCount = total == null ? 0 : Long.parseLong(total);
            redisTemplate.opsForHash().put(key, totalKey, String.valueOf(totalCount + 1));
            
            // 更新响应时间统计
            String responseTimeListKey = "responseTimes";
            String responseTimeListStr = (String) redisTemplate.opsForHash().get(key, responseTimeListKey);
            List<Long> responseTimes;
            if (responseTimeListStr != null) {
                responseTimes = new ObjectMapper().readValue(responseTimeListStr, 
                    new TypeReference<List<Long>>() {});
            } else {
                responseTimes = new ArrayList<>();
            }
            
            // 保留最近1500个响应时间数据
            if (responseTimes.size() >= 1500) {
                responseTimes.remove(0);
            }
            responseTimes.add(responseTime);
            
            // 计算统计指标
            Collections.sort(responseTimes);
            int size = responseTimes.size();
            
            // 平均响应时间
            double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
            metrics.put("avgResponseTime", String.format("%.2f", avgResponseTime));
            
            // 最大响应时间
            metrics.put("maxResponseTime", String.valueOf(responseTimes.get(size - 1)));
            
            // 95分位响应时间
            int p95Index = (int) Math.ceil(size * 0.95) - 1;
            metrics.put("p95ResponseTime", String.valueOf(responseTimes.get(p95Index)));
            
            // 99分位响应时间
            int p99Index = (int) Math.ceil(size * 0.99) - 1;
            metrics.put("p99ResponseTime", String.valueOf(responseTimes.get(p99Index)));
            
            // 保存响应时间列表
            redisTemplate.opsForHash().put(key, responseTimeListKey, 
                new ObjectMapper().writeValueAsString(responseTimes));
            
            // 保存统计指标
            for (Map.Entry<String, String> entry : metrics.entrySet()) {
                redisTemplate.opsForHash().put(key, entry.getKey(), entry.getValue());
            }
            
            redisTemplate.expire(key, METRICS_TTL, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("Failed to collect time limiter metrics", e);
        }
    }

    /**
     * 使用Redis Pipeline批量更新指标
     */
    private void saveMetricsBatch(String key, Map<String, String> metrics) {
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                StringRedisConnection stringRedisConn = (StringRedisConnection) connection;
                for (Map.Entry<String, String> entry : metrics.entrySet()) {
                    stringRedisConn.hSet(key, entry.getKey(), entry.getValue());
                }
                stringRedisConn.expire(key, METRICS_TTL* 60 * 60 * 24);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to save metrics batch", e);
        }
    }

    /**
     * 获取指标数据
     * 返回数据结构:
     * {
     *   "circuitBreaker": {
     *     "failureRate": "0.0",          // 失败率
     *     "slowCallRate": "0.0",         // 慢调用比例
     *     "numberOfFailedCalls": "0",     // 失败调用次数
     *     "numberOfSlowCalls": "0",       // 慢调用次数
     *     "numberOfSuccessfulCalls": "10", // 成功调用次数
     *     "state": "CLOSED"               // 熔断器状态
     *   },
     *   "rateLimiter": {
     *     "availablePermissions": "100",   // 可用许可数
     *     "numberOfWaitingThreads": "0"    // 等待线程数
     *   },
     *   "timeLimiter": {
     *       "timeoutCount": "1648888888888",      // 调用次数
     *       "avgResponseTime": "1648888888888",      // 平均响应时间
     *       "p95ResponseTime": "1648888888888",      // 95分位响应时间
     *       "p99ResponseTime": "1648888888888",      // 99分位响应时间
     *       "maxResponseTime": "1648888888888"      // 最大响应时间
     *   }
     * }
     *
     * @param channel 渠道名称
     * @return 该渠道的所有指标数据
     */
    public Map<String, Object> getChannelMetrics(String channel) {
        Map<String, Object> allMetrics = new HashMap<>();
        
        // 获取熔断器指标(断路器状态、成功/失败次数等)
        String circuitBreakerKey = String.format(METRICS_KEY_PREFIX, channel, "circuit_breaker");
        String circuitBreakerJson = redisTemplate.opsForValue().get(circuitBreakerKey);
        if (circuitBreakerJson != null) {
            try {
                allMetrics.put("circuitBreaker", new ObjectMapper().readValue(circuitBreakerJson, Map.class));
            } catch (Exception e) {
                log.error("Failed to parse circuit breaker metrics", e);
            }
        }
        
        // 获取限流器指标(可用许可数、等待线程数)
        String rateLimiterKey = String.format(METRICS_KEY_PREFIX, channel, "rate_limiter");
        String rateLimiterJson = redisTemplate.opsForValue().get(rateLimiterKey);
        if (rateLimiterJson != null) {
            try {
                allMetrics.put("rateLimiter", new ObjectMapper().readValue(rateLimiterJson, Map.class));
            } catch (Exception e) {
                log.error("Failed to parse rate limiter metrics", e);
            }
        }
        
        // 获取响应时间指标(时间序列数据)
        String responseTimeKey = String.format(METRICS_KEY_PREFIX, channel, "response_time");
        Set<ZSetOperations.TypedTuple<String>> responseTimeSet = 
                redisTemplate.opsForZSet().rangeWithScores(responseTimeKey, 0, -1);
        if (responseTimeSet != null) {
            List<Map<String, Object>> responseTimeList = responseTimeSet.stream()
                    .map(tuple -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("time", tuple.getValue());    // 时间戳
                        point.put("value", tuple.getScore());   // 响应时间
                        return point;
                    })
                    .collect(Collectors.toList());
            allMetrics.put("responseTime", responseTimeList);
        }
        
        return allMetrics;
    }

    /**
     * 统一处理异常
     */
    private MessageException handleException(String channel, Exception e) {
        if (e instanceof MessageException) {
            return (MessageException) e;
        }
        
        if (e instanceof CompletionException || e instanceof ExecutionException) {
            Throwable cause = e.getCause();
            if (cause instanceof MessageException) {
                return (MessageException) cause;
            }
            log.error("Channel {} operation failed", channel, cause);
            return new MessageException(String.format("Channel %s operation failed: %s", 
                channel, cause.getMessage()));
        }
        
        if (e instanceof TimeoutException) {
            log.error("Channel {} operation timeout", channel);
            return new MessageException(String.format("Channel %s operation timeout", channel));
        }
        
        if (e instanceof BulkheadFullException) {
            log.error("Channel {} bulkhead full", channel);
            return new MessageException(String.format("Channel %s too many concurrent requests", channel));
        }
        
        if (e instanceof RequestNotPermitted) {
            log.error("Channel {} request not permitted", channel);
            return new MessageException(String.format("Channel %s request not permitted due to rate limiting or circuit breaker", channel));
        }
        
        log.error("Channel {} unexpected error", channel, e);
        return new MessageException(String.format("Channel %s operation failed with unexpected error: %s", 
            channel, e.getMessage()));
    }

    /**
     * 收集异常指标
     * 包含以下指标:
     * - exceptionCount: 异常总数
     * - exceptionTypes: 各类异常计数
     */
    private void collectExceptionMetrics(String channel, Exception e) {
        String key = String.format(METRICS_KEY_PREFIX, channel, "exceptions");
        try {
            Map<String, String> metrics = new HashMap<>();
            
            // 增加异常总数
            String totalKey = "total";
            String total = (String) redisTemplate.opsForHash().get(key, totalKey);
            long totalCount = total == null ? 0 : Long.parseLong(total);
            redisTemplate.opsForHash().put(key, totalKey, String.valueOf(totalCount + 1));
            
            // 增加具体异常类型计数
            String exceptionType = getExceptionType(e);
            String count = (String) redisTemplate.opsForHash().get(key, exceptionType);
            long typeCount = count == null ? 0 : Long.parseLong(count);
            redisTemplate.opsForHash().put(key, exceptionType, String.valueOf(typeCount + 1));
            
            redisTemplate.expire(key, METRICS_TTL, TimeUnit.DAYS);
        } catch (Exception ex) {
            log.error("Failed to collect exception metrics", ex);
        }
    }

    /**
     * 获取异常类型
     */
    private String getExceptionType(Exception e) {
        if (e instanceof CompletionException || e instanceof ExecutionException) {
            return e.getCause().getClass().getSimpleName();
        }
        return e.getClass().getSimpleName();
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Resilience4jHelper executor service");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 
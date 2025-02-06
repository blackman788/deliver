export interface CircuitBreakerData {
  failureRate: string
  slowCallRate: string
  numberOfFailedCalls: string
  numberOfSlowCalls: string
  numberOfSuccessfulCalls: string
  state: string
}

export interface RateLimiterData {
  availablePermissions: string
  numberOfWaitingThreads: string
}

export interface TimeLimiterData {
  totalCount: string
  avgResponseTime: string
  p95ResponseTime: string
  p99ResponseTime: string
  maxResponseTime: string
}

export interface Exceptions {
  [key: string]: number;
}

export interface FlowControlData {
  circuitBreaker: CircuitBreakerData
  rateLimiter: RateLimiterData
  timeLimiter: TimeLimiterData
  exceptions: Exceptions
}

export interface RateLimiterConfig {
  limitForPeriod: string
  limitRefreshPeriodSeconds: string
  timeoutMillis: string
}

export interface CircuitBreakerConfig {
  failureRateThreshold: string
  slowCallRateThreshold: string
  waitDurationSeconds: string
  slowCallSeconds: string
  minimumNumberOfCalls: string
  slidingWindowSize: string
}

export interface TimeLimiterConfig {
  cancelRunningFuture: boolean
  timeoutSeconds: string
}

export interface FlowControlConfig {
  rateLimiter: RateLimiterConfig
  circuitBreaker: CircuitBreakerConfig
  timeLimiter: TimeLimiterConfig
} 
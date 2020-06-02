1. Add Maven Dependencies
  compile "io.github.resilience4j:resilience4j-spring-boot2:${resilience4jVersion}"
  compile('org.springframework.boot:spring-boot-starter-actuator')
  compile('org.springframework.boot:spring-boot-starter-aop')

2. Annotate the class or method with resilience4j annotations
@CircuitBreaker(name = BACKEND, fallbackMethod = "fallback")
@RateLimiter(name = BACKEND)
@Bulkhead(name = BACKEND, type= Bulkhead.Type.SEMAPHORE) 
@Bulkhead(name = BACKEND, type = Bulkhead.Type.THREADPOOL)
@Retry(name = BACKEND, fallbackMethod = "fallback")
@TimeLimiter(name = BACKEND)
public Mono<String> method(String param1) {
    return Mono.error(new NumberFormatException());
}

private Mono<String> fallback(String param1, IllegalArgumentException e) {
    return Mono.just("test");
}

private Mono<String> fallback(String param1, RuntimeException e) {
    return Mono.just("test");
}  

3. Bean CircuitBreakerAutoConfiguration is created using the properties specified in the application.yaml file
Update Properties in application.yaml / properties file
resilience4j.circuitbreaker:
    instances:
        backendA:
            registerHealthIndicator: true
            slidingWindowSize: 100
        backendB:
            registerHealthIndicator: true
            slidingWindowSize: 10
            permittedNumberOfCallsInHalfOpenState: 3
            slidingWindowType: TIME_BASED
            minimumNumberOfCalls: 20
            waitDurationInOpenState: 50s
            failureRateThreshold: 50
            eventConsumerBufferSize: 10
            recordFailurePredicate: io.github.robwin.exception.RecordFailurePredicate

            registerHealthIndicator: true
            ringBufferSizeInClosedState: 5
            ringBufferSizeInHalfOpenState: 3
            withDurationInOpenState: 120s
            failureRateThreshold: 50
            eventConsumerBufferSize: 10
            minimumNumberOfCalls: 10
            recordExceptions:
              - org.springframework.web.client.HttpServerErrorException
              - java.net.SocketTimeoutException
              - java.util.concurrent.TimeoutException
              - org.springframework.web.client.ResourceAccessException
            ignoreExceptions:            



4. Aspect order
   The Resilience4j Aspects order is following:
   Retry ( CircuitBreaker ( RateLimiter ( TimeLimiter ( Bulkhead ( Function ) ) ) ) )
   so Retry is applied at the end (if needed).

5. Health endpoint
   By default the CircuitBreaker or RateLimiter health indicators are disabled, but you can enable them via the configuration. Health Indicators are disabled, because the application status is DOWN, when a CircuitBreaker is OPEN. This might not be what you want to achieve.
   
   application YAML
   management.health.circuitbreakers.enabled: true
   management.health.ratelimiters.enabled: true
   
   resilience4j.circuitbreaker:
     configs:
       default:
         registerHealthIndicator: true
   
   
   resilience4j.ratelimiter:
     configs:
       default:
         registerHealthIndicator: true
         
   A closed CircuitBreaker state is mapped to UP, an open state to DOWN and a half-open state to UNKNOWN.   

6. Events endpoint
   The emitted CircuitBreaker, Retry, RateLimiter, Bulkhead and TimeLimiter events are stored in a separate circular event consumer buffers. The size of a event consumer buffer can be configured in the application.yml file (eventConsumerBufferSize).
   
   The endpoint /actuator/circuitbreakers lists the names of all CircuitBreaker instances. The endpoint is also available for Retry, RateLimiter, Bulkhead and TimeLimiter.
   
   The endpoint /actuator/circuitbreakerevents lists by default the latest 100 emitted events of all CircuitBreaker instances. The endpoint is also available for Retry, RateLimiter, Bulkhead and TimeLimiter.
   
7. Metrics endpoint
   CircuitBreaker, Retry, RateLimiter, Bulkhead and TimeLimiter Metrics are automatically published on the Metrics endpoint. To retrieve the names of the available metrics, make a GET request to /actuator/metrics

To retrieve a metric, make a GET request to /actuator/metrics/{metric.name}.
For example: /actuator/metrics/resilience4j.circuitbreaker.calls

When you want to publish CircuitBreaker endpoints on the Prometheus endpoint, you have to add the dependency io.micrometer:micrometer-registry-prometheus.


**_Bulkhead**_ 
1. Resilience4j Bulkhead is used to limit the number of concurrent execution.
    SemaphoreBulkhead
    FixedThreadPoolBulkhead
2. Add annotations
    //@Bulkhead(name="addservicebhSemaphore", fallbackMethod = "addItemBulkheadSemaphoreFallBack", type = Bulkhead.Type.SEMAPHORE)
    @Bulkhead(name="addservicebhFixedThreadPool", fallbackMethod = "addItemFixedBulkheadThreadFallBack", type = Bulkhead.Type.THREADPOOL)
3. Update the config file with 
resilience4j:
  bulkhead:
    instances:
      addservicebhSemaphore:
        maxWaitDuration: 10ms
        maxConcurrentCall : 1
  thread-pool-bulkhead:
    instances:
      addservicebhFixedThreadPool:
        maxThreadPoolSize: 1
        coreThreadPoolSize: 1
        queueCapacity: 5
       
**_RateLimiter_**
1. Used to control the rate of traffic sent or received       
2. @RateLimiter(name="addServiceRateLimiter",fallbackMethod = "addItemRateLimiterFallBack")
3. 
resilience4j:
  ratelimiter:
    instances:
      addItemRateLimiterFallBack:
        limitForPeriod: 2
        limitRefreshPeriod: 10000
        timeoutDuration: 1000ms
        
**_Retry_**
1. Used to control the rate of traffic sent or received       
2. @Retry(name="addServiceRateLimiter",fallbackMethod = "addItemRateLimiterFallBack")
3. 
resilience4j:
  retry:
    instances:
      addItemRateLimiterFallBack:
        limitForPeriod: 2
        limitRefreshPeriod: 10000
        timeoutDuration: 1000ms        
        


        
Micrometer , Prometheus , Graphana
1. Add the dependency

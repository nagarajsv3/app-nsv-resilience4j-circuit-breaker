package com.nsv.jsmbaba.service;

import com.nsv.jsmbaba.domain.Item;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class ExperienceServiceImpl implements ExperienceService {

    @Autowired
    private RestTemplate restTemplate;

    @Value(value= "${shopping.cart.add.item.url}")
    private String addItemUrl;

    @Value(value= "${shopping.cart.getall.items.url}")
    private String getAllItemsUrl;

    //Aspect Order
    //Retry(CircuitBreaker(RateLimiter(TimeLimiter(Bulkhead(function())))))
    @Override

    @CircuitBreaker(name="addservice", fallbackMethod = "addItemCircuitBreakerFallBack")
    @RateLimiter(name="addServiceRateLimiter",fallbackMethod = "addItemRateLimiterFallBack")
    @Bulkhead(name="addservicebhSemaphore", fallbackMethod = "addItemBulkheadSemaphoreFallBack", type = Bulkhead.Type.SEMAPHORE)
    //@Bulkhead(name="addservicebhFixedThreadPool", fallbackMethod = "addItemFixedBulkheadThreadFallBack", type = Bulkhead.Type.THREADPOOL)
    @Retry(name="addServiceRetry",fallbackMethod = "addItemRetryFallBack")
    public Item addItem(Item item) {

        log.info("Inside addItem Actual Method");
        log.info("Thread Name = {}", Thread.currentThread().getName());

        //Added to verify Bulkhead
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        HttpEntity requestEntity = getMeRequestEntity(item);
        ResponseEntity<Item> stringResponseEntity = restTemplate.postForEntity(addItemUrl, requestEntity, Item.class);
        Item body = stringResponseEntity.getBody();

        return body;
    }

    public Item addItemCircuitBreakerFallBack(Item item, Throwable t) {
        log.info("Inside Circuit Breaker Fallback Method");
        log.info("Exception = "+t.toString());
        item.setItemName("Circuit Breaker Fallback "+item.getItemName());
        item.setItemType("Circuit Breaker Fallback "+item.getItemType());
        return item;
    }

    public Item addItemRetryFallBack(Item item, Throwable t) {
        log.info("Inside Retry Fallback Method");
        log.info("Exception = "+t.toString());
        item.setItemName("Retry Fallback "+item.getItemName());
        item.setItemType("Retry Fallback "+item.getItemType());
        return item;
    }

    public Item addItemRateLimiterFallBack(Item item, Throwable t) {
        log.info("Inside RateLimiter Fallback Method");
        log.info("Exception = "+t.toString());
        item.setItemName("RateLimiter Fallback "+item.getItemName());
        item.setItemType("RateLimiter Fallback "+item.getItemType());
        return item;
    }

    public Item addItemBulkheadSemaphoreFallBack(Item item, Throwable t) {
        log.info("Inside Bulk head Semaphore Fallback Method");
        log.info("Exception = "+t.toString());
        item.setItemName("Bulkhead Fallback "+item.getItemName());
        item.setItemType("Bulkhead Fallback "+item.getItemType());
        return item;
    }

    public Item addItemFixedBulkheadThreadFallBack(Item item, Throwable t) {
        log.info("Inside Bulk head FixedThreadPool Fallback Method");
        log.info("Exception = "+t.toString());
        item.setItemName("Bulkhead Fallback "+item.getItemName());
        item.setItemType("Bulkhead Fallback "+item.getItemType());
        return item;
    }


    private HttpEntity<Item> getMeRequestEntity(Item item) {
        HttpEntity<Item> httpEntity = new HttpEntity(item, getRequestHeaders());
        return httpEntity;
    }

    private HttpHeaders getRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @Override
    public List<Item> getAllItems() {
        return null;
    }
}

package com.nsv.jsmbaba.service;

import com.nsv.jsmbaba.domain.Item;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    @Override
    @CircuitBreaker(name="addservice", fallbackMethod = "addItemFallBack")
    public Item addItem(Item item) {
        log.info("Actual Method");
        HttpEntity requestEntity = getMeRequestEntity(item);
        ResponseEntity<Item> stringResponseEntity = restTemplate.postForEntity(addItemUrl, requestEntity, Item.class);
        Item body = stringResponseEntity.getBody();

        return body;
    }

    public Item addItemFallBack(Item item, Throwable t) {
        log.info("Fallback Method");
        log.info("Exception = "+t.toString());
        item.setItemName("Fallback "+item.getItemName());
        item.setItemType("Fallback "+item.getItemType());
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

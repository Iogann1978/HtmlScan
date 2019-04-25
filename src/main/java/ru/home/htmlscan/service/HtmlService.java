package ru.home.htmlscan.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HtmlService {
    private RestTemplate restTemplate;

    @Autowired
    public HtmlService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async("htmlExecutor")
    public CompletableFuture<String> getHtml(String url) {
        val response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
            return CompletableFuture.completedFuture(response.getBody());
        } else {
            log.error("Http code = {}, reason:", response.getStatusCodeValue(), response.getStatusCode().getReasonPhrase());
            return CompletableFuture.completedFuture(null);
        }
    }

    @Async("htmlExecutor")
    public CompletableFuture<HttpStatus> register(String uri) {
        val params = new HashMap<String, String>();
        params.put("FIO", "Козлов Антон Викторович");
        params.put("PHONE", "+79099520361");
        params.put("EMAIL", "iogann1978@gmail.com");
        params.put("check_in-18918__form__checkbox-1", "true");
        params.put("check_in-18918__form__checkbox-2", "true");
        val response = restTemplate.exchange(uri, HttpMethod.GET, null, String.class, params);
        return CompletableFuture.completedFuture(response.getStatusCode());
    }
}

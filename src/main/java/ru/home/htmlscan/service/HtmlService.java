package ru.home.htmlscan.service;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.home.htmlscan.config.HtmlScanProperties;
import ru.home.htmlscan.model.RegisterItem;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HtmlService {
    private RestTemplate restTemplate;
    private HtmlScanProperties properties;
    private static final String phrase = ">Регистрация<";

    @Autowired
    public HtmlService(RestTemplate restTemplate, HtmlScanProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
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
    public CompletableFuture<HttpStatus> register(String html) {
        val item = RegisterItem.get(html);
        item.setFIO("Козлов Антон Викторович");
        item.setEMAIL("iogann1978@gmail.com");
        item.setPHONE("+79099520361");
        val headers = new HttpHeaders();
        headers.setAccept(ImmutableList.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        val request = new HttpEntity<>(item, headers);
        val params = new HashMap<String, String>();
        params.put("ACTION", "set");
        params.put("TIME_ID", item.getTIME_ID());
        val response = restTemplate.exchange(properties.getUrireg(), HttpMethod.GET,
                request, String.class, params);
        return CompletableFuture.completedFuture(response.getStatusCode());
    }

    @Async("htmlExecutor")
    public static CompletableFuture<Boolean> checkHtml(String html) {
        if(html.contains(phrase)) {
            return CompletableFuture.completedFuture(true);
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }
}

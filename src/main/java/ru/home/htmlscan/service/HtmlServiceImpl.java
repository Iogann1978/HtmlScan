package ru.home.htmlscan.service;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.home.htmlscan.config.HtmlProperties;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class HtmlServiceImpl implements HtmlService {
    private RestTemplate restTemplate;
    private HtmlProperties properties;
    private static final String phraseReg = ">Регистрация<", phraseEmb = "Посол",
            className="events_gallery__item__body";

    @Autowired
    public HtmlServiceImpl(RestTemplate restTemplate, HtmlProperties properties) {
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
    public CompletableFuture<HttpStatus> register(MultiValueMap<String, String> fields) {
        val headers = new HttpHeaders();
        headers.setAccept(ImmutableList.of(MediaType.APPLICATION_FORM_URLENCODED));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAcceptCharset(ImmutableList.of(StandardCharsets.UTF_8));
        val request = new HttpEntity<>(headers);
        log.info("reg item: {}", fields);
        val response = restTemplate.exchange(properties.getUrireg(), HttpMethod.POST,
                request, String.class, fields);
        if(response.hasBody()) {
            log.info("Response: {]", response.getBody());
        }
        return CompletableFuture.completedFuture(response.getStatusCode());
    }

    @Async("htmlExecutor")
    public CompletableFuture<Boolean> checkHtml(String html) {
        if(html.contains(phraseReg)) {
            return CompletableFuture.completedFuture(true);
        } else {
            return CompletableFuture.completedFuture(false);
        }
    }

    @Async("htmlExecutor")
    public CompletableFuture<Map<String, String>> checkEmbassies(String html) {
        val map = new HashMap<String, String>();
        Jsoup.parse(html).getElementsByClass(className)
                .stream().filter(e -> e.text().contains(phraseEmb))
                .forEach(e -> map.put(e.attr("href"), e.text()));
        return CompletableFuture.completedFuture(map);
    }

    @Async("htmlExecutor")
    public CompletableFuture<Map<String, String>> getEvents(String html) {
        val map = new HashMap<String, String>();
        Jsoup.parse(html).getElementsByClass(className)
                .stream().forEach(e -> map.put(e.attr("href"), e.text()));
        return CompletableFuture.completedFuture(map);
    }
}

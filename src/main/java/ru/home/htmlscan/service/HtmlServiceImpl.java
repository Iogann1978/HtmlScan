package ru.home.htmlscan.service;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.home.htmlscan.config.HtmlProperties;
import ru.home.htmlscan.model.RegisterItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    public CompletableFuture<HttpStatus> register(RegisterItem item) {
        val headers = new HttpHeaders();
        headers.setAccept(ImmutableList.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        val request = new HttpEntity<>(item, headers);
        val params = new HashMap<String, String>();
        params.put("ACTION", "set");
        params.put("TIME_ID", item.getTIME_ID());
        log.info("reg item: {}", item);
        val response = restTemplate.exchange(properties.getUrireg(), HttpMethod.GET,
                request, String.class, params);
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

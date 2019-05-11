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
import ru.home.htmlscan.model.SiteState;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HtmlServiceImpl implements HtmlService {
    private RestTemplate restTemplate;
    private HtmlProperties htmlProperties;
    private static final String classNameList = "events_gallery__item__col",
            classNameItem = "events_gallery__item__body",
            classNameHidden = "hidden-xs-up";

    @Autowired
    public HtmlServiceImpl(RestTemplate restTemplate, HtmlProperties htmlProperties) {
        this.restTemplate = restTemplate;
        this.htmlProperties = htmlProperties;
    }

    @Async("htmlExecutor")
    public CompletableFuture<Optional<String>> getHtml(String url) {
        Optional<String> html = Optional.empty();
        val response = restTemplate.getForEntity(url, String.class);
        if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
            html = Optional.ofNullable(response.getBody());
        } else {
            log.error("Http code = {}, reason:", response.getStatusCodeValue(), response.getStatusCode().getReasonPhrase());
        }
        return CompletableFuture.completedFuture(html);
    }

    @Async("htmlExecutor")
    public CompletableFuture<HttpStatus> register(Map<String, String> fields) {
        val headers = new HttpHeaders();
        headers.setAccept(ImmutableList.of(MediaType.APPLICATION_FORM_URLENCODED));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAcceptCharset(ImmutableList.of(StandardCharsets.UTF_8));
        val body = String.join("&", fields.entrySet().stream()
                .map(field -> field.getKey() + "=" + field.getValue())
                .collect(Collectors.toList()));
        val request = new HttpEntity<>(body, headers);
        log.info("reg item: {}", fields);
        val response = restTemplate.exchange(htmlProperties.getUrireg(), HttpMethod.POST,
                request, String.class, fields);
        if(response.hasBody()) {
            log.info("Response: {}", response.getBody());
        }
        return CompletableFuture.completedFuture(response.getStatusCode());
    }

    @Async("htmlExecutor")
    public CompletableFuture<Map<String, Map.Entry<String, SiteState>>> getEvents(String html) {
        val map = new HashMap<String, Map.Entry<String, SiteState>>();
        Jsoup.parse(html).getElementsByClass(classNameList)
                .stream().forEach(element -> {
                    val ref = element.select("a." + classNameItem);
                    if(element.hasClass(classNameHidden) && ref != null) {
                        map.put(htmlProperties.getDomain() + ref.attr("href"),
                                new AbstractMap.SimpleImmutableEntry<>(ref.text(), SiteState.REG_CLOSED));
                    } else {
                        map.put(htmlProperties.getDomain() + ref.attr("href"),
                                new AbstractMap.SimpleImmutableEntry<>(ref.text(), SiteState.REG_OPENED));
                    }
                });
        return CompletableFuture.completedFuture(map);
    }
}

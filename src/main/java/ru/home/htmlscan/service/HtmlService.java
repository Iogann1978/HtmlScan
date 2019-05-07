package ru.home.htmlscan.service;

import org.springframework.http.HttpStatus;
import ru.home.htmlscan.model.SiteState;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface HtmlService {
    CompletableFuture<String> getHtml(String url);
    CompletableFuture<HttpStatus> register(Map<String, String> fields);
    CompletableFuture<Map<String, Map.Entry<String, SiteState>>> getEvents(String html);
}

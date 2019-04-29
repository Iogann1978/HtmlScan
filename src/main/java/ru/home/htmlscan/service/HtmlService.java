package ru.home.htmlscan.service;

import org.springframework.http.HttpStatus;
import ru.home.htmlscan.model.RegisterItem;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface HtmlService {
    CompletableFuture<String> getHtml(String url);
    CompletableFuture<Boolean> checkHtml(String html);
    CompletableFuture<HttpStatus> register(RegisterItem item);
    CompletableFuture<Map<String, String>> checkEmbassies(String html);
    CompletableFuture<Map<String, String>> getEvents(String html);
}

package ru.home.htmlscan.service;

import ru.home.htmlscan.model.SiteItem;

import java.util.Map;

public interface ScanService {
    void run();
    Map<String, SiteItem> getRegister();
}

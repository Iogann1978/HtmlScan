package ru.home.htmlscan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.home.htmlscan.config.HtmlScanProperties;
import ru.home.htmlscan.service.HtmlService;
import ru.home.htmlscan.service.MailService;

@Component
@Slf4j
public class HtmlScanRunner {
	private HtmlScanProperties properties;
	private HtmlService htmlService;
	private MailService mailService;

	@Autowired
    public HtmlScanRunner(HtmlScanProperties properties, HtmlService htmlService, MailService mailService) {
	    this.properties = properties;
	    this.htmlService = htmlService;
	    this.mailService = mailService;
    }

    @Scheduled(fixedDelay = 5000)
    public void run() {
	    if(properties.getSites() == null) {
	        log.error("Site's list is null");
	        return;
        }
        properties.getSites().stream().forEach(u -> htmlService.getHtml(u).thenAccept(c ->
				mailService.checkSite(u, c).thenAccept(f -> {
					if(f) {
						htmlService.register(u);
					}
				})));
    }
}

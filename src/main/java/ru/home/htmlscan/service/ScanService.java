package ru.home.htmlscan.service;

import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.home.htmlscan.config.HtmlScanProperties;
import ru.home.htmlscan.model.SiteItem;
import ru.home.htmlscan.model.SiteState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ScanService {
	private HtmlScanProperties properties;
	private HtmlService htmlService;
	private MailService mailService;
	@Getter
	private Map<String, SiteItem> register = new ConcurrentHashMap<>();

	@Autowired
    public ScanService(HtmlScanProperties properties, HtmlService htmlService, MailService mailService) {
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
        properties.getSites().stream().forEach(uri -> htmlService.getHtml(uri).thenAccept(html -> {
			SiteItem item = register.computeIfAbsent(uri, key -> {
				val siteItem = SiteItem.builder()
						.attemps(0)
						.sended(0)
						.visits(0L)
						.state(SiteState.ADDED)
						.build();
				log.info("Site {} has added to register", uri);
				return siteItem;
			});
			item.visitsInc();

			if(item.getState() == SiteState.ADDED || item.getState() == SiteState.REG_CLOSED) {
				htmlService.checkHtml(uri).thenAccept(flag -> {
					if(flag) {
						item.setState(SiteState.REG_OPENED);
						mailService.sendMessage("Registration is opened", String.format("Registration for site %s is open!", uri));
						log.info("Registration for site {} is open!", uri);
						item.sendedInc();
						item.attempsInc();
						htmlService.register(uri).thenAccept(response -> {
							if(response == HttpStatus.OK) {
								log.info("You has registered on site {}", uri);
								mailService.sendMessage("Successful registration", String.format("You has registered on site %s!", uri));
								item.sendedInc();
								item.setState(SiteState.REGISTERED);
							} else {
								log.error("Registration status code={}, reason: {}", response, response.getReasonPhrase());
							}
						});
					} else if(item.getState() != SiteState.REGISTERED) {
							item.setState(SiteState.REG_CLOSED);
					}
				});
			};
		}));
    }
}

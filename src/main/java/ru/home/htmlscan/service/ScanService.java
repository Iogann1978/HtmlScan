package ru.home.htmlscan.service;

import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.home.htmlscan.config.HtmlProperties;
import ru.home.htmlscan.config.UserProperties;
import ru.home.htmlscan.model.SiteItem;
import ru.home.htmlscan.model.SiteState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ScanService {
	private HtmlProperties htmlProperties;
	private UserProperties userProperties;
	private HtmlService htmlService;
	private MailService mailService;
	@Getter
	private Map<String, SiteItem> register = new ConcurrentHashMap<>();

	@Autowired
    public ScanService(HtmlProperties htmlProperties, UserProperties userProperties, HtmlService htmlService, MailService mailService) {
	    this.htmlProperties = htmlProperties;
	    this.userProperties = userProperties;
	    this.htmlService = htmlService;
	    this.mailService = mailService;
    }

    @Scheduled(fixedDelay = 5000)
    public void run() {
	    if(htmlProperties.getSites() == null) {
	    	// Список сайтов не задан - уходим
	        log.error("Site's list is null");
	        return;
        }

        // Перебираем все сайты из списка
		htmlProperties.getSites().stream().forEach(uri -> htmlService.getHtml(uri).thenAccept(html -> {
        	if(html == null) {
        		// Ошибка при подключении к сайту вернёт строку null, выходим из обработки
        		return;
			}

        	// Если сайта нет в списке отслеживаемых, то добавляем его туда и возвращаем вставленный экземпляр
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
			// Поскольку мы уже получили его html, увеличиваем количество посещений
			item.visitsInc();

			// Если сайт был только добавлен или регистрация на нём пока закрыта, проверяем его на регистрацию
			if(item.getState() == SiteState.ADDED || item.getState() == SiteState.REG_CLOSED) {
				htmlService.checkHtml(uri).thenAccept(flag -> {
					if(flag) {
						// Регистрация открыта
						item.setState(SiteState.REG_OPENED);
						// Отправляем уведомление на почту
						userProperties.getUsers().stream().forEach(user ->
								mailService.sendMessage("Registration is opened", String.format("Registration for site %s is open!", uri), user.getEmail()));
						log.info("Registration for site {} is open!", uri);
						// Увеличиваем количество уведомлений на почту и попыток регистрации
						item.sendedInc();
						item.attempsInc();
						// Пробуем зарегистрироваться
						htmlService.register(uri).thenAccept(response -> {
							if(response == HttpStatus.OK) {
								// Регстрация вернула овет 200, отправляем уведомление на почту
								log.info("You has registered on site {}", uri);
								userProperties.getUsers().stream().forEach(user ->
										mailService.sendMessage("Successful registration", String.format("You has registered on site %s!", uri), user.getEmail()));
								// Увеличиваем количество уведомлений на почту и меняем статус
								item.sendedInc();
								item.setState(SiteState.REGISTERED);
							} else {
								// Регистрация не удалась, выводим причину нв лог
								log.error("Registration status code={}, reason: {}", response, response.getReasonPhrase());
							}
						});
					} else if(item.getState() != SiteState.REGISTERED && item.getState() != SiteState.REG_CLOSED) {
						// Если регистрация закрыта и сайт в состоянии Добавлени или Открыт для регистрации, то меняем его статус
						item.setState(SiteState.REG_CLOSED);
					}
				});
			};
		}));
    }
}

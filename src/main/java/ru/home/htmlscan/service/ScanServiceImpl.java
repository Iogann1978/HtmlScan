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
public class ScanServiceImpl implements ScanService {
	private HtmlProperties htmlProperties;
	private UserProperties userProperties;
	private HtmlService htmlService;
	private MailService mailService;
	// Коллекция сайтов для регистрации
	@Getter
	private Map<String, SiteItem> register = new ConcurrentHashMap<>();
	private static final String phraseEmb = "Посол";

	@Autowired
    public ScanServiceImpl(HtmlProperties htmlProperties, UserProperties userProperties, HtmlService htmlService, MailService mailService) {
	    this.htmlProperties = htmlProperties;
	    this.userProperties = userProperties;
	    this.htmlService = htmlService;
	    this.mailService = mailService;
    }

	@Scheduled(fixedDelay = 5000)
	public void scanList() {
		if(htmlProperties.getUrilist() == null) {
			log.error("[html.urilist] property is null");
			return;
		}
		if(userProperties.getUsers() == null) {
			// Список пользователей не задан - уходим
			log.error("[registration.users] property is null");
			return;
		}

		// Получаем html страницы со всеми событиями
		htmlService.getHtml(htmlProperties.getUrilist()).thenAccept(htmlEvents ->
			htmlEvents.ifPresent(htmlEventsPresent -> {
				htmlService.getEvents(htmlEventsPresent).thenAccept(map -> {
					// Проверяем, есть ли события с посольствами
					map.entrySet().stream().filter(element -> element.getValue().getKey().contains(phraseEmb))
							.forEach(embassy -> {
								SiteItem item = register.computeIfAbsent(embassy.getKey(), key -> {
									val siteItem = SiteItem.builder()
											.visits(0L)
											.attempts(0)
											.sent(0)
											.state(SiteState.ADDED)
											.build();
									log.info("Site {} has added to register", key);
									return siteItem;
								});
								if(item.getState() == SiteState.ADDED) {
									log.info("Embassy detected! {}, uri: {}", embassy.getValue().getKey(), embassy.getKey());
									userProperties.getUsers().stream().forEach(user ->
											mailService.sendMessage("Embassy detected!",
													String.format("Embassy detected! %s, uri: %s", embassy.getValue(), embassy.getKey()),
													user.getEMAIL()));
									item.sentInc();
									item.attemptsInc();
									item.setState(SiteState.EMBASSY_SENDED);
								}
							});

					// Проверяем, есть ли события открытые для регистрации
					map.entrySet().stream().filter(element -> element.getValue().getValue() == SiteState.REG_OPENED)
							.forEach(filteredElement -> {
								val uri = filteredElement.getKey();
								// Если сайта нет в нашем реестре, то добавляем его, иначе возвращаем значение из реестра
								SiteItem item = register.computeIfAbsent(uri, key -> {
									val siteItem = SiteItem.builder()
											.visits(0L)
											.attempts(0)
											.sent(0)
											.state(SiteState.ADDED)
											.build();
									log.info("Site {} has added to register", key);
									return siteItem;
								});

								// Если событие ещё не регистрировались и не отправлялось оповещение на почту
								if (item.getState() != SiteState.OPEN_SENDED && item.getState() != SiteState.REGISTERED) {
									log.info("Registration for site {} is open!", uri);
									userProperties.getUsers().stream().forEach(user ->
											mailService.sendMessage("Registration is open",
													String.format("Registration for site %s is open!", uri),
													user.getEMAIL()));
									item.sentInc();
									item.attemptsInc();
									item.setState(SiteState.OPEN_SENDED);
								}

								// Сайт есть в списке для регистрации и он ещё не регистрировался
								if (item.getState() != SiteState.REGISTERED &&
										(htmlProperties.getSites().contains(uri) || htmlProperties.getSites().isEmpty())) {
									// Пробуем зарегистрироваться
									htmlService.getHtml(uri).thenAccept(htmlReg ->
										htmlReg.ifPresent(htmlRegPresent -> {
											userProperties.getUsers().stream().forEach(user -> {
												item.visitsInc();
												val listFields = user.form(htmlRegPresent);
												listFields.ifPresent(listFieldsPresent ->
													listFieldsPresent.stream().forEach(fields -> {
														htmlService.register(fields).thenAccept(response -> {
															if (response == HttpStatus.OK) {
																// Регистрация вернула ответ 200, отправляем уведомление на почту
																log.info("You has registered on site {}", uri);
																mailService.sendMessage("Successful registration",
																		String.format("You has registered on site %s!", uri), user.getEMAIL());
																// Увеличиваем количество уведомлений на почту и меняем статус
																item.sentInc();
																item.setState(SiteState.REGISTERED);
															} else {
																// Регистрация не удалась, выводим причину в лог
																log.error("Registration status code={}, reason: {}", response, response.getReasonPhrase());
															}
														});
													})
												);
											});
										})
									);
								}
							});
				});
			})
		);
	}
}

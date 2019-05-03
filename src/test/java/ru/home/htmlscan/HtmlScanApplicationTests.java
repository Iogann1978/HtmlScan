package ru.home.htmlscan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;
import ru.home.htmlscan.config.HtmlProperties;
import ru.home.htmlscan.config.UserProperties;
import ru.home.htmlscan.model.RegisterItem;
import ru.home.htmlscan.model.SiteItem;
import ru.home.htmlscan.model.SiteState;
import ru.home.htmlscan.service.*;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class HtmlScanApplicationTests {

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private HtmlProperties htmlProperties;
	@Autowired
	private UserProperties userProperties;
	@Autowired
	private TestRestTemplate testRestTemplate;

	@Mock
	private JavaMailSender testSender;

	private HtmlService htmlService;
	private MailService mailService;
	private String htmlOpened, htmlClosed;

	@Before
	public void setUp() {
		val siteOpened = resourceLoader.getResource("classpath:site_opened.html");
		val siteClosed = resourceLoader.getResource("classpath:site_closed.html");
		try {
			htmlOpened = new String(Files.readAllBytes(siteOpened.getFile().toPath()), StandardCharsets.UTF_8);
			htmlClosed = new String(Files.readAllBytes(siteClosed.getFile().toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

		val mimeMessage = new MimeMessage((Session) null);
		doNothing().when(testSender).send(any(MimeMessage.class));
		when(testSender.createMimeMessage()).thenReturn(mimeMessage);

		mailService = new MailServiceImpl(testSender);
		htmlService = new HtmlServiceImpl(null, htmlProperties);
	}

	// Тестируем отдельную раюоту проверки открытой регистрации в сервисе htmlService
	@Test
	public void checkOpenedTest() {
		assertNotNull(htmlOpened);
		assertNotNull(htmlClosed);

		htmlProperties.getSites().stream().forEach(uri -> {
			try {
				assertFalse(htmlService.checkHtml(htmlClosed).get());
				assertTrue(htmlService.checkHtml(htmlOpened).get());
			} catch (InterruptedException | ExecutionException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		});
	}

	// Этот тест просто для моего спокойствия
	@Test
	public void mapTest() {
		String key = "http://test.ru";
		val map = new ConcurrentHashMap<String, SiteItem>();
		val item1 = SiteItem.builder()
				.attemps(0)
				.sended(0)
				.visits(0L)
				.state(SiteState.REG_CLOSED)
				.build();
		map.put(key, item1);
		val item2 = map.get(key);
		item2.setAttemps(item2.getAttemps() + 1);
		item2.setSended(item2.getSended() + 1);
		item2.setVisits(item2.getVisits() + 1);
		item2.setState(SiteState.REG_OPENED);
		val item3 = map.get(key);
		assertEquals(item2.getAttemps(), item3.getAttemps());
		assertEquals(item2.getSended(), item3.getSended());
		assertEquals(item2.getVisits(), item3.getVisits());
		assertEquals(item2.getState(), item3.getState());
		val item4 = map.computeIfAbsent(key, k -> null);
		assertEquals(item1, item4);
	}

	// Тест доступности всех REST-сервисов
	@Test
    public void restTest() {
	    val responseList = testRestTemplate.exchange("/htmlscan/list", HttpMethod.GET,
                null, new ParameterizedTypeReference<List<String>>(){});
	    assertEquals(HttpStatus.OK, responseList.getStatusCode());
        val responseString = testRestTemplate.getForEntity("/htmlscan/tasks", String.class);
        assertEquals(HttpStatus.OK, responseString.getStatusCode());
		val responseMap = testRestTemplate.exchange("/htmlscan/events", HttpMethod.GET,
				null, new ParameterizedTypeReference<Map<String, String>>(){});
		assertEquals(HttpStatus.OK, responseMap.getStatusCode());
	}

    @Test
	public void parseTest() {
		assertNotNull(htmlOpened);
		assertNotNull(userProperties.getUsers());

		userProperties.getUsers().stream().forEach( item -> {
			val fields = item.form(htmlOpened);
			fields.entrySet().stream().forEach(e -> {
				try {
					log.info("fields: {}={}", e.getKey(), URLEncoder.encode(e.getValue().get(0), "UTF-8"));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
			});
			log.info("registration item: {}", item);
			//log.info("registration fields: {}", fields);
			val uri = UriComponentsBuilder.fromHttpUrl(htmlProperties.getUrireg())
					.queryParams(fields).scheme("https").encode(StandardCharsets.UTF_8);
			log.info("encoded uri={}", uri.toUriString());
			assertNotNull(item);
		});
	}

	@Test
	public void testEventList() {
		val siteEventList = resourceLoader.getResource("classpath:events_list.html");
		try {
			String htmllist = new String(Files.readAllBytes(siteEventList.getFile().toPath()));
			Jsoup.parse(htmllist).getElementsByClass("events_gallery__item__body").forEach(e -> log.info("a={}, href={}", e.text(), e.attr("href")));
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}

package ru.home.htmlscan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.home.htmlscan.config.HtmlProperties;
import ru.home.htmlscan.config.UserProperties;
import ru.home.htmlscan.model.RegisterItem;
import ru.home.htmlscan.model.SiteItem;
import ru.home.htmlscan.model.SiteState;
import ru.home.htmlscan.service.*;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@PropertySource("classpath:application-test.yaml")
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
	@Mock
	private HtmlProperties htmlPropMock;
	@Mock
	private UserProperties userPropMock;
	@Mock
	private HtmlService htmlMock;
	@Mock
	private MailService mailMock;
	@InjectMocks
	private ScanServiceImpl scanService;

	private String htmlOpened, htmlClosed, htmlList, htmlMultiForm;

	@Before
	public void setUp() {
		val siteOpened = resourceLoader.getResource("classpath:site_opened.html");
		val siteClosed = resourceLoader.getResource("classpath:site_closed.html");
		val siteList = resourceLoader.getResource("classpath:events_list.html");
		val siteMultiForm = resourceLoader.getResource("classpath:site_multi_reg.html");
		try {
			htmlOpened = new String(Files.readAllBytes(siteOpened.getFile().toPath()), StandardCharsets.UTF_8);
			htmlClosed = new String(Files.readAllBytes(siteClosed.getFile().toPath()), StandardCharsets.UTF_8);
			htmlList = new String(Files.readAllBytes(siteList.getFile().toPath()), StandardCharsets.UTF_8);
			htmlMultiForm = new String(Files.readAllBytes(siteMultiForm.getFile().toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

		val mimeMessage = new MimeMessage((Session) null);
		doNothing().when(testSender).send(any(MimeMessage.class));
		when(testSender.createMimeMessage()).thenReturn(mimeMessage);
	}

	@Test
	public void mainTest() {
		val url = "https://dni-naslediya.ru/calendar/object/lektsiya-voprosy-sokhraneniya-promyshlennoy-arkhitektury-moskvy/";
		val map = new HashMap<String, Map.Entry<String, SiteState>>() {
			{
				put(url, new AbstractMap.SimpleImmutableEntry<>("Test", SiteState.REG_OPENED));
			}
		};

		when(htmlPropMock.getDomain()).thenReturn(htmlProperties.getDomain());
		when(htmlPropMock.getSites()).thenReturn(htmlProperties.getSites());
		when(htmlPropMock.getUrilist()).thenReturn(htmlProperties.getUrilist());
		when(htmlPropMock.getUrireg()).thenReturn(htmlProperties.getUrireg());
		when(userPropMock.getUsers()).thenReturn(userProperties.getUsers());

		/*
		when(htmlMock.getHtml(url))
				.thenReturn(CompletableFuture.completedFuture(htmlOpened));
		 */
		when(htmlMock.getHtml(url))
				.thenReturn(CompletableFuture.completedFuture(Optional.ofNullable(htmlMultiForm)));
		when(htmlMock.getHtml(htmlProperties.getUrilist()))
				.thenReturn(CompletableFuture.completedFuture(Optional.ofNullable(htmlList)));
		when(htmlMock.getEvents(htmlList))
				.thenReturn(CompletableFuture.completedFuture(map));
		when(htmlMock.register(anyMap()))
				.thenReturn(CompletableFuture.completedFuture(HttpStatus.OK));

		doNothing().when(mailMock).sendMessage(anyString(), anyString(), anyString());

		scanService.scanList();
		verify(htmlMock).getHtml(url);
		verify(htmlMock).getHtml(htmlProperties.getUrilist());
		verify(htmlMock, times(2)).register(anyMap());
		verify(mailMock, times(3)).sendMessage(anyString(), anyString(), anyString());
		assertEquals(1, scanService.getRegister().size());
	}

	// Этот тест просто для моего спокойствия
	@Test
	public void mapTest() {
		String key = "http://test.ru";
		val map = new ConcurrentHashMap<String, SiteItem>();
		val item1 = SiteItem.builder()
				.attempts(0)
				.sent(0)
				.visits(0L)
				.state(SiteState.REG_CLOSED)
				.build();
		map.put(key, item1);
		val item2 = map.get(key);
		item2.attemptsInc();
		item2.sentInc();
		item2.visitsInc();
		item2.setState(SiteState.REG_OPENED);
		val item3 = map.get(key);
		assertEquals(item2.getAttempts(), item3.getAttempts());
		assertEquals(item2.getSent(), item3.getSent());
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
				null, new ParameterizedTypeReference<Map<String, Map.Entry<String, SiteState>>>(){});
		assertEquals(HttpStatus.OK, responseMap.getStatusCode());
	}

    @Test
	public void parseTest() {
		assertNotNull(htmlOpened);
		assertNotNull(htmlClosed);
		assertNotNull(userProperties.getUsers());

		userProperties.getUsers().stream().forEach(item -> {
			log.info("registration item: {}", item);
			assertNotNull(item);
			assertTrue(item.form(htmlClosed).isEmpty());
			assertFalse(item.form(htmlMultiForm).isEmpty());
			val listFields = item.form(htmlOpened);
			assertFalse(listFields.isEmpty());
			listFields.stream().forEach(fields -> fields.entrySet().stream().forEach(e ->
					log.info("fields: {}={}", e.getKey(), e.getValue()))
			);
		});
	}
}

package ru.home.htmlscan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import ru.home.htmlscan.config.HtmlScanProperties;
import ru.home.htmlscan.model.RegisterItem;
import ru.home.htmlscan.model.SiteItem;
import ru.home.htmlscan.model.SiteState;
import ru.home.htmlscan.service.HtmlService;
import ru.home.htmlscan.service.MailService;
import ru.home.htmlscan.service.ScanService;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
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
	private HtmlScanProperties properties;

	@Mock
	private JavaMailSender testSender;
    @Autowired
    private TestRestTemplate testRestTemplate;

    private HtmlService htmlService;
	private MailService mailService;
	private ScanService scanService;
	private Resource siteOpened, siteClosed;

	@Before
	public void setUp() {
		siteOpened = resourceLoader.getResource("classpath:site_opened.html");
		siteClosed = resourceLoader.getResource("classpath:site_closed.html");

		val mimeMessage = new MimeMessage((Session) null);
		doNothing().when(testSender).send(any(MimeMessage.class));
		when(testSender.createMimeMessage()).thenReturn(mimeMessage);

		mailService = new MailService(testSender);
		htmlService = new HtmlService(null, properties);
		scanService = new ScanService(properties, htmlService, mailService);
	}

	@Test
	public void checkOpenedTest() {
		assertNotNull(siteOpened);
		assertNotNull(siteClosed);

		try {
			val htmlOpened = new String(Files.readAllBytes(siteOpened.getFile().toPath()));
			val htmlClosed = new String(Files.readAllBytes(siteClosed.getFile().toPath()));
			properties.getSites().stream().forEach(uri -> {
				try {
					assertFalse(htmlService.checkHtml(htmlClosed).get());
					assertTrue(htmlService.checkHtml(htmlOpened).get());
				} catch (InterruptedException | ExecutionException e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
				assertEquals(scanService.getRegister().get(uri).getSended(), 1);
			});
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void mapTest() {
		String key = "http://test/ru";
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

	@Test
    public void restTest() {
	    val responseList = testRestTemplate.exchange("/htmlscan/list", HttpMethod.GET,
                null, new ParameterizedTypeReference<List<String>>(){});;
	    assertEquals(responseList.getStatusCode(), HttpStatus.OK);
        val responseString = testRestTemplate.getForEntity("/htmlscan/tasks", String.class);
        assertEquals(responseString.getStatusCode(), HttpStatus.OK);
    }

    @Test
	public void parseTest() {
		assertNotNull(siteOpened);

		try {
			val htmlOpened = new String(Files.readAllBytes(siteOpened.getFile().toPath()));
			val item = RegisterItem.get(htmlOpened);
			log.info("registration item: {}", item);
			assertNotNull(item);
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}

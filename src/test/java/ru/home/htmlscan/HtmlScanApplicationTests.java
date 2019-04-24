package ru.home.htmlscan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;
import ru.home.htmlscan.config.HtmlScanProperties;
import ru.home.htmlscan.model.SiteItem;
import ru.home.htmlscan.model.SiteState;
import ru.home.htmlscan.service.MailService;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@Profile("test")
@Slf4j
public class HtmlScanApplicationTests {

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private HtmlScanProperties properties;
	@Mock
	private JavaMailSender testSender;

	private MailService mailService;

	@Before
	public void setUp() {
		val mimeMessage = new MimeMessage((Session) null);
		doNothing().when(testSender).send(any(MimeMessage.class));
		when(testSender.createMimeMessage()).thenReturn(mimeMessage);
		mailService = new MailService(testSender);
	}

	@Test
	public void checkOpenedTest() {
		val siteOpened = resourceLoader.getResource("classpath:site_opened.html");
		assertNotNull(siteOpened);
		val siteClosed = resourceLoader.getResource("classpath:site_closed.html");
		assertNotNull(siteClosed);

		try {
			val htmlOpened = new String(Files.readAllBytes(siteOpened.getFile().toPath()));
			val htmlClosed = new String(Files.readAllBytes(siteClosed.getFile().toPath()));
			properties.getSites().stream().forEach(u -> {
				mailService.checkSite(u, htmlOpened);
				mailService.checkSite(u, htmlClosed);
			});
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

		try {
			TimeUnit.SECONDS.sleep(10L);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	//@Test
	public void mailSendTest() {
		mailService.sendMessage("Test", "Test");
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
	}
}

package ru.home.htmlscan;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;
import ru.home.htmlscan.config.HtmlScanProperties;
import ru.home.htmlscan.service.MailService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@Profile("test")
public class HtmlScanApplicationTests {

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private MailService mailService;
	@Autowired
	private HtmlScanProperties properties;

	@Test
	public void contextLoads() {
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
			e.printStackTrace();
		}
	}

}

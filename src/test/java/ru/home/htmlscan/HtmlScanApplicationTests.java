package ru.home.htmlscan;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Profile("test")
@Slf4j
public class HtmlScanApplicationTests {

	@Autowired
	private ResourceLoader resourceLoader;

	@Test
	public void contextLoads() {
		resourceLoader.getResource("classpath:site_opened.html");
	}
}

package ru.home.htmlscan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("file:config/application.yaml")
public class HtmlScanApplication {

	public static void main(String[] args) {
		SpringApplication.run(HtmlScanApplication.class, args);
	}

}

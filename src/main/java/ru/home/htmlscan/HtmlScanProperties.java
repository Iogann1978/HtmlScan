package ru.home.htmlscan;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="html")
@Data
public class HtmlScanProperties {
	private List<String> sites = new ArrayList<String>();
}

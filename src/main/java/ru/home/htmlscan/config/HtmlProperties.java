package ru.home.htmlscan.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="html")
@Data
public class HtmlProperties {
	private List<String> sites = new ArrayList<String>();
	private String domain;
	private String urireg;
	private String urireserv;
	private String urilist;
}

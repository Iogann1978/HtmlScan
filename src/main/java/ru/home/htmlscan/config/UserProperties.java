package ru.home.htmlscan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.home.htmlscan.model.RegisterItem;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix="registration")
@Data
public class UserProperties {
    private List<RegisterItem> users = new ArrayList<>();
}

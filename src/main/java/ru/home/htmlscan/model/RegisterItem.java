package ru.home.htmlscan.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Data
@ToString
@NoArgsConstructor
@Slf4j
public class RegisterItem {
    private String PASSPORT_NUMBER, PASSPORT_GET, PASSPORT_GET_DATE, YEAR;
    @NonNull
    private String FIO, PHONE, EMAIL;

    public MultiValueMap<String, String> form(String html) {
        val form = Jsoup.parse(html).selectFirst("form");
        val fields = new LinkedMultiValueMap<String, String>();
        form.getAllElements().stream()
                .filter(element -> !element.attr("name").isEmpty())
                .forEach(element_filtered -> {
                    val key = element_filtered.attr("name");
                    switch(key) {
                        case "FIO":
                            fields.add(key, encodeField(FIO));
                            break;
                        case "PHONE":
                            fields.add(key, encodeField(PHONE));
                            break;
                        case "EMAIL":
                            fields.add(key, encodeField(EMAIL));
                            break;
                        case "PASSPORT_NUMBER":
                            fields.add(key, encodeField(PASSPORT_NUMBER));
                            break;
                        case "PASSPORT_GET":
                            fields.add(key, encodeField(PASSPORT_GET));
                            break;
                        case "PASSPORT_GET_DATE":
                            fields.add(key, encodeField(PASSPORT_GET_DATE));
                            break;
                        case "YEAR":
                            fields.add(key, encodeField(YEAR));
                            break;
                        default:
                            fields.add(key, encodeField(element_filtered.attr("value")));
                            break;
                    }
                });
        return fields;
    }

    public static String encodeField(String field) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(field, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return encoded;
    }
}
